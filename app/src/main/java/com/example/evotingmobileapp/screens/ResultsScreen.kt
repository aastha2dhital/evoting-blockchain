package com.example.evotingmobileapp.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
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

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var closingElectionId by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(elections) {
        elections.forEach { election ->
            adminViewModel.loadTurnoutCount(election.id)
        }
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.24f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.14f)
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
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = 16.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ResultsHeroCard(
                    electionCount = elections.size,
                    closedCount = elections.count { it.isClosed() }
                )
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
                                        exception.message ?: "Election could not be closed."
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
private fun ResultsHeroCard(
    electionCount: Int,
    closedCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "SV",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "SecureVote Nepal",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Election results",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }

                HeaderChip(text = "RESULTS")
            }

            Text(
                text = "View turnout while voting is open. Final candidate results and winner details appear after the election closes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MiniHeroTile(
                    modifier = Modifier.weight(1f),
                    title = electionCount.toString(),
                    subtitle = "Elections"
                )

                MiniHeroTile(
                    modifier = Modifier.weight(1f),
                    title = closedCount.toString(),
                    subtitle = "Closed"
                )
            }
        }
    }
}

@Composable
private fun HeaderChip(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun MiniHeroTile(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.58f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyResultsState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "0",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = "No elections available",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Create an election first. Results and turnout summaries will appear here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
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
    val winnerSummary = calculateWinnerSummary(election)

    val statusText = if (isClosed) "Closed" else "Open"
    val turnoutText = turnoutCount?.toString() ?: "Loading"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElectionHeader(
                title = election.title,
                subtitle = "Election ID: ${election.id}",
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
                        .height(54.dp),
                    enabled = !isClosing,
                    shape = RoundedCornerShape(18.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    Text(
                        text = if (isClosing) {
                            "Closing election..."
                        } else {
                            "Close election early"
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                FinalResultsPanel()

                WinnerPanel(winnerSummary = winnerSummary)

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)
                )

                Text(
                    text = "Candidate results",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (election.candidates.isEmpty()) {
                    InfoPanel(
                        title = "No candidates",
                        message = "No candidate records were found for this election.",
                        positive = false
                    )
                } else {
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
                                    candidateSymbol = candidateSymbolFor(candidate, index),
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
}

@Composable
private fun ElectionHeader(
    title: String,
    subtitle: String,
    statusText: String,
    isClosed: Boolean
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
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        StatusChip(
            text = statusText,
            isClosed = isClosed
        )
    }
}

@Composable
private fun SummaryPanel(
    statusText: String,
    turnoutText: String,
    totalVotes: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryTile(
            modifier = Modifier.weight(1f),
            label = "Status",
            value = statusText
        )

        SummaryTile(
            modifier = Modifier.weight(1f),
            label = "Turnout",
            value = turnoutText
        )

        SummaryTile(
            modifier = Modifier.weight(1f),
            label = "Votes",
            value = totalVotes.toString()
        )
    }
}

@Composable
private fun SummaryTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.68f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
private fun DatePanel(
    startText: String,
    endText: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f)
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ResultInfoRow(
                label = "Start",
                value = startText
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.70f)
            )

            ResultInfoRow(
                label = "End",
                value = endText
            )
        }
    }
}

@Composable
private fun LockedResultsPanel() {
    InfoPanel(
        title = "Results locked",
        message = "Turnout is visible while voting is open. Candidate results will appear after the election closes.",
        positive = false
    )
}

@Composable
private fun FinalResultsPanel() {
    InfoPanel(
        title = "Final results available",
        message = "The election is closed. Winner and candidate vote totals are now visible.",
        positive = true
    )
}

@Composable
private fun WinnerPanel(
    winnerSummary: WinnerSummary
) {
    val title = when {
        !winnerSummary.hasVotes -> "No winner yet"
        winnerSummary.isTie -> "Joint winners"
        else -> "Winner"
    }

    val message = when {
        !winnerSummary.hasVotes ->
            "No votes were recorded for this election."

        winnerSummary.isTie ->
            "${winnerSummary.winnerNames.joinToString(", ")} tied with ${winnerSummary.winningVotes} ${voteWord(winnerSummary.winningVotes)} each, out of ${winnerSummary.totalVotes} total ${voteWord(winnerSummary.totalVotes)}."

        else ->
            "${winnerSummary.winnerNames.first()} won with ${winnerSummary.winningVotes} ${voteWord(winnerSummary.winningVotes)}, out of ${winnerSummary.totalVotes} total ${voteWord(winnerSummary.totalVotes)}."
    }

    val containerColor = if (winnerSummary.hasVotes) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.76f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.74f)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = CircleShape,
                color = if (winnerSummary.hasVotes) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (winnerSummary.hasVotes) "1" else "-",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (winnerSummary.hasVotes) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

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
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.58f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
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
    candidateSymbol: String,
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

    val containerColor = if (isWinner) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.78f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.66f)
    }

    val border = if (isWinner) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.42f))
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f))
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = containerColor,
        border = border
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = if (isWinner) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = candidateSymbol,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = "#$rank  $candidate",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isWinner) {
                            SmallBadge(
                                text = if (isTie) "Joint winner" else "Winner"
                            )
                        }

                        SmallBadge(text = percentageText)
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = voteCount.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
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
                trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
            )
        }
    }
}

@Composable
private fun SmallBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.82f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.SemiBold
        )
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
            style = MaterialTheme.typography.bodyMedium,
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
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.82f)
    } else {
        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.82f)
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
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun InfoPanel(
    title: String,
    message: String,
    positive: Boolean
) {
    val containerColor = if (positive) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.62f)
    } else {
        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.66f)
    }

    val contentColor = if (positive) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onTertiaryContainer
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = containerColor
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = 0.86f)
            )
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
        Text(
            text = "Back",
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun calculateWinnerSummary(election: Election): WinnerSummary {
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

private fun candidateSymbolFor(candidateName: String, index: Int): String {
    val cleanedName = candidateName.trim()

    if (cleanedName.isNotBlank()) {
        val firstLetter = cleanedName.first().uppercaseChar()
        if (firstLetter.isLetterOrDigit()) {
            return firstLetter.toString()
        }
    }

    return (index + 1).toString()
}

private fun voteWord(count: Int): String {
    return if (count == 1) "vote" else "votes"
}