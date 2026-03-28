package com.example.evotingmobileapp.data

import com.example.evotingmobileapp.model.Election
import com.example.evotingmobileapp.model.VoteReceipt
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class VoteValidationResult(
    val success: Boolean,
    val message: String,
    val receipt: VoteReceipt? = null
)

class InMemoryElectionRepository {

    private val _elections = MutableStateFlow<List<Election>>(emptyList())
    val elections: StateFlow<List<Election>> = _elections.asStateFlow()

    private val defaultEligibleVoterIds = setOf("voter001")

    fun createElection(
        title: String,
        candidates: List<String>,
        startTimeMillis: Long,
        endTimeMillis: Long
    ) {
        val electionId = (_elections.value.size + 1).toString()

        val cleanedCandidates = candidates
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val voteCounts = cleanedCandidates.associateWith { 0 }

        val newElection = Election(
            id = electionId,
            title = title.trim(),
            candidates = cleanedCandidates,
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis,
            voteCounts = voteCounts,
            votedVoterIds = emptySet(),
            eligibleVoterIds = defaultEligibleVoterIds,
            checkedInVoterIds = emptySet()
        )

        _elections.value = _elections.value + newElection
    }

    fun getElectionById(electionId: String): Election? {
        return _elections.value.find { it.id == electionId }
    }

    fun checkInVoter(electionId: String, voterId: String): String {
        val currentElection = getElectionById(electionId)
            ?: return "Election not found"

        if (!currentElection.eligibleVoterIds.contains(voterId)) {
            return "Not eligible"
        }

        if (currentElection.checkedInVoterIds.contains(voterId)) {
            return "Already checked-in"
        }

        val updatedElection = currentElection.copy(
            checkedInVoterIds = currentElection.checkedInVoterIds + voterId
        )

        _elections.value = _elections.value.map { election ->
            if (election.id == electionId) updatedElection else election
        }

        return "Check-in successful"
    }

    fun validateVoting(
        electionId: String,
        voterId: String
    ): VoteValidationResult {
        val election = getElectionById(electionId)
            ?: return VoteValidationResult(false, "Election not found")

        if (!election.eligibleVoterIds.contains(voterId)) {
            return VoteValidationResult(false, "Not eligible")
        }

        if (!election.checkedInVoterIds.contains(voterId)) {
            return VoteValidationResult(false, "Not checked-in")
        }

        if (!election.hasStarted()) {
            return VoteValidationResult(false, "Election not started")
        }

        if (election.isClosed()) {
            return VoteValidationResult(false, "Election closed")
        }

        if (election.votedVoterIds.contains(voterId)) {
            return VoteValidationResult(false, "Already voted")
        }

        return VoteValidationResult(true, "Eligible to vote")
    }

    fun vote(
        electionId: String,
        voterId: String,
        candidateName: String
    ): VoteValidationResult {
        val election = getElectionById(electionId)
            ?: return VoteValidationResult(false, "Election not found")

        val validation = validateVoting(electionId, voterId)
        if (!validation.success) {
            return validation
        }

        if (!election.candidates.contains(candidateName)) {
            return VoteValidationResult(false, "Invalid candidate")
        }

        val updatedVoteCounts = election.voteCounts.toMutableMap()
        val currentCount = updatedVoteCounts[candidateName] ?: 0
        updatedVoteCounts[candidateName] = currentCount + 1

        val updatedElection = election.copy(
            voteCounts = updatedVoteCounts,
            votedVoterIds = election.votedVoterIds + voterId
        )

        _elections.value = _elections.value.map { current ->
            if (current.id == electionId) updatedElection else current
        }

        val receipt = VoteReceipt(
            electionId = election.id,
            electionTitle = election.title,
            voterId = voterId,
            candidateName = candidateName,
            timestamp = System.currentTimeMillis(),
            transactionHash = generateFakeTransactionHash()
        )

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