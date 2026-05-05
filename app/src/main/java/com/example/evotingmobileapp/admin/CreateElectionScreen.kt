package com.example.evotingmobileapp.admin

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.evotingmobileapp.R
import com.example.evotingmobileapp.blockchain.DemoWallets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val CreateNavy = Color(0xFF07133A)
private val CreateDeepBlue = Color(0xFF102A70)
private val CreateIndigo = Color(0xFF4F32F6)
private val CreatePurple = Color(0xFF7C3AED)
private val CreateBlue = Color(0xFF1479FF)
private val CreateTeal = Color(0xFF00B8A9)
private val CreateCoral = Color(0xFFFF5C7A)
private val CreateAmber = Color(0xFFFFB020)
private val CreateGreen = Color(0xFF17C964)
private val CreateRed = Color(0xFFE5484D)
private val CreateCard = Color(0xFFF6F8FF)
private val CreatePanel = Color(0xFFEAF1FF)
private val CreateInk = Color(0xFF081229)
private val CreateMuted = Color(0xFF59627E)
private val CreateLine = Color(0xFFD6DDF4)

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
        CreateElectionSuccessDialog(
            candidateCount = candidateCount,
            voterCount = voterCount,
            onDismiss = {
                showSuccessDialog = false
                navController.popBackStack()
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            CreateNavy,
                            CreateDeepBlue,
                            Color(0xFF3155D8),
                            Color(0xFFD8ECFF)
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            CreateDecorativeBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp)
                    .padding(top = 16.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                HeroCard(
                    candidateCount = candidateCount,
                    voterCount = voterCount
                )

                CreateProgressCard(
                    hasTitle = title.trim().isNotBlank(),
                    candidateCount = candidateCount,
                    voterCount = voterCount,
                    hasSchedule = parseDateTimeToMillis(startDateTimeInput) != null &&
                            parseDateTimeToMillis(endDateTimeInput) != null
                )

                FancySectionCard(
                    step = "01",
                    title = stringResource(R.string.create_election_section_identity_title),
                    subtitle = stringResource(R.string.create_election_section_identity_subtitle),
                    accent = CreateBlue
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

                    InfoPanel(
                        title = "Election identity",
                        message = "Use a clear title that will be understandable to voters and polling officers.",
                        accent = CreateBlue
                    )
                }

                FancySectionCard(
                    step = "02",
                    title = stringResource(R.string.create_election_section_candidates_title),
                    subtitle = stringResource(R.string.create_election_section_candidates_subtitle),
                    accent = CreateTeal
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

                    CountBadge(
                        text = stringResource(
                            R.string.create_election_candidate_count,
                            candidateCount
                        ),
                        accent = CreateTeal
                    )
                }

                FancySectionCard(
                    step = "03",
                    title = stringResource(R.string.create_election_section_whitelist_title),
                    subtitle = stringResource(R.string.create_election_section_whitelist_subtitle),
                    accent = CreateCoral
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
                                    value = DemoWallets.allVoterAddressesText
                                )
                                errorMessage = ""
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isCreating,
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CreateCoral)
                        ) {
                            Text(
                                text = stringResource(R.string.create_election_use_demo_voters),
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        CountBadge(
                            text = stringResource(
                                R.string.create_election_voter_count,
                                voterCount
                            ),
                            accent = CreateCoral
                        )
                    }

                    InfoPanel(
                        title = stringResource(R.string.create_election_registered_voter_wallets),
                        message = DemoWallets.allVoterAddressesText,
                        accent = CreateCoral,
                        monospace = true
                    )
                }

                FancySectionCard(
                    step = "04",
                    title = stringResource(R.string.create_election_section_schedule_title),
                    subtitle = stringResource(R.string.create_election_section_schedule_subtitle),
                    accent = CreateAmber
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
                            Text(
                                text = stringResource(R.string.create_election_start_now),
                                fontWeight = FontWeight.Bold
                            )
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
                            Text(
                                text = stringResource(R.string.create_election_plus_three_hours),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    InfoPanel(
                        title = stringResource(R.string.create_election_date_format_title),
                        message = stringResource(R.string.create_election_date_format_message),
                        accent = CreateAmber
                    )
                }

                ReviewCard(
                    title = title,
                    candidateCount = candidateCount,
                    voterCount = voterCount,
                    startDateTimeInput = startDateTimeInput,
                    endDateTimeInput = endDateTimeInput
                )

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
private fun CreateDecorativeBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .size(230.dp)
                .offset(x = (-90).dp, y = 40.dp),
            shape = CircleShape,
            color = CreateTeal.copy(alpha = 0.16f)
        ) {}

        Surface(
            modifier = Modifier
                .size(190.dp)
                .align(Alignment.TopEnd)
                .offset(x = 72.dp, y = 88.dp),
            shape = CircleShape,
            color = CreateCoral.copy(alpha = 0.16f)
        ) {}

        Surface(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 98.dp, y = 36.dp),
            shape = CircleShape,
            color = CreateAmber.copy(alpha = 0.14f)
        ) {}

        Surface(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-130).dp, y = 110.dp),
            shape = CircleShape,
            color = CreatePurple.copy(alpha = 0.15f)
        ) {}
    }
}

@Composable
private fun CreateElectionSuccessDialog(
    candidateCount: Int,
    voterCount: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            // Keep the success dialog visible until the admin confirms.
        },
        containerColor = CreateCard,
        shape = RoundedCornerShape(30.dp),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(62.dp),
                    shape = CircleShape,
                    color = CreateGreen.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, CreateGreen.copy(alpha = 0.34f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "✓",
                            style = MaterialTheme.typography.headlineSmall,
                            color = CreateGreen,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Text(
                    text = "Election created",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = CreateInk,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = CreateGreen.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, CreateGreen.copy(alpha = 0.24f))
                ) {
                    Text(
                        text = "The election has been deployed to the blockchain.",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = CreateInk,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = stringResource(R.string.create_election_success_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = CreateMuted
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = CreatePanel.copy(alpha = 0.86f),
                    border = BorderStroke(1.dp, CreateLine)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DialogSummaryRow(
                            label = "Candidates",
                            value = candidateCount.toString()
                        )

                        DialogSummaryRow(
                            label = "Eligible voters",
                            value = voterCount.toString()
                        )

                        DialogSummaryRow(
                            label = "Election status",
                            value = "Ready for QR check-in and voting"
                        )
                    }
                }

                Text(
                    text = "Next step: polling officers can check in eligible voters before they submit votes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CreateMuted
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CreateIndigo)
            ) {
                Text(
                    text = stringResource(R.string.create_election_success_button),
                    fontWeight = FontWeight.Black
                )
            }
        }
    )
}

@Composable
private fun HeroCard(
    candidateCount: Int,
    voterCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.95f),
                            Color(0xFFEFF4FF).copy(alpha = 0.92f)
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
        ) {
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 28.dp, y = (-32).dp),
                shape = CircleShape,
                color = CreateTeal.copy(alpha = 0.12f)
            ) {}

            Surface(
                modifier = Modifier
                    .size(78.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 30.dp, y = 24.dp),
                shape = CircleShape,
                color = CreateCoral.copy(alpha = 0.10f)
            ) {}

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(58.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = CreateIndigo,
                        shadowElevation = 8.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "SV",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "SecureVote Nepal",
                            style = MaterialTheme.typography.titleSmall,
                            color = CreateIndigo,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = stringResource(R.string.create_election_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = CreateInk,
                            fontWeight = FontWeight.Black
                        )
                    }

                    HeaderChip(text = "ADMIN")
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = CreateCoral.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, CreateCoral.copy(alpha = 0.30f))
                ) {
                    Text(
                        text = stringResource(R.string.create_election_admin_setup_badge),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = CreateCoral,
                        fontWeight = FontWeight.Black
                    )
                }

                Text(
                    text = stringResource(R.string.create_election_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = CreateMuted
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HeroMetric(
                        label = stringResource(R.string.create_election_metric_candidates),
                        value = candidateCount.toString(),
                        modifier = Modifier.weight(1f),
                        accent = CreateTeal
                    )

                    HeroMetric(
                        label = stringResource(R.string.create_election_metric_voters),
                        value = voterCount.toString(),
                        modifier = Modifier.weight(1f),
                        accent = CreateCoral
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderChip(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = CreateIndigo.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, CreateIndigo.copy(alpha = 0.22f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelSmall,
            color = CreateIndigo,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun HeroMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = accent.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.20f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = CreateInk,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = CreateMuted,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CreateProgressCard(
    hasTitle: Boolean,
    candidateCount: Int,
    voterCount: Int,
    hasSchedule: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.18f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Election setup checklist",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProgressStep(
                    modifier = Modifier.weight(1f),
                    number = "1",
                    label = "Title",
                    done = hasTitle
                )

                ProgressStep(
                    modifier = Modifier.weight(1f),
                    number = "2",
                    label = "Candidates",
                    done = candidateCount >= 2
                )

                ProgressStep(
                    modifier = Modifier.weight(1f),
                    number = "3",
                    label = "Voters",
                    done = voterCount > 0
                )

                ProgressStep(
                    modifier = Modifier.weight(1f),
                    number = "4",
                    label = "Schedule",
                    done = hasSchedule
                )
            }
        }
    }
}

@Composable
private fun ProgressStep(
    modifier: Modifier = Modifier,
    number: String,
    label: String,
    done: Boolean
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = if (done) CreateGreen.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.12f),
        border = BorderStroke(
            width = 1.dp,
            color = if (done) CreateGreen.copy(alpha = 0.50f) else Color.White.copy(alpha = 0.18f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                color = if (done) CreateGreen else Color.White.copy(alpha = 0.16f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (done) "✓" else number,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.92f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FancySectionCard(
    step: String,
    title: String,
    subtitle: String,
    accent: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    ModernCreateCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = accent.copy(alpha = 0.13f),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.28f))
            ) {
                Text(
                    text = step,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = accent,
                    fontWeight = FontWeight.Black
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = CreateInk
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CreateMuted
                )
            }
        }

        HorizontalDivider(color = CreateLine.copy(alpha = 0.80f))

        content()
    }
}

@Composable
private fun ReviewCard(
    title: String,
    candidateCount: Int,
    voterCount: Int,
    startDateTimeInput: String,
    endDateTimeInput: String
) {
    ModernCreateCard {
        SectionTitle(
            eyebrow = "REVIEW",
            title = "Final review before blockchain deployment",
            subtitle = "Check the election details before creating it. Once submitted, the election setup is sent to the smart contract."
        )

        HorizontalDivider(color = CreateLine.copy(alpha = 0.80f))

        ReviewRow(
            label = "Election title",
            value = title.ifBlank { "Not entered yet" },
            accent = CreateBlue
        )

        ReviewRow(
            label = "Candidates",
            value = "$candidateCount added",
            accent = CreateTeal
        )

        ReviewRow(
            label = "Eligible voters",
            value = "$voterCount whitelisted",
            accent = CreateCoral
        )

        ReviewRow(
            label = "Start time",
            value = startDateTimeInput,
            accent = CreateAmber
        )

        ReviewRow(
            label = "End time",
            value = endDateTimeInput,
            accent = CreateAmber
        )
    }
}

@Composable
private fun SectionTitle(
    eyebrow: String,
    title: String,
    subtitle: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = eyebrow,
            style = MaterialTheme.typography.labelSmall,
            color = CreateTeal,
            fontWeight = FontWeight.Black
        )

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = CreateInk,
            fontWeight = FontWeight.Black
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = CreateMuted
        )
    }
}

@Composable
private fun ReviewRow(
    label: String,
    value: String,
    accent: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = CreatePanel.copy(alpha = 0.86f),
        border = BorderStroke(1.dp, CreateLine.copy(alpha = 0.72f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(0.9f),
                style = MaterialTheme.typography.labelLarge,
                color = accent,
                fontWeight = FontWeight.Black
            )

            Text(
                text = value,
                modifier = Modifier.weight(1.1f),
                style = MaterialTheme.typography.bodyMedium,
                color = CreateMuted,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DialogSummaryRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            color = CreateIndigo,
            fontWeight = FontWeight.Black
        )

        Text(
            text = value,
            modifier = Modifier.weight(1.2f),
            style = MaterialTheme.typography.bodySmall,
            color = CreateMuted,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CountBadge(
    text: String,
    accent: Color
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = accent.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.26f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = accent,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun InfoPanel(
    title: String,
    message: String,
    accent: Color,
    monospace: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = accent.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.20f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = accent,
                fontWeight = FontWeight.Black
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = CreateMuted,
                fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default
            )
        }
    }
}

@Composable
private fun ErrorPanel(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = CreateRed.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, CreateRed.copy(alpha = 0.34f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = CreateRed.copy(alpha = 0.16f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "!",
                        style = MaterialTheme.typography.labelLarge,
                        color = CreateRed,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
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
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            CreateIndigo,
                            CreateBlue,
                            CreateTeal
                        )
                    ),
                    shape = RoundedCornerShape(30.dp)
                )
        ) {
            Surface(
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = (-34).dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.13f)
            ) {}

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.create_election_deploy_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = stringResource(R.string.create_election_deploy_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.86f)
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
                        Text(
                            text = stringResource(R.string.create_election_cancel_button),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = onCreate,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        enabled = !isCreating,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CreateCoral),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 5.dp)
                    ) {
                        Text(
                            text = if (isCreating) {
                                stringResource(R.string.create_election_creating_button)
                            } else {
                                stringResource(R.string.create_election_create_button)
                            },
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernCreateCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = CreateCard.copy(alpha = 0.96f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

private fun appendUniqueValue(
    input: String,
    value: String
): String {
    val existingValues = parseMultiValueInput(input)
    val valuesToAdd = parseMultiValueInput(value).filterNot { newValue ->
        existingValues.any { existingValue -> existingValue.equals(newValue, ignoreCase = true) }
    }

    return (existingValues + valuesToAdd).joinToString("\n")
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