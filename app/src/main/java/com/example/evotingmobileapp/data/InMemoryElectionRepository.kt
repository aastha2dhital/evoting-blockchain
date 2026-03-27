package com.example.evotingmobileapp.data

import com.example.evotingmobileapp.admin.LocalElectionDraft

class InMemoryElectionRepository : ElectionRepository {

    private val elections = mutableListOf<LocalElectionDraft>()

    override fun createElection(election: LocalElectionDraft) {
        elections.add(election)
    }

    override fun getAllElections(): List<LocalElectionDraft> {
        return elections.toList()
    }
}