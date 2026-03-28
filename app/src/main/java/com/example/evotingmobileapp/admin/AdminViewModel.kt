package com.example.evotingmobileapp.admin

import androidx.lifecycle.ViewModel
import com.example.evotingmobileapp.data.InMemoryElectionRepository
import com.example.evotingmobileapp.data.VoteValidationResult
import com.example.evotingmobileapp.model.Election
import com.example.evotingmobileapp.model.VoteReceipt
import kotlinx.coroutines.flow.StateFlow

class AdminViewModel : ViewModel() {

    private val repository = InMemoryElectionRepository()

    val elections: StateFlow<List<Election>> = repository.elections

    private var _latestReceipt: VoteReceipt? = null

    // ✅ ONLY THIS (no duplicate function)
    val latestReceipt: VoteReceipt?
        get() = _latestReceipt

    fun createElection(
        title: String,
        candidates: List<String>,
        startTimeMillis: Long,
        endTimeMillis: Long
    ) {
        repository.createElection(
            title = title,
            candidates = candidates,
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis
        )
    }

    fun getElectionById(electionId: String): Election? {
        return repository.getElectionById(electionId)
    }

    fun checkInVoter(electionId: String, voterId: String): String {
        return repository.checkInVoter(electionId, voterId)
    }

    fun validateVoting(electionId: String, voterId: String): VoteValidationResult {
        return repository.validateVoting(electionId, voterId)
    }

    fun submitVote(
        electionId: String,
        voterId: String,
        candidateName: String
    ): VoteValidationResult {
        val result = repository.vote(
            electionId = electionId,
            voterId = voterId,
            candidateName = candidateName
        )

        if (result.success && result.receipt != null) {
            _latestReceipt = result.receipt
        }

        return result
    }
}