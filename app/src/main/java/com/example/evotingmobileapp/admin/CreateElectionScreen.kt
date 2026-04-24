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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.evotingmobileapp.R
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
    var showSuccessDialog by rememberSaveable { mutableStateOf(false) }

    val titleRequiredError = stringResource(R.string.create_election_error_title_required)
    val candidatesRequiredError = stringResource(R.string.create_election_error_candidates_required)
    val eligibleVotersRequiredError = stringResource(R.string.create_election_error_eligible_voters_required)
    val invalidStartTimeError = stringResource(R.string.create_election_error_invalid_start_time)
    val invalidEndTimeError = stringResource(R.string.create_election_error_invalid_end_time)
    val endTimeBeforeStartError = stringResource(R.string.create_election_error_end_after_start)
    val genericCreateError = stringResource(R.string.create_election_error_generic)

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                navController.popBackStack()
            },
            title = {
                Text(
                    text = stringResource(R.string.create_election_success_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(text = stringResource(R.string.create_election_success_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        navController.popBackStack()
                    }
                ) {
                    Text(text = stringResource(R.string.create_election_success_button))
                }
            }
        )
    }

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
                text = stringResource(R.string.create_election_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.create_election_subtitle),
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
                        label = {
                            Text(text = stringResource(R.string.create_election_label_title))
                        },
                        placeholder = {
                            Text(text = stringResource(R.string.create_election_placeholder_title))
                        },
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
                        label = {
                            Text(text = stringResource(R.string.create_election_label_candidates))
                        },
                        placeholder = {
                            Text(text = stringResource(R.string.create_election_placeholder_candidates))
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
                        label = {
                            Text(text = stringResource(R.string.create_election_label_eligible_voters))
                        },
                        placeholder = {
                            Text(text = stringResource(R.string.create_election_placeholder_eligible_voters))
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
                        label = {
                            Text(text = stringResource(R.string.create_election_label_start_time))
                        },
                        placeholder = {
                            Text(text = stringResource(R.string.create_election_placeholder_datetime))
                        },
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
                        label = {
                            Text(text = stringResource(R.string.create_election_label_end_time))
                        },
                        placeholder = {
                            Text(text = stringResource(R.string.create_election_placeholder_datetime))
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isCreating
                    )

                    Text(
                        text = stringResource(R.string.create_election_helper_text),
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
                    Text(text = stringResource(R.string.create_election_cancel_button))
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
                                errorMessage = titleRequiredError
                            }

                            cleanedCandidates.size < 2 -> {
                                errorMessage = candidatesRequiredError
                            }

                            cleanedEligibleVoters.isEmpty() -> {
                                errorMessage = eligibleVotersRequiredError
                            }

                            startTimeMillis == null -> {
                                errorMessage = invalidStartTimeError
                            }

                            endTimeMillis == null -> {
                                errorMessage = invalidEndTimeError
                            }

                            endTimeMillis <= startTimeMillis -> {
                                errorMessage = endTimeBeforeStartError
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
                                        showSuccessDialog = true
                                    }.onFailure { exception ->
                                        errorMessage = exception.message ?: genericCreateError
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isCreating
                ) {
                    Text(
                        text = if (isCreating) {
                            stringResource(R.string.create_election_creating_button)
                        } else {
                            stringResource(R.string.create_election_create_button)
                        }
                    )
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