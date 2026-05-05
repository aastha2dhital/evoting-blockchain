package com.example.evotingmobileapp.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val ResultsNavy = Color(0xFF07133A)
private val ResultsDeepBlue = Color(0xFF102A70)
private val ResultsIndigo = Color(0xFF4F32F6)
private val ResultsPurple = Color(0xFF7C3AED)
private val ResultsBlue = Color(0xFF1479FF)
private val ResultsTeal = Color(0xFF00B8A9)
private val ResultsCoral = Color(0xFFFF5C7A)
private val ResultsAmber = Color(0xFFFFB020)
private val ResultsGreen = Color(0xFF17C964)
private val ResultsCard = Color(0xFFF6F8FF)
private val ResultsPanel = Color(0xFFEAF1FF)
private val ResultsInk = Color(0xFF081229)
private val ResultsMuted = Color(0xFF59627E)
private val ResultsLine = Color(0xFFD6DDF4)

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

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ResultsNavy,
                            ResultsDeepBlue,
                            Color(0xFF3155D8),
                            Color(0xFFD8ECFF)
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            ResultsDecorativeBackground()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(
                    start = 18.dp,
                    end = 18.dp,
                    top = 16.dp,
                    bottom = 28.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    ResultsHeroCard(
                        electionCount = elections.size,
                        closedCount = elections.count { it.isClosed() },
                        openCount = elections.count { !it.isClosed() }
                    )
                }

                if (elections.isEmpty()) {
                    item {
                        EmptyResultsState()
                    }
                } else {
                    item {
                        ResultsOverviewCard(
                            totalElections = elections.size,
                            closedElections = elections.count { it.isClosed() },
                            totalVotes = elections.sumOf { election -> election.voteCounts.values.sum() }
                        )
                    }

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
}

@Composable
private fun ResultsDecorativeBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .size(230.dp)
                .offset(x = (-90).dp, y = 40.dp),
            shape = CircleShape,
            color = ResultsTeal.copy(alpha = 0.16f)
        ) {}

        Surface(
            modifier = Modifier
                .size(190.dp)
                .align(Alignment.TopEnd)
                .offset(x = 72.dp, y = 88.dp),
            shape = CircleShape,
            color = ResultsCoral.copy(alpha = 0.16f)
        ) {}

        Surface(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 98.dp, y = 36.dp),
            shape = CircleShape,
            color = ResultsAmber.copy(alpha = 0.14f)
        ) {}

        Surface(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-130).dp, y = 110.dp),
            shape = CircleShape,
            color = ResultsPurple.copy(alpha = 0.15f)
        ) {}
    }
}

@Composable
private fun ResultsHeroCard(
    electionCount: Int,
    closedCount: Int,
    openCount: Int
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
                color = ResultsTeal.copy(alpha = 0.12f)
            ) {}

            Surface(
                modifier = Modifier
                    .size(78.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 30.dp, y = 24.dp),
                shape = CircleShape,
                color = ResultsCoral.copy(alpha = 0.10f)
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
                        color = ResultsIndigo,
                        shadowElevation = 8.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "SV",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
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
                            color = ResultsIndigo,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Election results",
                            style = MaterialTheme.typography.headlineSmall,
                            color = ResultsInk,
                            fontWeight = FontWeight.Black
                        )
                    }

                    HeaderChip(text = "RESULTS")
                }

                Text(
                    text = "View turnout while voting is open. Final candidate results and winner details appear after the election closes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ResultsMuted
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MiniHeroTile(
                        modifier = Modifier.weight(1f),
                        title = electionCount.toString(),
                        subtitle = "Elections",
                        accent = ResultsBlue
                    )

                    MiniHeroTile(
                        modifier = Modifier.weight(1f),
                        title = openCount.toString(),
                        subtitle = "Open",
                        accent = ResultsTeal
                    )

                    MiniHeroTile(
                        modifier = Modifier.weight(1f),
                        title = closedCount.toString(),
                        subtitle = "Closed",
                        accent = ResultsCoral
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
        color = ResultsCoral.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, ResultsCoral.copy(alpha = 0.30f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = ResultsCoral
        )
    }
}

@Composable
private fun MiniHeroTile(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    accent: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = accent.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.20f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 11.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = ResultsInk,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = ResultsMuted,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ResultsOverviewCard(
    totalElections: Int,
    closedElections: Int,
    totalVotes: Int
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
                text = "Public results overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Text(
                text = "A transparent overview of elections, closed contests, turnout checks, and confirmed vote totals.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.82f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OverviewStep(
                    modifier = Modifier.weight(1f),
                    value = totalElections.toString(),
                    label = "Total",
                    accent = ResultsBlue
                )

                OverviewStep(
                    modifier = Modifier.weight(1f),
                    value = closedElections.toString(),
                    label = "Closed",
                    accent = ResultsCoral
                )

                OverviewStep(
                    modifier = Modifier.weight(1f),
                    value = totalVotes.toString(),
                    label = "Votes",
                    accent = ResultsTeal
                )
            }
        }
    }
}

@Composable
private fun OverviewStep(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    accent: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = accent.copy(alpha = 0.20f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.42f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.84f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyResultsState() {
    ModernResultsCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = CircleShape,
                color = ResultsIndigo.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, ResultsIndigo.copy(alpha = 0.22f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "0",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = ResultsIndigo
                    )
                }
            }

            Text(
                text = "No elections available",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = ResultsInk,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Create an election first. Results and turnout summaries will appear here.",
                style = MaterialTheme.typography.bodyMedium,
                color = ResultsMuted,
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

    ModernResultsCard {
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
                    .height(56.dp),
                enabled = !isClosing,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ResultsCoral),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text(
                    text = if (isClosing) {
                        "Closing election..."
                    } else {
                        "Close election early"
                    },
                    fontWeight = FontWeight.Black
                )
            }
        } else {
            FinalResultsPanel()

            WinnerPanel(winnerSummary = winnerSummary)

            HorizontalDivider(color = ResultsLine.copy(alpha = 0.80f))

            Text(
                text = "Candidate results",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = ResultsInk
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
        Surface(
            modifier = Modifier.size(54.dp),
            shape = RoundedCornerShape(18.dp),
            color = if (isClosed) ResultsGreen.copy(alpha = 0.14f) else ResultsAmber.copy(alpha = 0.16f),
            border = BorderStroke(
                1.dp,
                if (isClosed) ResultsGreen.copy(alpha = 0.30f) else ResultsAmber.copy(alpha = 0.32f)
            )
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (isClosed) "✓" else "⏳",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = if (isClosed) ResultsGreen else ResultsAmber,
                    textAlign = TextAlign.Center
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = ResultsInk
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = ResultsMuted,
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
            value = statusText,
            accent = if (statusText == "Closed") ResultsGreen else ResultsAmber
        )

        SummaryTile(
            modifier = Modifier.weight(1f),
            label = "Turnout",
            value = turnoutText,
            accent = ResultsTeal
        )

        SummaryTile(
            modifier = Modifier.weight(1f),
            label = "Votes",
            value = totalVotes.toString(),
            accent = ResultsBlue
        )
    }
}

@Composable
private fun SummaryTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
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
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = ResultsInk,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = ResultsMuted,
                fontWeight = FontWeight.Bold,
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
        shape = RoundedCornerShape(22.dp),
        color = ResultsIndigo.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, ResultsIndigo.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ResultInfoRow(
                label = "Start",
                value = startText
            )

            HorizontalDivider(color = ResultsLine.copy(alpha = 0.80f))

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

    val backgroundColors = if (winnerSummary.hasVotes) {
        listOf(
            ResultsIndigo,
            ResultsBlue,
            ResultsTeal
        )
    } else {
        listOf(
            Color(0xFFE6EAF7),
            Color(0xFFF5F7FC)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = if (winnerSummary.hasVotes) 9.dp else 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(backgroundColors),
                    shape = RoundedCornerShape(30.dp)
                )
        ) {
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 34.dp, y = (-36).dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.14f)
            ) {}

            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(62.dp),
                    shape = CircleShape,
                    color = if (winnerSummary.hasVotes) {
                        Color.White.copy(alpha = 0.20f)
                    } else {
                        ResultsIndigo.copy(alpha = 0.12f)
                    },
                    border = BorderStroke(
                        1.dp,
                        if (winnerSummary.hasVotes) {
                            Color.White.copy(alpha = 0.34f)
                        } else {
                            ResultsIndigo.copy(alpha = 0.22f)
                        }
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (winnerSummary.hasVotes) "🏆" else "-",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = if (winnerSummary.hasVotes) Color.White else ResultsIndigo
                        )
                    }
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (winnerSummary.hasVotes) Color.White else ResultsInk,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (winnerSummary.hasVotes) Color.White.copy(alpha = 0.92f) else ResultsMuted,
                    fontWeight = FontWeight.Bold,
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
        color = Color.White.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.26f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.84f),
                fontWeight = FontWeight.Bold,
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
        ResultsIndigo.copy(alpha = 0.10f)
    } else {
        ResultsPanel.copy(alpha = 0.86f)
    }

    val border = if (isWinner) {
        BorderStroke(1.4.dp, ResultsIndigo.copy(alpha = 0.45f))
    } else {
        BorderStroke(1.dp, ResultsLine.copy(alpha = 0.72f))
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
                    color = if (isWinner) ResultsIndigo else Color.White.copy(alpha = 0.88f),
                    border = BorderStroke(
                        1.dp,
                        if (isWinner) ResultsIndigo else ResultsLine
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = candidateSymbol,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = if (isWinner) Color.White else ResultsIndigo,
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
                        fontWeight = FontWeight.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = ResultsInk
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isWinner) {
                            SmallBadge(
                                text = if (isTie) "Joint winner" else "Winner",
                                accent = ResultsGreen
                            )
                        }

                        SmallBadge(
                            text = percentageText,
                            accent = ResultsBlue
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = voteCount.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = ResultsIndigo
                    )

                    Text(
                        text = voteWord(voteCount),
                        style = MaterialTheme.typography.labelMedium,
                        color = ResultsMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = if (isWinner) ResultsGreen else ResultsIndigo,
                trackColor = Color.White.copy(alpha = 0.78f)
            )
        }
    }
}

@Composable
private fun SmallBadge(
    text: String,
    accent: Color
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = accent.copy(alpha = 0.13f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.24f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = accent,
            fontWeight = FontWeight.Black
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
            color = ResultsIndigo,
            fontWeight = FontWeight.Black
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = ResultsInk,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StatusChip(
    text: String,
    isClosed: Boolean
) {
    val accent = if (isClosed) ResultsGreen else ResultsAmber

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = accent.copy(alpha = 0.13f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.30f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
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
    positive: Boolean
) {
    val accent = if (positive) ResultsGreen else ResultsAmber

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = accent.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.24f))
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = accent.copy(alpha = 0.16f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (positive) "✓" else "!",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = accent
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = ResultsInk,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ResultsMuted
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
        Text(
            text = "Back",
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun ModernResultsCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = ResultsCard.copy(alpha = 0.96f)),
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