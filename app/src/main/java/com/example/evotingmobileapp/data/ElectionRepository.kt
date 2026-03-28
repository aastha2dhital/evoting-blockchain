package com.example.evotingmobileapp.data

import com.example.evotingmobileapp.model.Election
import kotlinx.coroutines.flow.StateFlow

interface ElectionRepository {
    val elections: StateFlow<List<Election>>

    fun addElection(
        title: String,
        candidates: List<String>,
        startTimeMillis: Long,
        endTimeMillis: Long
    )

    fun getElectionById(electionId: String): Election?

    fun getLatestElection(): Election?

    fun submitVote(
        electionId: String,
        candidateName: String,
        voterId: String
    ): Boolean

    fun hasVoted(
        electionId: String,
        voterId: String
    ): Boolean
}