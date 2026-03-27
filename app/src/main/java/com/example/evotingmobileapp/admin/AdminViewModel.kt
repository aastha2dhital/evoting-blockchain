package com.example.evotingmobileapp.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.evotingmobileapp.data.InMemoryElectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AdminViewModel : ViewModel() {

    private val repository = InMemoryElectionRepository()

    private val _uiState = MutableStateFlow(CreateElectionUiState())
    val uiState: StateFlow<CreateElectionUiState> = _uiState.asStateFlow()

    fun updateElectionTitle(title: String) {
        _uiState.update {
            it.copy(
                electionTitle = title,
                errorMessage = null,
                successMessage = null,
                isElectionCreated = false
            )
        }
    }

    fun updateCandidateName(index: Int, name: String) {
        val currentCandidates = _uiState.value.candidates.toMutableList()

        if (index !in currentCandidates.indices) return

        currentCandidates[index] = name

        _uiState.update {
            it.copy(
                candidates = currentCandidates,
                errorMessage = null,
                successMessage = null,
                isElectionCreated = false
            )
        }
    }

    fun addCandidate() {
        val updatedCandidates = _uiState.value.candidates.toMutableList()
        updatedCandidates.add("")

        _uiState.update {
            it.copy(
                candidates = updatedCandidates,
                errorMessage = null,
                successMessage = null,
                isElectionCreated = false
            )
        }
    }

    fun removeCandidate(index: Int) {
        val currentCandidates = _uiState.value.candidates

        if (index !in currentCandidates.indices) return

        if (currentCandidates.size <= 2) {
            _uiState.update {
                it.copy(
                    errorMessage = "At least 2 candidates are required.",
                    successMessage = null,
                    isElectionCreated = false
                )
            }
            return
        }

        val updatedCandidates = currentCandidates.toMutableList()
        updatedCandidates.removeAt(index)

        _uiState.update {
            it.copy(
                candidates = updatedCandidates,
                errorMessage = null,
                successMessage = null,
                isElectionCreated = false
            )
        }
    }

    fun createElection() {
        val title = _uiState.value.electionTitle.trim()
        val cleanedCandidates = _uiState.value.candidates
            .map { it.trim() }
            .filter { it.isNotBlank() }

        when {
            title.isBlank() -> {
                _uiState.update {
                    it.copy(
                        errorMessage = "Election title cannot be empty.",
                        successMessage = null,
                        isElectionCreated = false,
                        isLoading = false
                    )
                }
                return
            }

            cleanedCandidates.size < 2 -> {
                _uiState.update {
                    it.copy(
                        errorMessage = "Please enter at least 2 valid candidates.",
                        successMessage = null,
                        isElectionCreated = false,
                        isLoading = false
                    )
                }
                return
            }

            cleanedCandidates.distinct().size != cleanedCandidates.size -> {
                _uiState.update {
                    it.copy(
                        errorMessage = "Candidate names must be unique.",
                        successMessage = null,
                        isElectionCreated = false,
                        isLoading = false
                    )
                }
                return
            }
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null,
                isElectionCreated = false
            )
        }

        val newElection = LocalElectionDraft(
            id = repository.getAllElections().size + 1,
            title = title,
            candidates = cleanedCandidates
        )

        repository.createElection(newElection)

        Log.d("AdminViewModel", "Election saved in repository: $newElection")

        _uiState.update {
            it.copy(
                electionTitle = "",
                candidates = listOf("", ""),
                isLoading = false,
                isElectionCreated = true,
                successMessage = "Election created successfully.",
                errorMessage = null,
                createdElectionCount = repository.getAllElections().size
            )
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun resetCreatedState() {
        _uiState.update {
            it.copy(
                isElectionCreated = false
            )
        }
    }

    fun getCreatedElections(): List<LocalElectionDraft> {
        return repository.getAllElections()
    }
}

data class LocalElectionDraft(
    val id: Int,
    val title: String,
    val candidates: List<String>
)