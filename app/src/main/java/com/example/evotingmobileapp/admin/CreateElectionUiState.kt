package com.example.evotingmobileapp.admin

data class CreateElectionUiState(
    val electionTitle: String = "",
    val candidates: List<String> = listOf("", ""),
    val isLoading: Boolean = false,
    val isElectionCreated: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val createdElectionCount: Int = 0
)