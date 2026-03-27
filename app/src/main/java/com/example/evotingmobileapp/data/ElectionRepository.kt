package com.example.evotingmobileapp.data

import com.example.evotingmobileapp.admin.LocalElectionDraft

interface ElectionRepository {

    fun createElection(election: LocalElectionDraft)

    fun getAllElections(): List<LocalElectionDraft>
}