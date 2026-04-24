package com.example.evotingmobileapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.evotingmobileapp.R
import com.example.evotingmobileapp.admin.AdminViewModel
import com.example.evotingmobileapp.model.Election
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ResultsScreen(
    adminViewModel: AdminViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController? = null
) {
    val elections by adminViewModel.elections.collectAsState()
    val turnoutCounts by adminViewModel.turnoutCounts.collectAsState()

    LaunchedEffect(elections) {
        elections.forEach { election ->
            adminViewModel.loadTurnoutCount(election.id)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var closingElectionId by rememberSaveable { mutableStateOf<String?>(null) }

    val closeElectionFailedMessage = stringResource(R.string.results_close_failed)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            ResultsHeroCard()

            if (elections.isEmpty()) {
                EmptyResultsState()

                navController?.let { controller ->
                    ResultsBackButton(navController = controller)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(elections) { election ->
                        ElectionResultCard(
                            election = election,
                            turnoutCount = turnoutCounts[election.id],
                            isClosing = closingElectionId == election.id,
                            onCloseElectionEarly = {
                                if (closingElectionId != null) {
                                    return@ElectionResultCard
                                }

                                closingElectionId = election.id

                                coroutineScope.launch {
                                    val result = withContext(Dispatchers.IO) {
                                        adminViewModel.closeElectionEarly(election.id)
                                    }

                                    closingElectionId = null

                                    val message = result.fold(
                                        onSuccess = { successMessage -> successMessage },
                                        onFailure = { exception ->
                                            exception.message ?: closeElectionFailedMessage
                                        }
                                    )

                                    snackbarHostState.showSnackbar(message)
                                }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                navController?.let { controller ->
                    ResultsBackButton(navController = controller)
                }
            }
        }
    }
}

@Composable
private fun ResultsHeroCard() {
    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary
        )
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .background(brush = gradient)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.results_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = stringResource(R.string.results_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f)
            )

            PillColumn(
                items = listOf(
                    stringResource(R.string.results_pill_turnout),
                    stringResource(R.string.results_pill_locking),
                    stringResource(R.string.results_pill_admin_close)
                )
            )
        }
    }
}

@Composable
private fun EmptyResultsState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.results_empty_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.results_empty_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ElectionResultCard(
    election: Election,
    turnoutCount: Int?,
    isClosing: Boolean,
    onCloseElectionEarly: () -> Unit
) {
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val totalVotes = election.voteCounts.values.sum()
    val statusText = if (election.isClosed()) {
        stringResource(R.string.results_status_closed)
    } else {
        stringResource(R.string.results_status_open)
    }

    val turnoutText = turnoutCount?.toString() ?: stringResource(R.string.results_loading)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionTitle(
                title = election.title,
                subtitle = stringResource(R.string.results_election_overview_subtitle)
            )

            PillColumn(
                items = listOf(
                    stringResource(R.string.results_status_summary, statusText),
                    stringResource(R.string.results_turnout_summary, turnoutText),
                    stringResource(R.string.results_total_votes_summary, totalVotes)
                )
            )

            ResultInfoRow(
                label = stringResource(R.string.results_start_label),
                value = formatter.format(Date(election.startTimeMillis))
            )

            ResultInfoRow(
                label = stringResource(R.string.results_end_label),
                value = formatter.format(Date(election.endTimeMillis))
            )

            if (!election.isClosed()) {
                StatusSurface(
                    text = stringResource(R.string.results_locked_until_close),
                    positive = false
                )

                OutlinedButton(
                    onClick = onCloseElectionEarly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = !isClosing,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = if (isClosing) {
                            stringResource(R.string.results_closing_button)
                        } else {
                            stringResource(R.string.results_close_early_button)
                        }
                    )
                }

                return@Column
            }

            StatusSurface(
                text = stringResource(R.string.results_final_available),
                positive = true
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Text(
                text = stringResource(R.string.results_candidate_results_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            election.candidates.forEachIndexed { index, candidate ->
                val voteCount = election.voteCounts[candidate] ?: 0
                val percentage = if (totalVotes > 0) {
                    (voteCount.toFloat() / totalVotes.toFloat()) * 100f
                } else {
                    0f
                }

                CandidateResultCard(
                    candidate = candidate,
                    voteCount = voteCount,
                    percentageText = String.format(Locale.getDefault(), "%.1f%%", percentage)
                )

                if (index != election.candidates.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 2.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CandidateResultCard(
    candidate: String,
    voteCount: Int,
    percentageText: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = candidate,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.results_vote_count, voteCount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            StatusSurface(
                text = stringResource(R.string.results_vote_share, percentageText),
                positive = true
            )
        }
    }
}

@Composable
private fun ResultInfoRow(
    label: String,
    value: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatusSurface(
    text: String,
    positive: Boolean
) {
    val containerColor = if (positive) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.tertiaryContainer
    }

    val contentColor = if (positive) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onTertiaryContainer
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = containerColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor
        )
    }
}

@Composable
private fun PillColumn(
    items: List<String>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEach { item ->
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f)
            ) {
                Text(
                    text = item,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ResultsBackButton(
    navController: NavHostController
) {
    OutlinedButton(
        onClick = { navController.popBackStack() },
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Text(text = stringResource(R.string.results_back_button))
    }
}