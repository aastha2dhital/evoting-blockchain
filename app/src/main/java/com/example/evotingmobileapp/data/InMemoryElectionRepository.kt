package com.example.evotingmobileapp.data

import com.example.evotingmobileapp.model.Election
import com.example.evotingmobileapp.model.VoteReceipt
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryElectionRepository : ElectionRepository {

    private val _elections = MutableStateFlow<List<Election>>(emptyList())
    override val elections: StateFlow<List<Election>> = _elections.asStateFlow()

    private val _voteReceipts = MutableStateFlow<List<VoteReceipt>>(emptyList())
    override val voteReceipts: StateFlow<List<VoteReceipt>> = _voteReceipts.asStateFlow()

    private val defaultEligibleVoterIds = setOf("voter001")

    override fun createElection(
        title: String,
        candidates: List<String>,
        startTimeMillis: Long,
        endTimeMillis: Long,
        eligibleVoterIds: List<String>
    ) {
        val electionId = (_elections.value.size + 1).toString()

        val cleanedTitle = title.trim()

        val cleanedCandidates = candidates
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()

        val cleanedEligibleVoterIds = eligibleVoterIds
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
            .ifEmpty { defaultEligibleVoterIds }

        val voteCounts = cleanedCandidates.associateWith { 0 }

        val newElection = Election(
            id = electionId,
            title = cleanedTitle,
            candidates = cleanedCandidates,
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis,
            voteCounts = voteCounts,
            votedVoterIds = emptySet(),
            eligibleVoterIds = cleanedEligibleVoterIds,
            checkedInVoterIds = emptySet()
        )

        _elections.value = _elections.value + newElection
    }

    override fun getElectionById(electionId: String): Election? {
        return _elections.value.find { it.id == electionId }
    }

    override fun checkInVoter(electionId: String, voterId: String): String {
        val normalizedVoterId = voterId.trim()

        if (normalizedVoterId.isEmpty()) {
            return "Voter ID required"
        }

        val currentElection = getElectionById(electionId)
            ?: return "Election not found"

        if (!currentElection.eligibleVoterIds.contains(normalizedVoterId)) {
            return "Not eligible"
        }

        if (currentElection.checkedInVoterIds.contains(normalizedVoterId)) {
            return "Already checked-in"
        }

        val updatedElection = currentElection.copy(
            checkedInVoterIds = currentElection.checkedInVoterIds + normalizedVoterId
        )

        _elections.value = _elections.value.map { election ->
            if (election.id == electionId) updatedElection else election
        }

        return "Check-in successful"
    }

    override fun validateVoting(
        electionId: String,
        voterId: String
    ): VoteValidationResult {
        val normalizedVoterId = voterId.trim()

        if (normalizedVoterId.isEmpty()) {
            return VoteValidationResult(false, "Voter ID required")
        }

        val election = getElectionById(electionId)
            ?: return VoteValidationResult(false, "Election not found")

        if (!election.eligibleVoterIds.contains(normalizedVoterId)) {
            return VoteValidationResult(false, "Not eligible")
        }

        if (!election.checkedInVoterIds.contains(normalizedVoterId)) {
            return VoteValidationResult(false, "Not checked-in")
        }

        if (!election.hasStarted()) {
            return VoteValidationResult(false, "Election not started")
        }

        if (election.isClosed()) {
            return VoteValidationResult(false, "Election closed")
        }

        if (election.votedVoterIds.contains(normalizedVoterId)) {
            return VoteValidationResult(false, "Already voted")
        }

        return VoteValidationResult(true, "Eligible to vote")
    }

    override fun vote(
        electionId: String,
        voterId: String,
        candidateName: String
    ): VoteValidationResult {
        val normalizedVoterId = voterId.trim()
        val normalizedCandidateName = candidateName.trim()

        val election = getElectionById(electionId)
            ?: return VoteValidationResult(false, "Election not found")

        val validation = validateVoting(electionId, normalizedVoterId)
        if (!validation.success) {
            return validation
        }

        if (!election.candidates.contains(normalizedCandidateName)) {
            return VoteValidationResult(false, "Invalid candidate")
        }

        val updatedVoteCounts = election.voteCounts.toMutableMap()
        val currentCount = updatedVoteCounts[normalizedCandidateName] ?: 0
        updatedVoteCounts[normalizedCandidateName] = currentCount + 1

        val updatedElection = election.copy(
            voteCounts = updatedVoteCounts,
            votedVoterIds = election.votedVoterIds + normalizedVoterId
        )

        _elections.value = _elections.value.map { current ->
            if (current.id == electionId) updatedElection else current
        }

        val receipt = VoteReceipt(
            electionId = election.id,
            electionTitle = election.title,
            voterId = normalizedVoterId,
            candidateName = normalizedCandidateName,
            timestamp = System.currentTimeMillis(),
            transactionHash = generateFakeTransactionHash()
        )

        _voteReceipts.value = _voteReceipts.value + receipt

        return VoteValidationResult(
            success = true,
            message = "Vote submitted successfully",
            receipt = receipt
        )
    }

    private fun generateFakeTransactionHash(): String {
        return "0x" + UUID.randomUUID().toString().replace("-", "").take(32)
    }
}