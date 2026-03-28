package com.example.evotingmobileapp.model

data class VoteReceipt(
    val electionId: String,
    val electionTitle: String,
    val candidateName: String,
    val voterId: String,
    val transactionHash: String,
    val timestamp: Long
)