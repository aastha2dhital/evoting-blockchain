package com.example.evotingmobileapp.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

private data class ObserverWinnerSummary(
    val winnerNames: List<String>,
    val winningVotes: Int,
    val totalVotes: Int
) {
    val hasVotes: Boolean = totalVotes > 0
    val isTie: Boolean = winnerNames.size > 1
}

@Composable
fun ObserverTurnoutScreen(
    navController: NavHostController,
    adminViewModel: AdminViewModel
) {
    val elections by adminViewModel.elections.collectAsState()
    val turnoutCounts by adminViewModel.turnoutCounts.collectAsState()

    LaunchedEffect(Unit) {
        adminViewModel.refreshBlockchainData()
    }

    LaunchedEffect(elections) {
        elections.forEach { election ->
            adminViewModel.loadTurnoutCount(election.id)
        }
    }

    val now = remember(elections) { System.currentTimeMillis() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.24f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.14f)
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
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ObserverHeroCard(
                electionCount = elections.size,
                activeCount = elections.count { it.isActive(now) },
                closedCount = elections.count { it.isClosed(now) }
            )

            if (elections.isEmpty()) {
                EmptyObserverState()
            } else {
                elections.forEach { election ->
                    ObserverElectionCard(
                        election = election,
                        turnoutCount = turnoutCounts[election.id],
                        now = now
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = "Back",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = {
                        adminViewModel.refreshBlockchainData()
                        elections.forEach { election ->
                            adminViewModel.loadTurnoutCount(election.id)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    Text(
                        text = "Refresh",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ObserverHeroCard(
    electionCount: Int,
    activeCount: Int,
    closedCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
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
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
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
                        text = "Public turnout monitor",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }

                HeaderChip(text = "OBSERVER")
            }

            Text(
                text = "Read-only public visibility for turnout and closed election summaries. Admin-only actions are not available here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ObserverMetricTile(
                    modifier = Modifier.weight(1f),
                    label = "Elections",
                    value = electionCount.toString()
                )

                ObserverMetricTile(
                    modifier = Modifier.weight(1f),
                    label = "Active",
                    value = activeCount.toString()
                )

                ObserverMetricTile(
                    modifier = Modifier.weight(1f),
                    label = "Closed",
                    value = closedCount.toString()
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
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ObserverMetricTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.58f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ObserverElectionCard(
    election: Election,
    turnoutCount: Int?,
    now: Long
) {
    val eligibleCount = election.eligibleVoterIds.size
    val checkedInCount = election.checkedInVoterIds.size
    val localVotesCast = election.voteCounts.values.sum()
    val votesCast = turnoutCount ?: localVotesCast

    val turnoutPercent = if (eligibleCount > 0) {
        (votesCast.toDouble() / eligibleCount.toDouble()) * 100.0
    } else {
        0.0
    }

    val checkInPercent = if (eligibleCount > 0) {
        (checkedInCount.toDouble() / eligibleCount.toDouble()) * 100.0
    } else {
        0.0
    }

    val closed = election.isClosed(now)
    val active = election.isActive(now)

    val statusText = when {
        closed -> "Closed"
        active -> "Active"
        election.hasStarted(now) -> "Open"
        else -> "Scheduled"
    }

    val winnerSummary = calculateObserverWinnerSummary(election)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            ObserverElectionHeader(
                election = election,
                statusText = statusText,
                closed = closed
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TurnoutTile(
                    modifier = Modifier.weight(1f),
                    label = "Eligible",
                    value = eligibleCount.toString(),
                    detail = "registered"
                )

                TurnoutTile(
                    modifier = Modifier.weight(1f),
                    label = "Checked-in",
                    value = checkedInCount.toString(),
                    detail = "${formatPercent(checkInPercent)}%"
                )

                TurnoutTile(
                    modifier = Modifier.weight(1f),
                    label = "Votes",
                    value = votesCast.toString(),
                    detail = "${formatPercent(turnoutPercent)}%"
                )
            }

            TurnoutSummaryPanel(
                checkedInCount = checkedInCount,
                eligibleCount = eligibleCount,
                votesCast = votesCast
            )

            if (closed) {
                ResultPreviewPanel(
                    voteCounts = election.voteCounts,
                    winnerSummary = winnerSummary
                )
            } else {
                HiddenResultsPanel()
            }
        }
    }
}

@Composable
private fun ObserverElectionHeader(
    election: Election,
    statusText: String,
    closed: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(50.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = election.id,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = election.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${formatDate(election.startTimeMillis)} to ${formatDate(election.endTimeMillis)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        StatusChip(
            text = statusText,
            closed = closed
        )
    }
}

@Composable
private fun StatusChip(
    text: String,
    closed: Boolean
) {
    val containerColor = if (closed) {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.82f)
    } else {
        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.82f)
    }

    val contentColor = if (closed) {
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
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

@Composable
private fun TurnoutTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    detail: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.68f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = detail,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.76f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TurnoutSummaryPanel(
    checkedInCount: Int,
    eligibleCount: Int,
    votesCast: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.46f)
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(
                text = "Turnout summary",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Checked-in voters: $checkedInCount of $eligibleCount. Votes cast: $votesCast of $eligibleCount.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Observer mode is read-only and does not provide admin, check-in, voting, or closing controls.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f)
            )
        }
    }
}

@Composable
private fun HiddenResultsPanel() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
    ) {
        Text(
            text = "Candidate results remain hidden until this election is closed.",
            modifier = Modifier.padding(15.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ResultPreviewPanel(
    voteCounts: Map<String, Int>,
    winnerSummary: ObserverWinnerSummary
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.76f)
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Closed election result",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Bold
            )

            when {
                voteCounts.isEmpty() -> {
                    Text(
                        text = "No vote totals are available yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                !winnerSummary.hasVotes -> {
                    Text(
                        text = "No votes were recorded for this election.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                winnerSummary.isTie -> {
                    Text(
                        text = "${winnerSummary.winnerNames.joinToString(", ")} tied with ${winnerSummary.winningVotes} ${voteWord(winnerSummary.winningVotes)} each.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                else -> {
                    Text(
                        text = "${winnerSummary.winnerNames.first()} won with ${winnerSummary.winningVotes} ${voteWord(winnerSummary.winningVotes)}.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            voteCounts.entries
                .sortedWith(
                    compareByDescending<Map.Entry<String, Int>> { it.value }
                        .thenBy { it.key.lowercase(Locale.getDefault()) }
                )
                .forEachIndexed { index, entry ->
                    ResultPreviewRow(
                        rank = index + 1,
                        candidate = entry.key,
                        votes = entry.value
                    )
                }
        }
    }
}

@Composable
private fun ResultPreviewRow(
    rank: Int,
    candidate: String,
    votes: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = rank.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = candidate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "$votes ${voteWord(votes)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.82f)
            )
        }
    }
}

@Composable
private fun EmptyObserverState() {
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "OBS",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "No elections available",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Once an election is created, public turnout summaries will appear here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun calculateObserverWinnerSummary(election: Election): ObserverWinnerSummary {
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

    return ObserverWinnerSummary(
        winnerNames = winnerNames,
        winningVotes = winningVotes,
        totalVotes = totalVotes
    )
}

private fun formatDate(timeMillis: Long): String {
    val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    return formatter.format(Date(timeMillis))
}

private fun formatPercent(value: Double): String {
    return String.format(Locale.getDefault(), "%.1f", value)
}

private fun voteWord(count: Int): String {
    return if (count == 1) "vote" else "votes"
}