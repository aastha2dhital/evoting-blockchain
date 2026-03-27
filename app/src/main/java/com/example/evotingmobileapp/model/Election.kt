package com.example.evotingmobileapp.model

data class Election(
    val id: Int,
    val title: String,
    val candidates: List<Candidate>,
    val startTime: Long,
    val endTime: Long,
    val isActive: Boolean
)