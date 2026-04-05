package com.example.evotingmobileapp.data

import android.content.Context
import com.example.evotingmobileapp.blockchain.BlockchainRepository
import com.example.evotingmobileapp.model.Election
import com.example.evotingmobileapp.model.VoteReceipt
import kotlinx.coroutines.flow.StateFlow
import java.math.BigInteger

class BlockchainElectionRepository(
    private val blockchainRepository: BlockchainRepository = BlockchainRepository(),
    private val fallbackRepository: ElectionRepository = InMemoryElectionRepository()
) : ElectionRepository {

    override val elections: StateFlow<List<Election>> = fallbackRepository.elections

    override val voteReceipts: StateFlow<List<VoteReceipt>> = fallbackRepository.voteReceipts

    fun checkInVoterOnChain(
        context: Context,
        electionId: String,
        voterWalletAddress: String
    ): Result<String> {
        val parsedElectionId = electionId.toBigIntegerOrNull()
            ?: return Result.failure(
                IllegalArgumentException("Election ID must be a valid non-negative integer.")
            )

        if (parsedElectionId < BigInteger.ZERO) {
            return Result.failure(
                IllegalArgumentException("Election ID must be a valid non-negative integer.")
            )
        }

        return blockchainRepository.checkInVoterOnChain(
            context = context,
            electionId = parsedElectionId,
            voterWalletAddress = voterWalletAddress
        )
    }

    override fun createElection(
        title: String,
        candidates: List<String>,
        startTimeMillis: Long,
        endTimeMillis: Long,
        eligibleVoterIds: List<String>
    ) {
        fallbackRepository.createElection(
            title = title,
            candidates = candidates,
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis,
            eligibleVoterIds = eligibleVoterIds
        )
    }

    override fun getElectionById(electionId: String): Election? {
        return fallbackRepository.getElectionById(electionId)
    }

    override fun checkInVoter(electionId: String, voterId: String): String {
        return fallbackRepository.checkInVoter(electionId, voterId)
    }

    override fun validateVoting(electionId: String, voterId: String): VoteValidationResult {
        return fallbackRepository.validateVoting(electionId, voterId)
    }

    override fun vote(
        electionId: String,
        voterId: String,
        candidateName: String
    ): VoteValidationResult {
        return fallbackRepository.vote(
            electionId = electionId,
            voterId = voterId,
            candidateName = candidateName
        )
    }
}