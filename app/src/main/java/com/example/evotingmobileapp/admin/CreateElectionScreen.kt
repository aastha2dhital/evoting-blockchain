package com.example.evotingmobileapp.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CreateElectionScreen(
    adminViewModel: AdminViewModel = viewModel()
) {
    val uiState by adminViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isElectionCreated) {
        if (uiState.isElectionCreated) {
            adminViewModel.resetCreatedState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Create Election",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = uiState.electionTitle,
            onValueChange = { newTitle ->
                adminViewModel.updateElectionTitle(newTitle)
            },
            label = {
                Text("Election Title")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Candidates",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        uiState.candidates.forEachIndexed { index, candidateName ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    OutlinedTextField(
                        value = candidateName,
                        onValueChange = { updatedName ->
                            adminViewModel.updateCandidateName(index, updatedName)
                        },
                        label = {
                            Text("Candidate ${index + 1}")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (uiState.candidates.size > 2) {
                        TextButton(
                            onClick = {
                                adminViewModel.removeCandidate(index)
                            }
                        ) {
                            Text("Remove Candidate")
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = {
                adminViewModel.addCandidate()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Candidate")
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (uiState.successMessage != null) {
            Text(
                text = uiState.successMessage ?: "",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = {
                adminViewModel.createElection()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (uiState.isLoading) "Creating..." else "Create Election"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "Locally created elections: ${uiState.createdElectionCount}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}