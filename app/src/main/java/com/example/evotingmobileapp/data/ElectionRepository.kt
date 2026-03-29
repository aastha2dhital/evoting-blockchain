package com.example.evotingmobileapp.data

import com.example.evotingmobileapp.model.Election
import com.example.evotingmobileapp.model.VoteReceipt
import kotlinx.coroutines.flow.StateFlow

interface ElectionRepository {
    val elections: StateFlow<List<Election>>
    val voteReceipts: StateFlow<List<VoteReceipt>>

    fun createElection(
        title: String,
        candidates: List<String>,
        startTimeMillis: Long,
        endTimeMillis: Long,
        eligibleVoterIds: List<String> = emptyList()
    )

    fun getElectionById(electionId: String): Election?

    fun checkInVoter(electionId: String, voterId: String): String

    fun validateVoting(
        electionId: String,
        voterId: String
    ): VoteValidationResult

    fun vote(
        electionId: String,
        voterId: String,
        candidateName: String
    ): VoteValidationResult
}