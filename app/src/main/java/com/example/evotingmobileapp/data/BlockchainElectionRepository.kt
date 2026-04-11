package com.example.evotingmobileapp.data

import android.content.Context
import com.example.evotingmobileapp.blockchain.BlockchainRepository
import com.example.evotingmobileapp.model.Election
import com.example.evotingmobileapp.model.VoteReceipt
import java.math.BigInteger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BlockchainElectionRepository(
    private val appContext: Context,
    private val blockchainRepository: BlockchainRepository = BlockchainRepository(),
    private val fallbackRepository: InMemoryElectionRepository = InMemoryElectionRepository()
) : ElectionRepository {

    override val elections: StateFlow<List<Election>> = fallbackRepository.elections

    private val _voteReceipts = MutableStateFlow<List<VoteReceipt>>(emptyList())
    override val voteReceipts: StateFlow<List<VoteReceipt>> = _voteReceipts.asStateFlow()

    init {
        syncVoteReceiptsFromFallback()
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

        val realElectionId = onChainResult.electionId.toString()

        fallbackRepository.createElectionWithId(
            electionId = realElectionId,
            title = cleanedTitle,
            candidates = cleanedCandidates,
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis,
            eligibleVoterIds = cleanedEligibleWalletAddresses
        )

        syncVoteReceiptsFromFallback()
    }

    override fun getElectionById(electionId: String): Election? {
        return fallbackRepository.getElectionById(electionId.trim())
    }

    override fun checkInVoter(electionId: String, voterId: String): String {
        val cleanedElectionId = electionId.trim()
        val parsedElectionId = parseElectionId(cleanedElectionId)
            ?: return "Election ID must be a valid non-negative integer."

        val cleanedWalletAddress = voterId.trim()

        val onChainResult = blockchainRepository.checkInVoterOnChain(
            context = appContext,
            electionId = parsedElectionId,
            voterWalletAddress = cleanedWalletAddress
        )

        return onChainResult.fold(
            onSuccess = {
                val result = fallbackRepository.checkInVoter(
                    electionId = cleanedElectionId,
                    voterId = cleanedWalletAddress
                )
                syncVoteReceiptsFromFallback()
                result
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
        return fallbackRepository.validateVoting(
            electionId = electionId.trim(),
            voterId = voterId.trim()
        )
    }

    override fun vote(
        electionId: String,
        voterId: String,
        candidateName: String
    ): VoteValidationResult {
        val cleanedElectionId = electionId.trim()
        val cleanedWalletAddress = voterId.trim()
        val cleanedCandidateName = candidateName.trim()

        val parsedElectionId = parseElectionId(cleanedElectionId)
            ?: return VoteValidationResult(
                success = false,
                message = "Election ID must be a valid non-negative integer."
            )

        val election = fallbackRepository.getElectionById(cleanedElectionId)
            ?: return VoteValidationResult(
                success = false,
                message = "Election not found."
            )

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
            electionId = parsedElectionId,
            candidateId = candidateId,
            voterWalletAddress = cleanedWalletAddress
        )

        return onChainVoteResult.fold(
            onSuccess = { realTxHash ->
                val localResult = fallbackRepository.vote(
                    electionId = cleanedElectionId,
                    voterId = cleanedWalletAddress,
                    candidateName = cleanedCandidateName
                )

                val finalResult = if (localResult.success && localResult.receipt != null) {
                    localResult.copy(
                        receipt = localResult.receipt.copy(
                            transactionHash = realTxHash
                        )
                    )
                } else {
                    localResult
                }

                syncVoteReceiptsAfterVote(finalResult.receipt)

                finalResult
            },
            onFailure = { exception ->
                VoteValidationResult(
                    success = false,
                    message = exception.message ?: "Blockchain vote failed."
                )
            }
        )
    }

    private fun syncVoteReceiptsFromFallback() {
        _voteReceipts.value = fallbackRepository.voteReceipts.value
    }

    private fun syncVoteReceiptsAfterVote(latestReceipt: VoteReceipt?) {
        val fallbackReceipts = fallbackRepository.voteReceipts.value

        _voteReceipts.value = if (latestReceipt == null || fallbackReceipts.isEmpty()) {
            fallbackReceipts
        } else {
            fallbackReceipts.dropLast(1) + latestReceipt
        }
    }

    private fun parseElectionId(electionId: String): BigInteger? {
        val parsedElectionId = electionId.trim().toBigIntegerOrNull() ?: return null
        return if (parsedElectionId >= BigInteger.ZERO) parsedElectionId else null
    }
}