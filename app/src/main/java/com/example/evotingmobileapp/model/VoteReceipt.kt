package com.example.evotingmobileapp.model

data class VoteReceipt(
    val electionId: Int,
    val candidateId: Int,
    val voterWalletAddress: String,
    val transactionHash: String,
    val timestamp: Long
)