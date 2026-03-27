package com.example.evotingmobileapp.model

data class Candidate(
    val id: Int,
    val name: String,
    val party: String,
    val voteCount: Int = 0
)