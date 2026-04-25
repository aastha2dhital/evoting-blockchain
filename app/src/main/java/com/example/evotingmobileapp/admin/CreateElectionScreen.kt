package com.example.evotingmobileapp.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.evotingmobileapp.BuildConfig
import com.example.evotingmobileapp.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    var startDateTimeInput by rememberSaveable { mutableStateOf(formatDateTime(now - 60 * 1000)) }
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

    val candidateCount = parseMultiValueInput(candidatesInput).size
    val voterCount = parseMultiValueInput(eligibleVoterIdsInput).size

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                navController.popBackStack()
            },
            title = {
                Text(
                    text = stringResource(R.string.create_election_success_title),
                    fontWeight = FontWeight.ExtraBold
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.13f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.07f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                HeroCard(
                    candidateCount = candidateCount,
                    voterCount = voterCount
                )

                FancySectionCard(
                    step = "01",
                    title = stringResource(R.string.create_election_section_identity_title),
                    subtitle = stringResource(R.string.create_election_section_identity_subtitle)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            errorMessage = ""
                        },
                        label = { Text(text = stringResource(R.string.create_election_label_title)) },
                        placeholder = {
                            Text(text = stringResource(R.string.create_election_placeholder_title_example))
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isCreating,
                        shape = RoundedCornerShape(18.dp)
                    )
                }

                FancySectionCard(
                    step = "02",
                    title = stringResource(R.string.create_election_section_candidates_title),
                    subtitle = stringResource(R.string.create_election_section_candidates_subtitle)
                ) {
                    OutlinedTextField(
                        value = candidatesInput,
                        onValueChange = {
                            candidatesInput = it
                            errorMessage = ""
                        },
                        label = { Text(text = stringResource(R.string.create_election_label_candidates)) },
                        placeholder = {
                            Text(text = stringResource(R.string.create_election_placeholder_candidates_simple))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        enabled = !isCreating,
                        shape = RoundedCornerShape(18.dp)
                    )

                    CountBadge(text = "$candidateCount added")
                }

                FancySectionCard(
                    step = "03",
                    title = stringResource(R.string.create_election_section_whitelist_title),
                    subtitle = stringResource(R.string.create_election_section_whitelist_subtitle)
                ) {
                    OutlinedTextField(
                        value = eligibleVoterIdsInput,
                        onValueChange = {
                            eligibleVoterIdsInput = it
                            errorMessage = ""
                        },
                        label = { Text(text = stringResource(R.string.create_election_label_eligible_voters)) },
                        placeholder = {
                            Text(text = stringResource(R.string.create_election_placeholder_registered_voter))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        enabled = !isCreating,
                        shape = RoundedCornerShape(18.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                eligibleVoterIdsInput = appendUniqueValue(
                                    input = eligibleVoterIdsInput,
                                    value = BuildConfig.DEMO_VOTER_WALLET_ADDRESS
                                )
                                errorMessage = ""
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isCreating,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.create_election_use_registered_voter),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        CountBadge(text = "$voterCount voter")
                    }

                    InfoPanel(
                        title = stringResource(R.string.create_election_registered_voter_wallet),
                        message = BuildConfig.DEMO_VOTER_WALLET_ADDRESS
                    )
                }

                FancySectionCard(
                    step = "04",
                    title = stringResource(R.string.create_election_section_schedule_title),
                    subtitle = stringResource(R.string.create_election_section_schedule_subtitle)
                ) {
                    OutlinedTextField(
                        value = startDateTimeInput,
                        onValueChange = {
                            startDateTimeInput = it
                            errorMessage = ""
                        },
                        label = { Text(text = stringResource(R.string.create_election_label_start_time)) },
                        placeholder = { Text(text = stringResource(R.string.create_election_placeholder_datetime)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isCreating,
                        shape = RoundedCornerShape(18.dp)
                    )

                    OutlinedTextField(
                        value = endDateTimeInput,
                        onValueChange = {
                            endDateTimeInput = it
                            errorMessage = ""
                        },
                        label = { Text(text = stringResource(R.string.create_election_label_end_time)) },
                        placeholder = { Text(text = stringResource(R.string.create_election_placeholder_datetime)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isCreating,
                        shape = RoundedCornerShape(18.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val currentTime = System.currentTimeMillis()
                                startDateTimeInput = formatDateTime(currentTime - 60 * 1000)
                                endDateTimeInput = formatDateTime(currentTime + 60 * 60 * 1000)
                                errorMessage = ""
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isCreating,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(text = stringResource(R.string.create_election_start_now))
                        }

                        OutlinedButton(
                            onClick = {
                                val startMillis = parseDateTimeToMillis(startDateTimeInput)
                                    ?: System.currentTimeMillis()
                                endDateTimeInput = formatDateTime(startMillis + 3 * 60 * 60 * 1000)
                                errorMessage = ""
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isCreating,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(text = stringResource(R.string.create_election_plus_three_hours))
                        }
                    }

                    InfoPanel(
                        title = stringResource(R.string.create_election_date_format_title),
                        message = stringResource(R.string.create_election_date_format_message)
                    )
                }

                if (errorMessage.isNotBlank()) {
                    ErrorPanel(message = errorMessage)
                }

                DeployCard(
                    isCreating = isCreating,
                    onCancel = { navController.popBackStack() },
                    onCreate = {
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
                                                eligibleVoterIdsInput = cleanedEligibleVoters.joinToString("\n")
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
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun HeroCard(
    candidateCount: Int,
    voterCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f)
            ) {
                Text(
                    text = "ADMIN SETUP",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Text(
                text = stringResource(R.string.create_election_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = stringResource(R.string.create_election_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HeroMetric(
                    label = "Candidates",
                    value = candidateCount.toString(),
                    modifier = Modifier.weight(1f)
                )

                HeroMetric(
                    label = "Voters",
                    value = voterCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HeroMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FancySectionCard(
    step: String,
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 7.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = step,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f)
            )

            content()
        }
    }
}

@Composable
private fun CountBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun InfoPanel(
    title: String,
    message: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorPanel(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DeployCard(
    isCreating: Boolean,
    onCancel: () -> Unit,
    onCreate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 7.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Ready to create on blockchain?",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "This will deploy the election data and whitelist rules to your local blockchain prototype.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.82f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    enabled = !isCreating,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(text = stringResource(R.string.create_election_cancel_button))
                }

                Button(
                    onClick = onCreate,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    enabled = !isCreating,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = if (isCreating) {
                            stringResource(R.string.create_election_creating_button)
                        } else {
                            stringResource(R.string.create_election_create_button)
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun appendUniqueValue(
    input: String,
    value: String
): String {
    val existingValues = parseMultiValueInput(input)

    return if (existingValues.any { it.equals(value, ignoreCase = true) }) {
        input
    } else {
        if (input.isBlank()) {
            value
        } else {
            input.trimEnd() + "\n" + value
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