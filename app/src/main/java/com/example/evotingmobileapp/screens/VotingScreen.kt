package com.example.evotingmobileapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.navigation.NavHostController
import com.example.evotingmobileapp.admin.AdminViewModel
import com.example.evotingmobileapp.data.VoteValidationResult
import com.example.evotingmobileapp.navigation.AppRoutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun VotingScreen(
    navController: NavHostController,
    adminViewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val elections by adminViewModel.elections.collectAsState()
    val connectedWalletAddress by adminViewModel.connectedWalletAddress.collectAsState()
    val walletConnected by adminViewModel.walletConnected.collectAsState()

    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var voterWalletAddress by rememberSaveable { mutableStateOf("") }
    var selectedElectionId by rememberSaveable { mutableStateOf("") }
    var selectedCandidate by rememberSaveable { mutableStateOf("") }
    var isCheckingIn by rememberSaveable { mutableStateOf(false) }
    var isSubmittingVote by rememberSaveable { mutableStateOf(false) }

    val isBusy = isCheckingIn || isSubmittingVote
    val selectedElection = elections.find { it.id == selectedElectionId }

    LaunchedEffect(walletConnected, connectedWalletAddress) {
        if (walletConnected && connectedWalletAddress.isNotBlank()) {
            voterWalletAddress = connectedWalletAddress
        }
    }

    LaunchedEffect(elections, selectedElectionId) {
        if (selectedElectionId.isNotBlank() && selectedElection == null) {
            selectedElectionId = ""
        }

        if (selectedElection == null) {
            selectedCandidate = ""
        } else if (
            selectedCandidate.isNotBlank() &&
            !selectedElection.candidates.contains(selectedCandidate)
        ) {
            selectedCandidate = ""
        }
    }

    fun getElectionStatusText(): String {
        val election = selectedElection ?: return "No election selected"
        return when {
            election.isClosed() -> "Closed"
            election.isActive() -> "Active"
            else -> "Not Started"
        }
    }

    fun getVotingAccessText(): String {
        val election = selectedElection ?: return "Please select an election first."
        val trimmedWalletAddress = voterWalletAddress.trim()

        if (trimmedWalletAddress.isBlank()) {
            return if (walletConnected) {
                "Wallet connection exists, but the wallet address field is empty."
            } else {
                "Connect a wallet on the login screen or enter a wallet address for prototype testing."
            }
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SnackbarHost(hostState = snackBarHostState)

        Text(
            text = "Vote Now",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Use the connected wallet address for prototype voting. You can still edit it manually when needed for testing.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Wallet Identity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = if (walletConnected && connectedWalletAddress.isNotBlank()) {
                        "Connected wallet detected. It will be used as the voter identity for check-in and voting."
                    } else {
                        "No shared wallet is connected right now. You can still type a wallet address for prototype testing."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = voterWalletAddress,
                    onValueChange = { voterWalletAddress = it },
                    label = { Text("Wallet Address") },
                    placeholder = { Text("0x...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isBusy
                )

                if (walletConnected && connectedWalletAddress.isNotBlank()) {
                    Text(
                        text = "Connected wallet: ${shortenWalletAddress(connectedWalletAddress)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (voterWalletAddress != connectedWalletAddress) {
                        TextButton(
                            onClick = { voterWalletAddress = connectedWalletAddress },
                            enabled = !isBusy
                        ) {
                            Text("Use Connected Wallet")
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select Election",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                if (elections.isEmpty()) {
                    Text(
                        text = "No elections created yet. Please create an election first.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    elections.forEach { election ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedElectionId == election.id,
                                    onClick = {
                                        if (!isBusy) {
                                            selectedElectionId = election.id
                                            selectedCandidate = ""
                                        }
                                    },
                                    enabled = !isBusy
                                )

                                Column(
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = election.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Election ID: ${election.id}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        selectedElection?.let { election ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Election Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Status: ${getElectionStatusText()}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        text = "Voting Access: ${getVotingAccessText()}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Candidates",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    election.candidates.forEach { candidate ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
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
                                    onClick = {
                                        if (!isBusy) {
                                            selectedCandidate = candidate
                                        }
                                    },
                                    enabled = !isBusy
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = candidate,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = {
                            val trimmedWalletAddress = voterWalletAddress.trim()

                            if (trimmedWalletAddress.isBlank()) {
                                coroutineScope.launch {
                                    snackBarHostState.showSnackbar("Enter wallet address first")
                                }
                                return@OutlinedButton
                            }

                            adminViewModel.setConnectedWalletAddress(trimmedWalletAddress)
                            isCheckingIn = true

                            coroutineScope.launch {
                                val result = runCatching {
                                    withContext(Dispatchers.IO) {
                                        adminViewModel.checkInVoter(
                                            electionId = election.id,
                                            voterId = trimmedWalletAddress
                                        )
                                    }
                                }

                                isCheckingIn = false

                                val message = result.getOrElse { exception ->
                                    exception.message ?: "Blockchain check-in failed."
                                }

                                snackBarHostState.showSnackbar(message)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isBusy
                    ) {
                        Text(if (isCheckingIn) "Checking In..." else "Check In With This Wallet")
                    }

                    Button(
                        onClick = {
                            val trimmedWalletAddress = voterWalletAddress.trim()

                            if (trimmedWalletAddress.isBlank()) {
                                coroutineScope.launch {
                                    snackBarHostState.showSnackbar("Enter wallet address first")
                                }
                                return@Button
                            }

                            if (selectedCandidate.isBlank()) {
                                coroutineScope.launch {
                                    snackBarHostState.showSnackbar("Please select a candidate")
                                }
                                return@Button
                            }

                            adminViewModel.setConnectedWalletAddress(trimmedWalletAddress)
                            isSubmittingVote = true

                            coroutineScope.launch {
                                val result = runCatching {
                                    withContext(Dispatchers.IO) {
                                        adminViewModel.submitVote(
                                            electionId = election.id,
                                            voterId = trimmedWalletAddress,
                                            candidateName = selectedCandidate
                                        )
                                    }
                                }

                                isSubmittingVote = false

                                val finalResult = result.getOrElse { exception ->
                                    VoteValidationResult(
                                        success = false,
                                        message = exception.message ?: "Blockchain vote failed."
                                    )
                                }

                                snackBarHostState.showSnackbar(finalResult.message)

                                if (finalResult.success) {
                                    val transactionHash = finalResult.receipt?.transactionHash.orEmpty()

                                    if (transactionHash.isNotBlank()) {
                                        navController.navigate(
                                            AppRoutes.receiptRoute(transactionHash)
                                        )
                                    } else {
                                        navController.navigate(AppRoutes.RECEIPT)
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isBusy
                    ) {
                        Text(if (isSubmittingVote) "Submitting Vote..." else "Submit Vote")
                    }
                }
            }
        }

        TextButton(
            onClick = { navController.popBackStack() },
            enabled = !isBusy
        ) {
            Text("Back")
        }
    }
}

private fun shortenWalletAddress(address: String): String {
    if (address.length <= 16) return address
    return "${address.take(10)}...${address.takeLast(8)}"
}