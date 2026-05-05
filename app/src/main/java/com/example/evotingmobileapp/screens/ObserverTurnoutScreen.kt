package com.example.evotingmobileapp.screens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

private val ObserverNavy = Color(0xFF07133A)
private val ObserverDeepBlue = Color(0xFF102A70)
private val ObserverIndigo = Color(0xFF4F32F6)
private val ObserverPurple = Color(0xFF7C3AED)
private val ObserverBlue = Color(0xFF1479FF)
private val ObserverTeal = Color(0xFF00B8A9)
private val ObserverCoral = Color(0xFFFF5C7A)
private val ObserverAmber = Color(0xFFFFB020)
private val ObserverGreen = Color(0xFF17C964)
private val ObserverCard = Color(0xFFF6F8FF)
private val ObserverPanel = Color(0xFFEAF1FF)
private val ObserverInk = Color(0xFF081229)
private val ObserverMuted = Color(0xFF59627E)
private val ObserverLine = Color(0xFFD6DDF4)

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
                        ObserverNavy,
                        ObserverDeepBlue,
                        Color(0xFF3155D8),
                        Color(0xFFD8ECFF)
                    )
                )
            )
    ) {
        ObserverDecorativeBackground()

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
            ObserverHeroCard(
                electionCount = elections.size,
                activeCount = elections.count { it.isActive(now) },
                closedCount = elections.count { it.isClosed(now) }
            )

            ObserverReadOnlyNotice()

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

            ObserverFooterActions(
                onBack = { navController.popBackStack() },
                onRefresh = {
                    adminViewModel.refreshBlockchainData()
                    elections.forEach { election ->
                        adminViewModel.loadTurnoutCount(election.id)
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ObserverDecorativeBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .size(230.dp)
                .offset(x = (-90).dp, y = 40.dp),
            shape = CircleShape,
            color = ObserverTeal.copy(alpha = 0.16f)
        ) {}

        Surface(
            modifier = Modifier
                .size(190.dp)
                .align(Alignment.TopEnd)
                .offset(x = 72.dp, y = 88.dp),
            shape = CircleShape,
            color = ObserverCoral.copy(alpha = 0.16f)
        ) {}

        Surface(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 98.dp, y = 36.dp),
            shape = CircleShape,
            color = ObserverAmber.copy(alpha = 0.14f)
        ) {}

        Surface(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-130).dp, y = 110.dp),
            shape = CircleShape,
            color = ObserverPurple.copy(alpha = 0.15f)
        ) {}
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
                color = ObserverTeal.copy(alpha = 0.12f)
            ) {}

            Surface(
                modifier = Modifier
                    .size(78.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 30.dp, y = 24.dp),
                shape = CircleShape,
                color = ObserverCoral.copy(alpha = 0.10f)
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
                        color = ObserverIndigo,
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
                            color = ObserverIndigo,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Public turnout monitor",
                            style = MaterialTheme.typography.headlineSmall,
                            color = ObserverInk,
                            fontWeight = FontWeight.Black
                        )
                    }

                    HeaderChip(text = "OBSERVER")
                }

                Text(
                    text = "Read-only public visibility for turnout and closed election summaries. Admin-only actions are not available here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ObserverMuted
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ObserverMetricTile(
                        modifier = Modifier.weight(1f),
                        label = "Elections",
                        value = electionCount.toString(),
                        accent = ObserverBlue
                    )

                    ObserverMetricTile(
                        modifier = Modifier.weight(1f),
                        label = "Active",
                        value = activeCount.toString(),
                        accent = ObserverTeal
                    )

                    ObserverMetricTile(
                        modifier = Modifier.weight(1f),
                        label = "Closed",
                        value = closedCount.toString(),
                        accent = ObserverCoral
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
        color = ObserverCoral.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, ObserverCoral.copy(alpha = 0.30f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelSmall,
            color = ObserverCoral,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun ObserverMetricTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = accent.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.20f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = ObserverInk,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = ObserverMuted,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ObserverReadOnlyNotice() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.18f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = ObserverTeal.copy(alpha = 0.20f),
                border = BorderStroke(1.dp, ObserverTeal.copy(alpha = 0.42f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = "Transparent observer mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                Text(
                    text = "This page refreshes blockchain election data and shows turnout without allowing voting, check-in, or admin changes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.84f)
                )
            }
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

    ModernObserverCard {
        ObserverElectionHeader(
            election = election,
            statusText = statusText,
            closed = closed,
            active = active
        )

        HorizontalDivider(color = ObserverLine.copy(alpha = 0.80f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TurnoutTile(
                modifier = Modifier.weight(1f),
                label = "Eligible",
                value = eligibleCount.toString(),
                detail = "registered",
                accent = ObserverBlue
            )

            TurnoutTile(
                modifier = Modifier.weight(1f),
                label = "Checked-in",
                value = checkedInCount.toString(),
                detail = "${formatPercent(checkInPercent)}%",
                accent = ObserverTeal
            )

            TurnoutTile(
                modifier = Modifier.weight(1f),
                label = "Votes",
                value = votesCast.toString(),
                detail = "${formatPercent(turnoutPercent)}%",
                accent = ObserverCoral
            )
        }

        TurnoutProgressPanel(
            checkedInPercent = checkInPercent,
            turnoutPercent = turnoutPercent
        )

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

@Composable
private fun ObserverElectionHeader(
    election: Election,
    statusText: String,
    closed: Boolean,
    active: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(54.dp),
            shape = RoundedCornerShape(18.dp),
            color = when {
                closed -> ObserverGreen.copy(alpha = 0.14f)
                active -> ObserverTeal.copy(alpha = 0.14f)
                else -> ObserverAmber.copy(alpha = 0.16f)
            },
            border = BorderStroke(
                1.dp,
                when {
                    closed -> ObserverGreen.copy(alpha = 0.30f)
                    active -> ObserverTeal.copy(alpha = 0.30f)
                    else -> ObserverAmber.copy(alpha = 0.32f)
                }
            )
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = election.id.take(3).uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelLarge,
                    color = when {
                        closed -> ObserverGreen
                        active -> ObserverTeal
                        else -> ObserverAmber
                    },
                    fontWeight = FontWeight.Black,
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
                color = ObserverInk,
                fontWeight = FontWeight.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${formatDate(election.startTimeMillis)} to ${formatDate(election.endTimeMillis)}",
                style = MaterialTheme.typography.bodySmall,
                color = ObserverMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        StatusChip(
            text = statusText,
            closed = closed,
            active = active
        )
    }
}

@Composable
private fun StatusChip(
    text: String,
    closed: Boolean,
    active: Boolean
) {
    val accent = when {
        closed -> ObserverGreen
        active -> ObserverTeal
        else -> ObserverAmber
    }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = accent.copy(alpha = 0.13f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.30f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Black,
            color = accent
        )
    }
}

@Composable
private fun TurnoutTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    detail: String,
    accent: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = accent.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.20f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = ObserverInk,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = ObserverMuted,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = detail,
                style = MaterialTheme.typography.labelSmall,
                color = accent,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TurnoutProgressPanel(
    checkedInPercent: Double,
    turnoutPercent: Double
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = ObserverIndigo.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, ObserverIndigo.copy(alpha = 0.20f))
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Live turnout progress",
                style = MaterialTheme.typography.titleSmall,
                color = ObserverInk,
                fontWeight = FontWeight.Black
            )

            ProgressLine(
                label = "QR check-in rate",
                percent = checkedInPercent,
                accent = ObserverTeal
            )

            ProgressLine(
                label = "Vote submission rate",
                percent = turnoutPercent,
                accent = ObserverCoral
            )
        }
    }
}

@Composable
private fun ProgressLine(
    label: String,
    percent: Double,
    accent: Color
) {
    val progressTarget = (percent / 100.0).coerceIn(0.0, 1.0).toFloat()
    val progress by animateFloatAsState(
        targetValue = progressTarget,
        label = "observerTurnoutProgress"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = ObserverInk,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${formatPercent(percent)}%",
                style = MaterialTheme.typography.labelLarge,
                color = accent,
                fontWeight = FontWeight.Black
            )
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(RoundedCornerShape(999.dp)),
            color = accent,
            trackColor = Color.White.copy(alpha = 0.78f)
        )
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
        color = ObserverTeal.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, ObserverTeal.copy(alpha = 0.24f))
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(
                text = "Turnout summary",
                style = MaterialTheme.typography.titleSmall,
                color = ObserverInk,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "Checked-in voters: $checkedInCount of $eligibleCount. Votes cast: $votesCast of $eligibleCount.",
                style = MaterialTheme.typography.bodyMedium,
                color = ObserverInk,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Observer mode is read-only and does not provide admin, check-in, voting, or closing controls.",
                style = MaterialTheme.typography.bodySmall,
                color = ObserverMuted
            )
        }
    }
}

@Composable
private fun HiddenResultsPanel() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = ObserverAmber.copy(alpha = 0.13f),
        border = BorderStroke(1.dp, ObserverAmber.copy(alpha = 0.26f))
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            horizontalArrangement = Arrangement.spacedBy(11.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = ObserverAmber.copy(alpha = 0.18f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "!",
                        style = MaterialTheme.typography.labelLarge,
                        color = ObserverAmber,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Results hidden",
                    style = MaterialTheme.typography.labelLarge,
                    color = ObserverInk,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "Candidate results remain hidden until this election is closed.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ObserverMuted,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ResultPreviewPanel(
    voteCounts: Map<String, Int>,
    winnerSummary: ObserverWinnerSummary
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = ObserverIndigo.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, ObserverIndigo.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Closed election result",
                style = MaterialTheme.typography.titleMedium,
                color = ObserverInk,
                fontWeight = FontWeight.Black
            )

            WinnerObserverPanel(winnerSummary = winnerSummary)

            if (voteCounts.isEmpty()) {
                Text(
                    text = "No vote totals are available yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ObserverMuted,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                voteCounts.entries
                    .sortedWith(
                        compareByDescending<Map.Entry<String, Int>> { it.value }
                            .thenBy { it.key.lowercase(Locale.getDefault()) }
                    )
                    .forEachIndexed { index, entry ->
                        ResultPreviewRow(
                            rank = index + 1,
                            candidate = entry.key,
                            votes = entry.value,
                            isWinner = winnerSummary.hasVotes &&
                                    winnerSummary.winnerNames.any {
                                        it.equals(entry.key, ignoreCase = true)
                                    },
                            isTie = winnerSummary.isTie
                        )
                    }
            }
        }
    }
}

@Composable
private fun WinnerObserverPanel(
    winnerSummary: ObserverWinnerSummary
) {
    val message = when {
        !winnerSummary.hasVotes ->
            "No votes were recorded for this election."

        winnerSummary.isTie ->
            "${winnerSummary.winnerNames.joinToString(", ")} tied with ${winnerSummary.winningVotes} ${voteWord(winnerSummary.winningVotes)} each."

        else ->
            "${winnerSummary.winnerNames.first()} won with ${winnerSummary.winningVotes} ${voteWord(winnerSummary.winningVotes)}."
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = if (winnerSummary.hasVotes) {
            ObserverGreen.copy(alpha = 0.13f)
        } else {
            ObserverPanel.copy(alpha = 0.86f)
        },
        border = BorderStroke(
            1.dp,
            if (winnerSummary.hasVotes) {
                ObserverGreen.copy(alpha = 0.30f)
            } else {
                ObserverLine.copy(alpha = 0.72f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = if (winnerSummary.hasVotes) ObserverGreen else ObserverIndigo.copy(alpha = 0.14f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (winnerSummary.hasVotes) "1" else "-",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (winnerSummary.hasVotes) Color.White else ObserverIndigo,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = if (winnerSummary.isTie) {
                        "Joint winners"
                    } else if (winnerSummary.hasVotes) {
                        "Winner"
                    } else {
                        "No winner yet"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = ObserverInk,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ObserverMuted,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ResultPreviewRow(
    rank: Int,
    candidate: String,
    votes: Int,
    isWinner: Boolean,
    isTie: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = if (isWinner) ObserverGreen.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.76f),
        border = BorderStroke(
            1.dp,
            if (isWinner) ObserverGreen.copy(alpha = 0.26f) else ObserverLine.copy(alpha = 0.72f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = if (isWinner) ObserverGreen else ObserverIndigo.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = rank.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isWinner) Color.White else ObserverIndigo,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = candidate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ObserverInk,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = if (isWinner) {
                        if (isTie) "Joint winner" else "Winner"
                    } else {
                        "$votes ${voteWord(votes)}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isWinner) ObserverGreen else ObserverMuted,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "$votes",
                style = MaterialTheme.typography.titleMedium,
                color = ObserverIndigo,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun EmptyObserverState() {
    ModernObserverCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = CircleShape,
                color = ObserverIndigo.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, ObserverIndigo.copy(alpha = 0.22f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "OBS",
                        style = MaterialTheme.typography.titleMedium,
                        color = ObserverIndigo,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Text(
                text = "No elections available",
                style = MaterialTheme.typography.titleLarge,
                color = ObserverInk,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Once an election is created, public turnout summaries will appear here.",
                style = MaterialTheme.typography.bodyMedium,
                color = ObserverMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ObserverFooterActions(
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .weight(1f)
                .height(54.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(
                text = "Back",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Button(
            onClick = onRefresh,
            modifier = Modifier
                .weight(1f)
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ObserverCoral),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 5.dp)
        ) {
            Text(
                text = "Refresh",
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun ModernObserverCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = ObserverCard.copy(alpha = 0.96f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            content = content
        )
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