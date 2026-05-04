package com.example.evotingmobileapp.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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

private data class WinnerSummary(
    val winnerNames: List<String>,
    val winningVotes: Int,
    val totalVotes: Int
) {
    val hasVotes: Boolean = totalVotes > 0
    val isTie: Boolean = winnerNames.size > 1
}

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

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.26f),
            MaterialTheme.colorScheme.background
        )
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 18.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                ResultsHeroCard()
            }

            if (elections.isEmpty()) {
                item {
                    EmptyResultsState()
                }
            } else {
                items(
                    items = elections,
                    key = { election -> election.id }
                ) { election ->
                    ElectionResultCard(
                        election = election,
                        turnoutCount = turnoutCounts[election.id],
                        isClosing = closingElectionId == election.id,
                        onCloseElectionEarly = {
                            if (closingElectionId != null) return@ElectionResultCard

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
            }

            navController?.let { controller ->
                item {
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
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .background(brush = gradient)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f),
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )

            HeroPillColumn(
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
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
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
    val isClosed = election.isClosed()
    val winnerSummary = rememberWinnerSummary(election)

    val statusText = if (isClosed) {
        stringResource(R.string.results_status_closed)
    } else {
        stringResource(R.string.results_status_open)
    }

    val turnoutText = turnoutCount?.toString() ?: stringResource(R.string.results_loading)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            ElectionHeader(
                title = election.title,
                subtitle = stringResource(R.string.results_election_overview_subtitle),
                statusText = statusText,
                isClosed = isClosed
            )

            SummaryPanel(
                statusText = statusText,
                turnoutText = turnoutText,
                totalVotes = totalVotes
            )

            DatePanel(
                startText = formatter.format(Date(election.startTimeMillis)),
                endText = formatter.format(Date(election.endTimeMillis))
            )

            if (!isClosed) {
                LockedResultsPanel()

                Button(
                    onClick = onCloseElectionEarly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isClosing,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = if (isClosing) {
                            stringResource(R.string.results_closing_button)
                        } else {
                            stringResource(R.string.results_close_early_button)
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                FinalResultsPanel()

                WinnerPanel(winnerSummary = winnerSummary)

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Text(
                    text = stringResource(R.string.results_candidate_results_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    election.candidates
                        .sortedWith(
                            compareByDescending<String> { candidate ->
                                election.voteCounts[candidate] ?: 0
                            }.thenBy { candidate -> candidate.lowercase(Locale.getDefault()) }
                        )
                        .forEachIndexed { index, candidate ->
                            val voteCount = election.voteCounts[candidate] ?: 0
                            val percentage = if (totalVotes > 0) {
                                (voteCount.toFloat() / totalVotes.toFloat()) * 100f
                            } else {
                                0f
                            }

                            val isWinner = winnerSummary.hasVotes &&
                                    winnerSummary.winnerNames.any {
                                        it.equals(candidate, ignoreCase = true)
                                    }

                            CandidateResultCard(
                                rank = index + 1,
                                candidate = candidate,
                                voteCount = voteCount,
                                percentage = percentage,
                                isWinner = isWinner,
                                isTie = winnerSummary.isTie
                            )
                        }
                }
            }
        }
    }
}

@Composable
private fun ElectionHeader(
    title: String,
    subtitle: String,
    statusText: String,
    isClosed: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            StatusChip(
                text = statusText,
                isClosed = isClosed
            )
        }
    }
}

@Composable
private fun SummaryPanel(
    statusText: String,
    turnoutText: String,
    totalVotes: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryTile(
            label = "Election status",
            value = stringResource(R.string.results_status_summary, statusText)
        )

        SummaryTile(
            label = "Turnout",
            value = stringResource(R.string.results_turnout_summary, turnoutText)
        )

        SummaryTile(
            label = "Total votes",
            value = stringResource(R.string.results_total_votes_summary, totalVotes)
        )
    }
}

@Composable
private fun SummaryTile(
    label: String,
    value: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun DatePanel(
    startText: String,
    endText: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ResultInfoRow(
                label = stringResource(R.string.results_start_label),
                value = startText
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
            )

            ResultInfoRow(
                label = stringResource(R.string.results_end_label),
                value = endText
            )
        }
    }
}

@Composable
private fun LockedResultsPanel() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.results_locked_until_close),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Turnout can be checked while voting is open, but candidate results stay locked until the election is closed.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.82f)
            )
        }
    }
}

@Composable
private fun FinalResultsPanel() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = stringResource(R.string.results_final_available),
            modifier = Modifier.padding(18.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun WinnerPanel(
    winnerSummary: WinnerSummary
) {
    val title = when {
        !winnerSummary.hasVotes -> stringResource(R.string.results_winner_title)
        winnerSummary.isTie -> stringResource(R.string.results_winners_title)
        else -> stringResource(R.string.results_winner_title)
    }

    val message = when {
        !winnerSummary.hasVotes -> stringResource(R.string.results_no_votes_message)

        winnerSummary.isTie -> stringResource(
            R.string.results_tie_message,
            winnerSummary.winnerNames.joinToString(", "),
            winnerSummary.winningVotes,
            winnerSummary.totalVotes
        )

        else -> stringResource(
            R.string.results_winner_message,
            winnerSummary.winnerNames.first(),
            winnerSummary.winningVotes,
            winnerSummary.totalVotes
        )
    }

    val containerColor = if (winnerSummary.hasVotes) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (winnerSummary.hasVotes) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(62.dp),
                shape = CircleShape,
                color = if (winnerSummary.hasVotes) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (winnerSummary.hasVotes) "★" else "—",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (winnerSummary.hasVotes) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = contentColor,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }

            if (winnerSummary.hasVotes) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WinnerStatTile(
                        modifier = Modifier.weight(1f),
                        label = "Winning votes",
                        value = winnerSummary.winningVotes.toString()
                    )

                    WinnerStatTile(
                        modifier = Modifier.weight(1f),
                        label = "Total votes",
                        value = winnerSummary.totalVotes.toString()
                    )
                }

                Text(
                    text = if (winnerSummary.isTie) {
                        "This election ended in a tie, so multiple candidates share the highest vote count."
                    } else {
                        "${winnerSummary.winnerNames.first()} won with ${winnerSummary.winningVotes} ${voteWord(winnerSummary.winningVotes)}."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.82f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun WinnerStatTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CandidateResultCard(
    rank: Int,
    candidate: String,
    voteCount: Int,
    percentage: Float,
    isWinner: Boolean,
    isTie: Boolean
) {
    val progress by animateFloatAsState(
        targetValue = (percentage / 100f).coerceIn(0f, 1f),
        label = "candidateResultProgress"
    )

    val percentageText = String.format(Locale.getDefault(), "%.1f%%", percentage)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isWinner) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isWinner) 4.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    modifier = Modifier.size(38.dp),
                    shape = CircleShape,
                    color = if (isWinner) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = rank.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isWinner) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = candidate,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isWinner) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )

                    if (isWinner) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = if (isTie) "TIED HIGHEST" else stringResource(R.string.results_winner_badge),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = voteCount.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = voteWord(voteCount),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = stringResource(R.string.results_vote_share, percentageText),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = if (isWinner) "Highest result" else "Candidate result",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ResultInfoRow(
    label: String,
    value: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StatusChip(
    text: String,
    isClosed: Boolean
) {
    val containerColor = if (isClosed) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.tertiaryContainer
    }

    val contentColor = if (isClosed) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onTertiaryContainer
    }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = containerColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun HeroPillColumn(
    items: List<String>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEach { item ->
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f)
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
            .height(56.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Text(
            text = stringResource(R.string.results_back_button),
            fontWeight = FontWeight.Bold
        )
    }
}

private fun rememberWinnerSummary(election: Election): WinnerSummary {
    val voteCounts = election.candidates.associateWith { candidate ->
        election.voteCounts[candidate] ?: 0
    }

    val totalVotes = voteCounts.values.sum()
    val winningVotes = voteCounts.values.maxOrNull() ?: 0

    val winnerNames = if (totalVotes <= 0 || winningVotes <= 0) {
        emptyList()
    } else {
        voteCounts
            .filterValues { votes -> votes == winningVotes }
            .keys
            .toList()
    }

    return WinnerSummary(
        winnerNames = winnerNames,
        winningVotes = winningVotes,
        totalVotes = totalVotes
    )
}

private fun voteWord(count: Int): String {
    return if (count == 1) "vote" else "votes"
}