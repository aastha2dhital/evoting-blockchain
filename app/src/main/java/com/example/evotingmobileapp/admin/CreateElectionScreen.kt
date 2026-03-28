package com.example.evotingmobileapp.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CreateElectionScreen(
    navController: NavHostController? = null,
    adminViewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var startDateTime by remember { mutableStateOf("") }
    var endDateTime by remember { mutableStateOf("") }
    var candidates by remember { mutableStateOf(listOf("", "")) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    val formatter = remember {
        SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).apply {
            isLenient = false
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.padding(top = 8.dp))

            Text(
                text = "Create Election",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Set the election details and schedule voting using 12-hour time with AM/PM.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors()
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
                            successMessage = ""
                        },
                        label = { Text("Election Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = startDateTime,
                        onValueChange = {
                            startDateTime = it
                            errorMessage = ""
                            successMessage = ""
                        },
                        label = { Text("Start (yyyy-MM-dd hh:mm AM/PM)") },
                        placeholder = { Text("2026-03-27 06:15 PM") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = endDateTime,
                        onValueChange = {
                            endDateTime = it
                            errorMessage = ""
                            successMessage = ""
                        },
                        label = { Text("End (yyyy-MM-dd hh:mm AM/PM)") },
                        placeholder = { Text("2026-03-27 08:30 PM") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Candidates",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Add at least 2 candidates.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    candidates.forEachIndexed { index, candidate ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = candidate,
                                onValueChange = { newValue ->
                                    candidates = candidates.toMutableList().also {
                                        it[index] = newValue
                                    }
                                    errorMessage = ""
                                    successMessage = ""
                                },
                                label = { Text("Candidate ${index + 1}") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            if (candidates.size > 2) {
                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = {
                                        candidates = candidates.toMutableList().also {
                                            it.removeAt(index)
                                        }
                                        errorMessage = ""
                                        successMessage = ""
                                    },
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text("Remove")
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            candidates = candidates + ""
                            errorMessage = ""
                            successMessage = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Candidate")
                    }
                }
            }
        }

        if (errorMessage.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors()
                ) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        if (successMessage.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors()
                ) {
                    Text(
                        text = successMessage,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    val cleanedTitle = title.trim()
                    val cleanedCandidates = candidates
                        .map { it.trim() }
                        .filter { it.isNotBlank() }

                    val parsedStart = try {
                        formatter.parse(startDateTime.trim())?.time
                    } catch (_: Exception) {
                        null
                    }

                    val parsedEnd = try {
                        formatter.parse(endDateTime.trim())?.time
                    } catch (_: Exception) {
                        null
                    }

                    when {
                        cleanedTitle.isBlank() -> {
                            errorMessage = "Please enter an election title."
                            successMessage = ""
                        }

                        cleanedCandidates.size < 2 -> {
                            errorMessage = "Please enter at least 2 candidates."
                            successMessage = ""
                        }

                        parsedStart == null -> {
                            errorMessage = "Please enter a valid start date and time in this format: yyyy-MM-dd hh:mm AM/PM"
                            successMessage = ""
                        }

                        parsedEnd == null -> {
                            errorMessage = "Please enter a valid end date and time in this format: yyyy-MM-dd hh:mm AM/PM"
                            successMessage = ""
                        }

                        parsedEnd <= parsedStart -> {
                            errorMessage = "End time must be after start time."
                            successMessage = ""
                        }

                        else -> {
                            adminViewModel.createElection(
                                title = cleanedTitle,
                                candidates = cleanedCandidates,
                                startTimeMillis = parsedStart,
                                endTimeMillis = parsedEnd
                            )

                            successMessage = "Election created successfully."
                            errorMessage = ""

                            title = ""
                            startDateTime = ""
                            endDateTime = ""
                            candidates = listOf("", "")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Election")
            }
        }

        item {
            Spacer(modifier = Modifier.padding(bottom = 16.dp))
        }
    }
}