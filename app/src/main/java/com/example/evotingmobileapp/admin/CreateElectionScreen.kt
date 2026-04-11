package com.example.evotingmobileapp.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CreateElectionScreen(
    navController: NavHostController,
    adminViewModel: AdminViewModel
) {
    val now = remember { System.currentTimeMillis() }
    val coroutineScope = rememberCoroutineScope()

    var title by rememberSaveable { mutableStateOf("") }
    var candidatesInput by rememberSaveable { mutableStateOf("") }
    var eligibleVoterIdsInput by rememberSaveable { mutableStateOf("") }
    var startDateTimeInput by rememberSaveable { mutableStateOf(formatDateTime(now + 5 * 60 * 1000)) }
    var endDateTimeInput by rememberSaveable { mutableStateOf(formatDateTime(now + 60 * 60 * 1000)) }
    var errorMessage by rememberSaveable { mutableStateOf("") }
    var isCreating by rememberSaveable { mutableStateOf(false) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Create Election",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Set up the election details, candidate list, schedule, and eligible voter whitelist.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            errorMessage = ""
                        },
                        label = { Text("Election title") },
                        placeholder = { Text("Student Council Election 2026") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isCreating
                    )

                    OutlinedTextField(
                        value = candidatesInput,
                        onValueChange = {
                            candidatesInput = it
                            errorMessage = ""
                        },
                        label = { Text("Candidates") },
                        placeholder = {
                            Text(
                                "Enter candidate names separated by commas or new lines\nExample:\nAlice\nBob\nCharlie"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        enabled = !isCreating
                    )

                    OutlinedTextField(
                        value = eligibleVoterIdsInput,
                        onValueChange = {
                            eligibleVoterIdsInput = it
                            errorMessage = ""
                        },
                        label = { Text("Eligible voter wallet addresses / whitelist") },
                        placeholder = {
                            Text(
                                "Enter wallet addresses separated by commas or new lines\nExample:\n0x70997970c51812dc3a010c7d01b50e0d17dc79c8\n0x3c44cdddb6a900fa2b585dd299e03d12fa4293bc"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        enabled = !isCreating
                    )

                    OutlinedTextField(
                        value = startDateTimeInput,
                        onValueChange = {
                            startDateTimeInput = it
                            errorMessage = ""
                        },
                        label = { Text("Start date & time") },
                        placeholder = { Text("dd/MM/yyyy hh:mm a") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isCreating
                    )

                    OutlinedTextField(
                        value = endDateTimeInput,
                        onValueChange = {
                            endDateTimeInput = it
                            errorMessage = ""
                        },
                        label = { Text("End date & time") },
                        placeholder = { Text("dd/MM/yyyy hh:mm a") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isCreating
                    )

                    Text(
                        text = "Use Ethereum wallet addresses for the whitelist.\nUse this date/time format: dd/MM/yyyy hh:mm a\nExample: 29/03/2026 08:30 PM",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (errorMessage.isNotBlank()) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f),
                    enabled = !isCreating
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        val cleanedTitle = title.trim()
                        val cleanedCandidates = parseMultiValueInput(candidatesInput)
                        val cleanedEligibleVoters = parseMultiValueInput(eligibleVoterIdsInput)
                        val startTimeMillis = parseDateTimeToMillis(startDateTimeInput)
                        val endTimeMillis = parseDateTimeToMillis(endDateTimeInput)

                        when {
                            cleanedTitle.isBlank() -> {
                                errorMessage = "Please enter an election title."
                            }

                            cleanedCandidates.size < 2 -> {
                                errorMessage = "Please enter at least 2 candidates."
                            }

                            cleanedEligibleVoters.isEmpty() -> {
                                errorMessage = "Please enter at least 1 eligible voter wallet address."
                            }

                            startTimeMillis == null -> {
                                errorMessage = "Invalid start date/time. Use format: dd/MM/yyyy hh:mm a"
                            }

                            endTimeMillis == null -> {
                                errorMessage = "Invalid end date/time. Use format: dd/MM/yyyy hh:mm a"
                            }

                            endTimeMillis <= startTimeMillis -> {
                                errorMessage = "End time must be later than start time."
                            }

                            else -> {
                                errorMessage = ""
                                isCreating = true

                                coroutineScope.launch {
                                    val result = runCatching {
                                        withContext(Dispatchers.IO) {
                                            adminViewModel.createElection(
                                                title = cleanedTitle,
                                                candidates = cleanedCandidates,
                                                startTimeMillis = startTimeMillis,
                                                endTimeMillis = endTimeMillis,
                                                eligibleVoterIdsInput = eligibleVoterIdsInput
                                            )
                                        }
                                    }

                                    isCreating = false

                                    result.onSuccess {
                                        navController.popBackStack()
                                    }.onFailure { exception ->
                                        errorMessage = exception.message
                                            ?: "Failed to create election on blockchain."
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isCreating
                ) {
                    Text(if (isCreating) "Creating..." else "Create")
                }
            }
        }
    }
}

private fun parseMultiValueInput(input: String): List<String> {
    return input
        .split(",", "\n", ";")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
}

private fun parseDateTimeToMillis(input: String): Long? {
    return try {
        val formatter = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
        formatter.isLenient = false
        formatter.parse(input.trim())?.time
    } catch (_: Exception) {
        null
    }
}

private fun formatDateTime(timeInMillis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
    return formatter.format(Date(timeInMillis))
}