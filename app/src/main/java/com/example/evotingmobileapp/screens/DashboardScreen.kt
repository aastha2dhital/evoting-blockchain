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
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.evotingmobileapp.admin.AdminViewModel
import com.example.evotingmobileapp.model.Election
import com.example.evotingmobileapp.navigation.AppRoutes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class DashboardMode {
    ADMIN,
    VOTER
}

@Composable
fun DashboardScreen(
    navController: NavHostController,
    adminViewModel: AdminViewModel
) {
    val elections by adminViewModel.elections.collectAsState()
    val latestElection = elections.lastOrNull()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val dashboardMode = when (currentRoute) {
        AppRoutes.VOTER_DASHBOARD -> DashboardMode.VOTER
        AppRoutes.ADMIN_DASHBOARD, AppRoutes.DASHBOARD -> DashboardMode.ADMIN
        else -> DashboardMode.ADMIN
    }

    val title = when (dashboardMode) {
        DashboardMode.ADMIN -> "Admin Control Center"
        DashboardMode.VOTER -> "Voter Dashboard"
    }

    val subtitle = when (dashboardMode) {
        DashboardMode.ADMIN ->
            "Create elections, perform voter QR check-in, review turnout, and manage blockchain-backed election activity."
        DashboardMode.VOTER ->
            "Review available elections, cast your vote once you have been checked in by the polling officer, and verify your receipt."
    }

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
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            when (dashboardMode) {
                DashboardMode.ADMIN -> {
                    AdminDashboardActions(navController = navController)

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

                DashboardMode.VOTER -> {
                    VoterDashboardActions(navController = navController)

                    if (latestElection == null) {
                        VoterEmptyState()
                    } else {
                        VoterElectionCard(
                            election = latestElection
                        )
                    }

                    if (elections.isNotEmpty()) {
                        Text(
                            text = "Available Elections",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )

                        elections
                            .asReversed()
                            .forEach { election ->
                                VoterElectionCard(election = election)
                            }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminDashboardActions(
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

        Button(
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

        TextButton(
            onClick = {
                navController.navigate(AppRoutes.LOGIN) {
                    popUpTo(AppRoutes.LOGIN) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }
    }
}

@Composable
private fun VoterDashboardActions(
    navController: NavHostController
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = { navController.navigate(AppRoutes.VOTING) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Vote Now")
        }

        OutlinedButton(
            onClick = { navController.navigate(AppRoutes.RECEIPT) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Verify Receipt")
        }

        TextButton(
            onClick = {
                navController.navigate(AppRoutes.LOGIN) {
                    popUpTo(AppRoutes.LOGIN) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
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
                text = "Create your first election to begin whitelist setup, polling-officer QR check-in, voting, and receipt tracking.",
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
private fun VoterEmptyState() {
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
                text = "No elections available right now",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "When an election is available, the polling officer will check you in before you cast your vote and verify your receipt.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
private fun VoterElectionCard(
    election: Election
) {
    val statusText = when {
        election.isClosed() -> "Closed"
        election.isActive() -> "Active"
        else -> "Scheduled"
    }

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
                text = election.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            StatusBadge(statusText = statusText)

            DetailRow(label = "Election ID", value = election.id)
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