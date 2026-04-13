package com.example.evotingmobileapp.data

import com.example.evotingmobileapp.model.Election
import com.example.evotingmobileapp.model.VoteReceipt
import java.util.Locale
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
        val generatedElectionId = nextGeneratedElectionId()

        createElectionWithId(
            electionId = generatedElectionId,
            title = title,
            candidates = candidates,
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis,
            eligibleVoterIds = eligibleVoterIds
        )
    }

    fun createElectionWithId(
        electionId: String,
        title: String,
        candidates: List<String>,
        startTimeMillis: Long,
        endTimeMillis: Long,
        eligibleVoterIds: List<String>
    ) {
        val cleanedElectionId = electionId.trim()

        require(cleanedElectionId.isNotEmpty()) {
            "Election ID cannot be blank."
        }

        val cleanedTitle = title.trim()

        val cleanedCandidates = candidates
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()

        val cleanedEligibleVoterIds = eligibleVoterIds
            .map { normalizeVoterId(it) }
            .filter { it.isNotEmpty() }
            .toSet()
            .ifEmpty {
                defaultEligibleVoterIds
                    .map { normalizeVoterId(it) }
                    .toSet()
            }

        val voteCounts = cleanedCandidates.associateWith { 0 }

        val newElection = Election(
            id = cleanedElectionId,
            title = cleanedTitle,
            candidates = cleanedCandidates,
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis,
            voteCounts = voteCounts,
            votedVoterIds = emptySet(),
            eligibleVoterIds = cleanedEligibleVoterIds,
            checkedInVoterIds = emptySet()
        )

        _elections.value = (_elections.value.filterNot { it.id == cleanedElectionId } + newElection)
            .sortedBy { it.id.toIntOrNull() ?: Int.MAX_VALUE }
    }

    override fun getElectionById(electionId: String): Election? {
        return _elections.value.find { it.id == electionId }
    }

    override fun checkInVoter(electionId: String, voterId: String): String {
        val normalizedVoterId = normalizeVoterId(voterId)

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
        val normalizedVoterId = normalizeVoterId(voterId)

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
        val normalizedVoterId = normalizeVoterId(voterId)
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

        _voteReceipts.value += receipt

        return VoteValidationResult(
            success = true,
            message = "Vote submitted successfully",
            receipt = receipt
        )
    }

    override fun closeElectionEarly(electionId: String): Result<String> {
        val election = getElectionById(electionId)
            ?: return Result.failure(IllegalStateException("Election not found"))

        if (election.isClosed()) {
            return Result.failure(IllegalStateException("Election is already closed"))
        }

        val updatedElection = election.copy(
            isManuallyClosed = true,
            endTimeMillis = System.currentTimeMillis()
        )

        _elections.value = _elections.value.map { current ->
            if (current.id == electionId) updatedElection else current
        }

        return Result.success("Election closed successfully.")
    }

    private fun nextGeneratedElectionId(): String {
        val highestExistingId = _elections.value
            .mapNotNull { it.id.toIntOrNull() }
            .maxOrNull() ?: 0

        return (highestExistingId + 1).toString()
    }

    private fun generateFakeTransactionHash(): String {
        return "0x" + UUID.randomUUID().toString().replace("-", "").take(32)
    }

    private fun normalizeVoterId(voterId: String): String {
        return voterId.trim().lowercase(Locale.ROOT)
    }
}