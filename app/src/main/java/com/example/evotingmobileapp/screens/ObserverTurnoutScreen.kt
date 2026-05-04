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
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.26f),
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.18f),
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
                        fontWeight = FontWeight.Bold
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
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = "Refresh",
                        fontWeight = FontWeight.Bold
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
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
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
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.20f)
                )
            ) {
                Text(
                    text = "OBSERVER VIEW",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Black
                )
            }

            Text(
                text = "Public Turnout Monitor",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "Read-only election visibility for observers. This screen shows turnout and result summaries without admin controls.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f)
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
private fun ObserverMetricTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.86f),
                fontWeight = FontWeight.Bold,
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

    val statusColor = when {
        closed -> MaterialTheme.colorScheme.errorContainer
        active -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    val winner = election.voteCounts
        .filterValues { it >= 0 }
        .maxByOrNull { it.value }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            text = "#${election.id}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = election.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "${formatDate(election.startTimeMillis)} → ${formatDate(election.endTimeMillis)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = statusColor
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

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

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f)
            ) {
                Column(
                    modifier = Modifier.padding(15.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Turnout summary",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = "Checked-in voters: $checkedInCount of $eligibleCount • Votes cast: $votesCast of $eligibleCount",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Observer mode is read-only. It does not allow election creation, QR check-in, voting, closing, or admin-only actions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f)
                    )
                }
            }

            if (closed) {
                ResultPreviewPanel(
                    voteCounts = election.voteCounts,
                    winnerName = winner?.key,
                    winnerVotes = winner?.value
                )
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
                ) {
                    Text(
                        text = "Results remain hidden until this election is closed.",
                        modifier = Modifier.padding(15.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
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
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.78f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
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
private fun ResultPreviewPanel(
    voteCounts: Map<String, Int>,
    winnerName: String?,
    winnerVotes: Int?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.82f)
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Closed election result",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Black
            )

            if (voteCounts.isEmpty()) {
                Text(
                    text = "No vote totals are available yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                if (winnerName != null && winnerVotes != null) {
                    Text(
                        text = "Winner: $winnerName with $winnerVotes vote${if (winnerVotes == 1) "" else "s"}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Black
                    )
                }

                voteCounts.entries
                    .sortedByDescending { it.value }
                    .forEachIndexed { index, entry ->
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
                                        text = (index + 1).toString(),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.key,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = "${entry.value} vote${if (entry.value == 1) "" else "s"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.82f)
                                )
                            }
                        }
                    }
            }
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
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Text(
                text = "No elections available",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Once an election is created, observer turnout summaries will appear here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatDate(timeMillis: Long): String {
    val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    return formatter.format(Date(timeMillis))
}

private fun formatPercent(value: Double): String {
    return String.format(Locale.getDefault(), "%.1f", value)
}