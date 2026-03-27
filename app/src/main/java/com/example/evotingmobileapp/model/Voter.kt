package com.example.evotingmobileapp.model

data class Voter(
    val walletAddress: String,
    val hasCheckedIn: Boolean = false,
    val hasVoted: Boolean = false,
    val checkedInElectionId: Int? = null
)