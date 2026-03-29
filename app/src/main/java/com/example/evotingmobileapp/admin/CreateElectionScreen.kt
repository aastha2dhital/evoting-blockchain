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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CreateElectionScreen(
    navController: NavHostController,
    adminViewModel: AdminViewModel
) {
    val now = remember { System.currentTimeMillis() }

    var title by rememberSaveable { mutableStateOf("") }
    var candidatesInput by rememberSaveable { mutableStateOf("") }
    var eligibleVoterIdsInput by rememberSaveable { mutableStateOf("voter001") }
    var startDateTimeInput by rememberSaveable { mutableStateOf(formatDateTime(now + 5 * 60 * 1000)) }
    var endDateTimeInput by rememberSaveable { mutableStateOf(formatDateTime(now + 60 * 60 * 1000)) }
    var errorMessage by rememberSaveable { mutableStateOf("") }

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
                        modifier = Modifier.fillMaxWidth()
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
                        minLines = 4
                    )

                    OutlinedTextField(
                        value = eligibleVoterIdsInput,
                        onValueChange = {
                            eligibleVoterIdsInput = it
                            errorMessage = ""
                        },
                        label = { Text("Eligible voter IDs / whitelist") },
                        placeholder = {
                            Text(
                                "Enter voter IDs separated by commas or new lines\nExample:\nvoter001\nvoter002\nvoter003"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4
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
                        modifier = Modifier.fillMaxWidth()
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
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Use this format for date and time: dd/MM/yyyy hh:mm a\nExample: 29/03/2026 08:30 PM",
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
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        val cleanedTitle = title.trim()
                        val cleanedCandidates = parseMultiValueInput(candidatesInput)
                        val startTimeMillis = parseDateTimeToMillis(startDateTimeInput)
                        val endTimeMillis = parseDateTimeToMillis(endDateTimeInput)

                        when {
                            cleanedTitle.isBlank() -> {
                                errorMessage = "Please enter an election title."
                            }

                            cleanedCandidates.size < 2 -> {
                                errorMessage = "Please enter at least 2 candidates."
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
                                adminViewModel.createElection(
                                    title = cleanedTitle,
                                    candidates = cleanedCandidates,
                                    startTimeMillis = startTimeMillis,
                                    endTimeMillis = endTimeMillis,
                                    eligibleVoterIdsInput = eligibleVoterIdsInput
                                )
                                navController.popBackStack()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Create")
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