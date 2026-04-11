package com.example.evotingmobileapp.screens

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.evotingmobileapp.admin.AdminViewModel
import com.example.evotingmobileapp.navigation.AppRoutes
import kotlinx.coroutines.launch

@Composable
fun VotingScreen(
    navController: NavController,
    adminViewModel: AdminViewModel
) {
    val elections by adminViewModel.elections.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var walletAddress by rememberSaveable { mutableStateOf("") }
    var selectedElectionId by rememberSaveable { mutableStateOf("") }
    var selectedCandidate by rememberSaveable { mutableStateOf("") }

    val selectedElection = elections.find { it.id == selectedElectionId }

    fun getElectionStatusText(): String {
        val election = selectedElection ?: return "No election selected"
        return when {
            election.isClosed() -> "Closed"
            election.isActive() -> "Active"
            else -> "Not Started"
        }
    }

    fun getVotingAccessText(): String {
        val election = selectedElection ?: return "Please select an election"
        val trimmedWalletAddress = walletAddress.trim()

        if (trimmedWalletAddress.isBlank()) {
            return "Enter a wallet address"
        }

        val result = adminViewModel.validateVoting(
            electionId = election.id,
            voterId = trimmedWalletAddress
        )

        return if (result.success) {
            "Eligible and checked-in. Ready to vote."
        } else {
            result.message
        }
    }

    LaunchedEffect(selectedElectionId, elections) {
        if (selectedElection == null) {
            selectedCandidate = ""
        } else if (
            selectedCandidate.isNotBlank() &&
            !selectedElection.candidates.contains(selectedCandidate)
        ) {
            selectedCandidate = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        SnackbarHost(hostState = snackbarHostState)

        Text(
            text = "Vote Now",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    text = "Voter Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = walletAddress,
                    onValueChange = { walletAddress = it },
                    label = { Text("Wallet Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Available Elections",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (elections.isEmpty()) {
                    Text(
                        text = "No elections created yet.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    elections.forEach { election ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = election.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Election ID: ${election.id}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    RadioButton(
                                        selected = selectedElectionId == election.id,
                                        onClick = {
                                            selectedElectionId = election.id
                                            selectedCandidate = ""
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        selectedElection?.let { election ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text(
                        text = "Election Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Status: ${getElectionStatusText()}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Voting Access: ${getVotingAccessText()}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Candidates",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    election.candidates.forEach { candidate ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedCandidate == candidate,
                                    onClick = { selectedCandidate = candidate }
                                )

                                Text(
                                    text = candidate,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val trimmedWalletAddress = walletAddress.trim()

                            if (trimmedWalletAddress.isBlank()) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Enter wallet address")
                                }
                                return@Button
                            }

                            if (selectedCandidate.isBlank()) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Please select a candidate")
                                }
                                return@Button
                            }

                            val result = adminViewModel.submitVote(
                                electionId = election.id,
                                voterId = trimmedWalletAddress,
                                candidateName = selectedCandidate
                            )

                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(result.message)
                            }

                            if (result.success) {
                                navController.navigate(AppRoutes.RECEIPT)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Submit Vote")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { navController.popBackStack() }
        ) {
            Text("Back")
        }
    }
}