package com.example.evotingmobileapp.data

import android.content.Context
import com.example.evotingmobileapp.blockchain.BlockchainRepository
import com.example.evotingmobileapp.blockchain.OnChainTransactionVerification
import com.example.evotingmobileapp.model.Election
import com.example.evotingmobileapp.model.VoteReceipt
import java.math.BigInteger
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BlockchainElectionRepository(
    private val appContext: Context,
    private val blockchainRepository: BlockchainRepository = BlockchainRepository(),
    private val cacheStore: ElectionCacheStore = ElectionCacheStore(appContext)
) : ElectionRepository {

    private val _elections = MutableStateFlow(cacheStore.loadElections())
    override val elections: StateFlow<List<Election>> = _elections.asStateFlow()

    private val _voteReceipts = MutableStateFlow(cacheStore.loadVoteReceipts())
    override val voteReceipts: StateFlow<List<VoteReceipt>> = _voteReceipts.asStateFlow()

    fun refreshFromBlockchain(): Result<Unit> {
        return try {
            val chainElections = blockchainRepository
                .getAllElectionsOnChain(appContext)
                .getOrThrow()

            val mergedElections = mergeChainAndCachedElections(chainElections)

            _elections.value = mergedElections
            _voteReceipts.value = _voteReceipts.value.filter { receipt ->
                mergedElections.any { election -> election.id == receipt.electionId }
            }

            persistSnapshot()

            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    fun checkInVoterOnChain(
        context: Context,
        electionId: String,
        voterWalletAddress: String
    ): Result<String> {
        val cleanedElectionId = electionId.trim()
        val parsedElectionId = parseElectionId(cleanedElectionId)
            ?: return Result.failure(
                IllegalArgumentException("Election ID must be a valid non-negative integer.")
            )

        val normalizedWalletAddress = normalizeWalletAddress(voterWalletAddress)

        if (normalizedWalletAddress.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Voter wallet address cannot be blank.")
            )
        }

        return blockchainRepository.checkInVoterOnChain(
            context = context,
            electionId = parsedElectionId,
            voterWalletAddress = normalizedWalletAddress
        ).fold(
            onSuccess = { txHash ->
                applySuccessfulCheckIn(
                    electionId = cleanedElectionId,
                    normalizedWalletAddress = normalizedWalletAddress
                )

                Result.success("Check-in successful. Transaction: ${shortenHash(txHash)}")
            },
            onFailure = { exception ->
                Result.failure(
                    IllegalStateException(
                        exception.message ?: "Blockchain check-in failed.",
                        exception
                    )
                )
            }
        )
    }

    fun verifyTransactionReceiptOnChain(
        context: Context,
        transactionHash: String
    ): Result<OnChainTransactionVerification> {
        return blockchainRepository.verifyTransactionReceiptOnChain(
            context = context,
            transactionHash = transactionHash.trim()
        )
    }

    override fun createElection(
        title: String,
        candidates: List<String>,
        startTimeMillis: Long,
        endTimeMillis: Long,
        eligibleVoterIds: List<String>
    ) {
        val cleanedTitle = title.trim()

        val cleanedCandidates = candidates
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        require(cleanedTitle.isNotBlank()) {
            "Election title is required."
        }

        require(cleanedCandidates.size >= 2) {
            "At least two candidates are required."
        }

        require(endTimeMillis > startTimeMillis) {
            "Election end time must be after start time."
        }

        val cleanedEligibleWalletAddresses = eligibleVoterIds
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase(Locale.ROOT) }

        require(cleanedEligibleWalletAddresses.isNotEmpty()) {
            "At least one eligible voter wallet is required."
        }

        val normalizedEligibleWalletAddresses = cleanedEligibleWalletAddresses
            .map { normalizeWalletAddress(it) }
            .toSet()

        val startTimeSeconds = BigInteger.valueOf(startTimeMillis / 1000L)
        val endTimeSeconds = BigInteger.valueOf(endTimeMillis / 1000L)

        val onChainResult = blockchainRepository.createElectionOnChain(
            context = appContext,
            title = cleanedTitle,
            candidateNames = cleanedCandidates,
            startTimeSeconds = startTimeSeconds,
            endTimeSeconds = endTimeSeconds,
            eligibleVoterAddresses = cleanedEligibleWalletAddresses
        ).getOrElse { exception ->
            throw IllegalStateException(
                exception.message ?: "Failed to create election on blockchain.",
                exception
            )
        }

        val localElection = Election(
            id = onChainResult.electionId.toString(),
            title = cleanedTitle,
            candidates = cleanedCandidates,
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis,
            voteCounts = cleanedCandidates.associateWith { 0 },
            votedVoterIds = emptySet(),
            eligibleVoterIds = normalizedEligibleWalletAddresses,
            checkedInVoterIds = emptySet()
        )

        upsertElection(localElection)
        refreshFromBlockchain()
    }

    override fun getElectionById(electionId: String): Election? {
        val cleanedElectionId = electionId.trim()
        return _elections.value.find { it.id == cleanedElectionId }
    }

    override fun checkInVoter(
        electionId: String,
        voterId: String
    ): String {
        return checkInVoterOnChain(
            context = appContext,
            electionId = electionId,
            voterWalletAddress = voterId
        ).fold(
            onSuccess = { message -> message },
            onFailure = { exception ->
                exception.message ?: "Blockchain check-in failed."
            }
        )
    }

    override fun validateVoting(
        electionId: String,
        voterId: String
    ): VoteValidationResult {
        val cleanedElectionId = electionId.trim()
        val normalizedWalletAddress = normalizeWalletAddress(voterId)

        if (normalizedWalletAddress.isBlank()) {
            return VoteValidationResult(
                success = false,
                message = "No active voter wallet session found."
            )
        }

        val parsedElectionId = parseElectionId(cleanedElectionId)
            ?: return VoteValidationResult(
                success = false,
                message = "Election ID must be a valid non-negative integer."
            )

        refreshFromBlockchain()

        val election = getElectionById(cleanedElectionId)
            ?: return VoteValidationResult(
                success = false,
                message = "Election not found."
            )

        val isEligible = blockchainRepository.isEligibleVoterOnChain(
            context = appContext,
            electionId = parsedElectionId,
            voterWalletAddress = normalizedWalletAddress
        ).getOrElse { exception ->
            return VoteValidationResult(
                success = false,
                message = exception.message ?: "Failed to check voter eligibility on blockchain."
            )
        }

        if (!isEligible) {
            return VoteValidationResult(
                success = false,
                message = "This voter is not in the eligible voter whitelist."
            )
        }

        val isCheckedIn = blockchainRepository.isCheckedInOnChain(
            context = appContext,
            electionId = parsedElectionId,
            voterWalletAddress = normalizedWalletAddress
        ).getOrElse { exception ->
            return VoteValidationResult(
                success = false,
                message = exception.message ?: "Failed to check voter check-in status on blockchain."
            )
        }

        if (!isCheckedIn) {
            return VoteValidationResult(
                success = false,
                message = "This voter has not completed QR check-in yet."
            )
        }

        if (!election.hasStarted()) {
            return VoteValidationResult(
                success = false,
                message = "Election has not started yet."
            )
        }

        if (election.isClosed()) {
            return VoteValidationResult(
                success = false,
                message = "Election is closed."
            )
        }

        val hasVoted = blockchainRepository.hasVotedOnChain(
            context = appContext,
            electionId = parsedElectionId,
            voterWalletAddress = normalizedWalletAddress
        ).getOrElse { exception ->
            return VoteValidationResult(
                success = false,
                message = exception.message ?: "Failed to check voter voting status on blockchain."
            )
        }

        if (hasVoted) {
            return VoteValidationResult(
                success = false,
                message = "This voter has already voted in this election."
            )
        }

        return VoteValidationResult(
            success = true,
            message = "Eligible and checked-in. Ready to vote."
        )
    }

    override fun vote(
        electionId: String,
        voterId: String,
        candidateName: String
    ): VoteValidationResult {
        val cleanedElectionId = electionId.trim()
        val normalizedWalletAddress = normalizeWalletAddress(voterId)
        val cleanedCandidateName = candidateName.trim()

        val election = getElectionById(cleanedElectionId)
            ?: return VoteValidationResult(
                success = false,
                message = "Election not found."
            )

        val validation = validateVoting(
            electionId = cleanedElectionId,
            voterId = normalizedWalletAddress
        )

        if (!validation.success) {
            return validation
        }

        val candidateId = resolveCandidateId(
            election = election,
            candidateName = cleanedCandidateName
        ) ?: return VoteValidationResult(
            success = false,
            message = "Selected candidate was not found."
        )

        val parsedElectionId = parseElectionId(cleanedElectionId)
            ?: return VoteValidationResult(
                success = false,
                message = "Election ID must be a valid non-negative integer."
            )

        val onChainVoteResult = blockchainRepository.voteOnChain(
            context = appContext,
            electionId = parsedElectionId,
            candidateId = candidateId,
            voterWalletAddress = normalizedWalletAddress
        )

        return onChainVoteResult.fold(
            onSuccess = { realTxHash ->
                val updatedElection = buildElectionAfterSuccessfulVote(
                    election = election,
                    cleanedCandidateName = cleanedCandidateName,
                    normalizedWalletAddress = normalizedWalletAddress
                )

                upsertElection(updatedElection)

                val receipt = createVoteReceipt(
                    election = election,
                    cleanedCandidateName = cleanedCandidateName,
                    normalizedWalletAddress = normalizedWalletAddress,
                    transactionHash = realTxHash
                )

                _voteReceipts.value = (_voteReceipts.value + receipt)
                    .distinctBy { it.transactionHash.lowercase(Locale.ROOT) }

                persistSnapshot()
                refreshFromBlockchain()

                VoteValidationResult(
                    success = true,
                    message = "Vote submitted successfully.",
                    receipt = receipt
                )
            },
            onFailure = { exception ->
                VoteValidationResult(
                    success = false,
                    message = exception.message ?: "Blockchain vote failed."
                )
            }
        )
    }

    override fun closeElectionEarly(electionId: String): Result<String> {
        val cleanedElectionId = electionId.trim()
        val parsedElectionId = parseElectionId(cleanedElectionId)
            ?: return Result.failure(
                IllegalArgumentException("Election ID must be a valid non-negative integer.")
            )

        val election = getElectionById(cleanedElectionId)
            ?: return Result.failure(
                IllegalStateException("Election not found.")
            )

        if (election.isClosed()) {
            return Result.failure(
                IllegalStateException("Election is already closed.")
            )
        }

        val onChainResult = blockchainRepository.closeElectionEarlyOnChain(
            context = appContext,
            electionId = parsedElectionId
        )

        return onChainResult.fold(
            onSuccess = {
                refreshFromBlockchain().fold(
                    onSuccess = {
                        Result.success("Election closed successfully.")
                    },
                    onFailure = { refreshException ->
                        Result.failure(
                            IllegalStateException(
                                refreshException.message
                                    ?: "Election was closed on-chain, but refresh failed.",
                                refreshException
                            )
                        )
                    }
                )
            },
            onFailure = { exception ->
                Result.failure(
                    IllegalStateException(
                        exception.message ?: "Failed to close election early.",
                        exception
                    )
                )
            }
        )
    }

    override fun getTurnoutCount(electionId: String): Result<Int> {
        val cleanedElectionId = electionId.trim()
        val parsedElectionId = parseElectionId(cleanedElectionId)
            ?: return Result.failure(
                IllegalArgumentException("Election ID must be a valid non-negative integer.")
            )

        return blockchainRepository.getTurnoutCountOnChain(
            context = appContext,
            electionId = parsedElectionId
        )
    }

    private fun mergeChainAndCachedElections(
        chainElections: List<Election>
    ): List<Election> {
        val cachedById = _elections.value.associateBy { it.id }

        return chainElections
            .map { chainElection ->
                val cachedElection = cachedById[chainElection.id]

                if (cachedElection == null) {
                    chainElection
                } else {
                    chainElection.copy(
                        eligibleVoterIds = cachedElection.eligibleVoterIds,
                        checkedInVoterIds = cachedElection.checkedInVoterIds,
                        votedVoterIds = cachedElection.votedVoterIds
                    )
                }
            }
            .sortedBy { it.id.toIntOrNull() ?: Int.MAX_VALUE }
    }

    private fun applySuccessfulCheckIn(
        electionId: String,
        normalizedWalletAddress: String
    ) {
        val currentElection = getElectionById(electionId) ?: return

        val updatedElection = currentElection.copy(
            checkedInVoterIds = currentElection.checkedInVoterIds + normalizedWalletAddress
        )

        upsertElection(updatedElection)
        refreshFromBlockchain()
    }

    private fun resolveCandidateId(
        election: Election,
        candidateName: String
    ): BigInteger? {
        val candidateIndex = election.candidates.indexOfFirst {
            it.equals(candidateName, ignoreCase = false)
        }

        if (candidateIndex == -1) return null

        return BigInteger.valueOf((candidateIndex + 1).toLong())
    }

    private fun buildElectionAfterSuccessfulVote(
        election: Election,
        cleanedCandidateName: String,
        normalizedWalletAddress: String
    ): Election {
        val updatedVoteCounts = election.voteCounts.toMutableMap()

        updatedVoteCounts[cleanedCandidateName] =
            (updatedVoteCounts[cleanedCandidateName] ?: 0) + 1

        return election.copy(
            voteCounts = updatedVoteCounts,
            votedVoterIds = election.votedVoterIds + normalizedWalletAddress
        )
    }

    private fun createVoteReceipt(
        election: Election,
        cleanedCandidateName: String,
        normalizedWalletAddress: String,
        transactionHash: String
    ): VoteReceipt {
        return VoteReceipt(
            electionId = election.id,
            electionTitle = election.title,
            candidateName = cleanedCandidateName,
            voterId = normalizedWalletAddress,
            transactionHash = transactionHash,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun upsertElection(updatedElection: Election) {
        _elections.value = (
                _elections.value.filterNot { it.id == updatedElection.id } + updatedElection
                ).sortedBy { it.id.toIntOrNull() ?: Int.MAX_VALUE }

        persistSnapshot()
    }

    private fun persistSnapshot() {
        cacheStore.saveElections(_elections.value)
        cacheStore.saveVoteReceipts(_voteReceipts.value)
    }

    private fun parseElectionId(electionId: String): BigInteger? {
        val parsedElectionId = electionId.trim().toBigIntegerOrNull() ?: return null
        return if (parsedElectionId >= BigInteger.ZERO) parsedElectionId else null
    }

    private fun normalizeWalletAddress(walletAddress: String): String {
        return walletAddress.trim().lowercase(Locale.ROOT)
    }

    private fun shortenHash(hash: String): String {
        if (hash.length <= 18) return hash
        return "${hash.take(10)}...${hash.takeLast(8)}"
    }
}