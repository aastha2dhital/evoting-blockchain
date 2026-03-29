package com.example.evotingmobileapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.evotingmobileapp.admin.AdminViewModel
import com.example.evotingmobileapp.model.Election
import com.example.evotingmobileapp.navigation.AppRoutes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    navController: NavHostController,
    adminViewModel: AdminViewModel
) {
    val elections by adminViewModel.elections.collectAsState()
    val latestElection = elections.lastOrNull()

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
                text = "Admin Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Manage elections, monitor voter activity, and continue the full voting flow from here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            DashboardActions(navController = navController)

            if (latestElection == null) {
                EmptyElectionState(
                    onCreateElectionClick = {
                        navController.navigate(AppRoutes.CREATE_ELECTION)
                    }
                )
            } else {
                ElectionOverviewCard(
                    sectionTitle = "Latest Election Overview",
                    election = latestElection
                )
            }

            if (elections.isNotEmpty()) {
                Text(
                    text = "All Elections",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                elections
                    .asReversed()
                    .forEach { election ->
                        ElectionOverviewCard(
                            sectionTitle = election.title,
                            election = election
                        )
                    }
            }
        }
    }
}

@Composable
private fun DashboardActions(
    navController: NavHostController
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = { navController.navigate(AppRoutes.CREATE_ELECTION) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Election")
        }

        Button(
            onClick = { navController.navigate(AppRoutes.QR_CHECK_IN) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("QR Check-In")
        }

        OutlinedButton(
            onClick = { navController.navigate(AppRoutes.VOTING) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Vote Now")
        }

        OutlinedButton(
            onClick = { navController.navigate(AppRoutes.RESULTS) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Results")
        }

        OutlinedButton(
            onClick = { navController.navigate(AppRoutes.BLOCKCHAIN_RECORDS) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Blockchain Records")
        }
    }
}

@Composable
private fun EmptyElectionState(
    onCreateElectionClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "No elections available yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Create your first election to begin whitelist setup, QR check-in, voting, and receipt tracking.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TextButton(onClick = onCreateElectionClick) {
                Text("Create Election")
            }
        }
    }
}

@Composable
private fun ElectionOverviewCard(
    sectionTitle: String,
    election: Election
) {
    val statusText = when {
        election.isClosed() -> "Closed"
        election.isActive() -> "Active"
        else -> "Scheduled"
    }

    val eligibleCount = election.eligibleVoterIds.size
    val checkedInCount = election.checkedInVoterIds.size
    val voteCount = election.voteCounts.values.sum()
    val candidateCount = election.candidates.size

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = sectionTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            StatusBadge(statusText = statusText)

            DetailRow(label = "Election ID", value = election.id)
            DetailRow(label = "Candidates", value = candidateCount.toString())
            DetailRow(label = "Eligible Voters", value = eligibleCount.toString())
            DetailRow(label = "Checked-In Voters", value = checkedInCount.toString())
            DetailRow(label = "Votes Cast", value = voteCount.toString())
            DetailRow(label = "Starts", value = formatDashboardDateTime(election.startTimeMillis))
            DetailRow(label = "Ends", value = formatDashboardDateTime(election.endTimeMillis))
        }
    }
}

@Composable
private fun StatusBadge(
    statusText: String
) {
    val containerColor = when (statusText) {
        "Active" -> MaterialTheme.colorScheme.primaryContainer
        "Closed" -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer
    }

    val contentColor = when (statusText) {
        "Active" -> MaterialTheme.colorScheme.onPrimaryContainer
        "Closed" -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onTertiaryContainer
    }

    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = statusText,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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

private fun formatDashboardDateTime(timeInMillis: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return formatter.format(Date(timeInMillis))
}