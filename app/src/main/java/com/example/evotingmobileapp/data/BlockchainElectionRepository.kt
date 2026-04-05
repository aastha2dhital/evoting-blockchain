package com.example.evotingmobileapp.data

import com.example.evotingmobileapp.model.Election
import com.example.evotingmobileapp.model.VoteReceipt
import kotlinx.coroutines.flow.StateFlow

class BlockchainElectionRepository(
    private val fallbackRepository: ElectionRepository = InMemoryElectionRepository()
) : ElectionRepository {

    override val elections: StateFlow<List<Election>> = fallbackRepository.elections

    override val voteReceipts: StateFlow<List<VoteReceipt>> = fallbackRepository.voteReceipts

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