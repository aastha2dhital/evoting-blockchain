package com.example.evotingmobileapp.data

import android.content.Context
import com.example.evotingmobileapp.blockchain.BlockchainRepository
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
            val chainElections = blockchainRepository.getAllElectionsOnChain(appContext)
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
        val parsedElectionId = parseElectionId(electionId)
            ?: return Result.failure(
                IllegalArgumentException("Election ID must be a valid non-negative integer.")
            )

        return blockchainRepository.checkInVoterOnChain(
            context = context,
            electionId = parsedElectionId,
            voterWalletAddress = voterWalletAddress.trim()
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

        val cleanedEligibleWalletAddresses = eligibleVoterIds
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

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

    override fun checkInVoter(electionId: String, voterId: String): String {
        val cleanedElectionId = electionId.trim()
        val parsedElectionId = parseElectionId(cleanedElectionId)
            ?: return "Election ID must be a valid non-negative integer."

        val normalizedWalletAddress = normalizeWalletAddress(voterId)

        if (normalizedWalletAddress.isBlank()) {
            return "Voter wallet address cannot be blank."
        }

        val onChainResult = blockchainRepository.checkInVoterOnChain(
            context = appContext,
            electionId = parsedElectionId,
            voterWalletAddress = normalizedWalletAddress
        )

        return onChainResult.fold(
            onSuccess = {
                val currentElection = getElectionById(cleanedElectionId)
                    ?: return@fold "Election not found."

                val updatedElection = currentElection.copy(
                    checkedInVoterIds = currentElection.checkedInVoterIds + normalizedWalletAddress
                )

                upsertElection(updatedElection)
                refreshFromBlockchain()

                "Check-in successful"
            },
            onFailure = { exception ->
                exception.message ?: "Blockchain check-in failed."
            }
        )
    }

    override fun validateVoting(
        electionId: String,
        voterId: String
    ): VoteValidationResult {
        val normalizedWalletAddress = normalizeWalletAddress(voterId)

        if (normalizedWalletAddress.isBlank()) {
            return VoteValidationResult(
                success = false,
                message = "Voter ID required"
            )
        }

        val election = getElectionById(electionId.trim())
            ?: return VoteValidationResult(
                success = false,
                message = "Election not found"
            )

        if (!election.eligibleVoterIds.contains(normalizedWalletAddress)) {
            return VoteValidationResult(
                success = false,
                message = "Not eligible"
            )
        }

        if (!election.checkedInVoterIds.contains(normalizedWalletAddress)) {
            return VoteValidationResult(
                success = false,
                message = "Not checked-in"
            )
        }

        if (!election.hasStarted()) {
            return VoteValidationResult(
                success = false,
                message = "Election not started"
            )
        }

        if (election.isClosed()) {
            return VoteValidationResult(
                success = false,
                message = "Election closed"
            )
        }

        if (election.votedVoterIds.contains(normalizedWalletAddress)) {
            return VoteValidationResult(
                success = false,
                message = "Already voted"
            )
        }

        return VoteValidationResult(
            success = true,
            message = "Eligible to vote"
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

        val candidateIndex = election.candidates.indexOfFirst {
            it == cleanedCandidateName
        }

        if (candidateIndex == -1) {
            return VoteValidationResult(
                success = false,
                message = "Selected candidate was not found."
            )
        }

        val candidateId = BigInteger.valueOf((candidateIndex + 1).toLong())

        val onChainVoteResult = blockchainRepository.voteOnChain(
            context = appContext,
            electionId = parseElectionId(cleanedElectionId)
                ?: return VoteValidationResult(
                    success = false,
                    message = "Election ID must be a valid non-negative integer."
                ),
            candidateId = candidateId,
            voterWalletAddress = normalizedWalletAddress
        )

        return onChainVoteResult.fold(
            onSuccess = { realTxHash ->
                val updatedVoteCounts = election.voteCounts.toMutableMap()
                updatedVoteCounts[cleanedCandidateName] =
                    (updatedVoteCounts[cleanedCandidateName] ?: 0) + 1

                val updatedElection = election.copy(
                    voteCounts = updatedVoteCounts,
                    votedVoterIds = election.votedVoterIds + normalizedWalletAddress
                )

                upsertElection(updatedElection)

                val receipt = VoteReceipt(
                    electionId = election.id,
                    electionTitle = election.title,
                    candidateName = cleanedCandidateName,
                    voterId = normalizedWalletAddress,
                    transactionHash = realTxHash,
                    timestamp = System.currentTimeMillis()
                )

                _voteReceipts.value = _voteReceipts.value + receipt
                persistSnapshot()

                refreshFromBlockchain()

                VoteValidationResult(
                    success = true,
                    message = "Vote submitted successfully",
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
}