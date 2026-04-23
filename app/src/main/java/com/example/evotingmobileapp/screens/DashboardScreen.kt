package com.example.evotingmobileapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.evotingmobileapp.admin.AdminViewModel
import com.example.evotingmobileapp.auth.AuthSessionViewModel
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
    adminViewModel: AdminViewModel,
    authSessionViewModel: AuthSessionViewModel
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
            "Manage elections, complete QR check-in, monitor turnout, and review blockchain-backed election activity."
        DashboardMode.VOTER ->
            "Review active elections, vote after polling-officer check-in, and verify your blockchain receipt."
    }

    val onLogout = {
        authSessionViewModel.disconnectWallet()
        navController.navigate(AppRoutes.LOGIN) {
            popUpTo(AppRoutes.LOGIN) { inclusive = true }
        }
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            DashboardHeroCard(
                title = title,
                subtitle = subtitle,
                electionsCount = elections.size,
                latestElectionTitle = latestElection?.title,
                dashboardMode = dashboardMode
            )

            when (dashboardMode) {
                DashboardMode.ADMIN -> {
                    AdminDashboardActions(
                        navController = navController,
                        onLogout = onLogout
                    )

                    if (latestElection == null) {
                        EmptyElectionState(
                            onCreateElectionClick = {
                                navController.navigate(AppRoutes.CREATE_ELECTION)
                            }
                        )
                    } else {
                        ElectionOverviewCard(
                            sectionTitle = "Latest Election Overview",
                            election = latestElection,
                            emphasize = true
                        )
                    }

                    if (elections.isNotEmpty()) {
                        SectionHeading(
                            title = "All Elections",
                            subtitle = "Review created elections, status, turnout, and schedule."
                        )

                        elections
                            .asReversed()
                            .forEach { election ->
                                ElectionOverviewCard(
                                    sectionTitle = election.title,
                                    election = election,
                                    emphasize = false
                                )
                            }
                    }
                }

                DashboardMode.VOTER -> {
                    VoterDashboardActions(
                        navController = navController,
                        onLogout = onLogout
                    )

                    if (latestElection == null) {
                        VoterEmptyState()
                    } else {
                        VoterElectionCard(
                            election = latestElection,
                            emphasize = true
                        )
                    }

                    if (elections.isNotEmpty()) {
                        SectionHeading(
                            title = "Available Elections",
                            subtitle = "Review election timing and status before you proceed to vote."
                        )

                        elections
                            .asReversed()
                            .forEach { election ->
                                VoterElectionCard(
                                    election = election,
                                    emphasize = false
                                )
                            }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DashboardHeroCard(
    title: String,
    subtitle: String,
    electionsCount: Int,
    latestElectionTitle: String?,
    dashboardMode: DashboardMode
) {
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HeroPill(
                    label = if (dashboardMode == DashboardMode.ADMIN) "Role: Admin" else "Role: Voter"
                )
                HeroPill(label = "Elections: $electionsCount")
                HeroPill(
                    label = latestElectionTitle?.let { "Latest: $it" } ?: "No elections yet"
                )
            }
        }
    }
}

@Composable
private fun HeroPill(
    label: String
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AdminDashboardActions(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    SectionHeading(
        title = "Quick Actions",
        subtitle = "Core administration tasks for election setup and monitoring."
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DashboardPrimaryActionButton(
            text = "Create Election",
            onClick = { navController.navigate(AppRoutes.CREATE_ELECTION) }
        )

        DashboardPrimaryActionButton(
            text = "QR Check-In",
            onClick = { navController.navigate(AppRoutes.QR_CHECK_IN) }
        )

        DashboardPrimaryActionButton(
            text = "View Results",
            onClick = { navController.navigate(AppRoutes.RESULTS) }
        )

        OutlinedButton(
            onClick = { navController.navigate(AppRoutes.BLOCKCHAIN_RECORDS) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(
                text = "Blockchain Records",
                style = MaterialTheme.typography.labelLarge
            )
        }

        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }
    }
}

@Composable
private fun VoterDashboardActions(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    SectionHeading(
        title = "Quick Actions",
        subtitle = "Proceed to cast your vote or verify your blockchain receipt."
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DashboardPrimaryActionButton(
            text = "Vote Now",
            onClick = { navController.navigate(AppRoutes.VOTING) }
        )

        OutlinedButton(
            onClick = { navController.navigate(AppRoutes.RECEIPT) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(
                text = "Verify Receipt",
                style = MaterialTheme.typography.labelLarge
            )
        }

        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }
    }
}

@Composable
private fun DashboardPrimaryActionButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(18.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun SectionHeading(
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
private fun EmptyElectionState(
    onCreateElectionClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
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
private fun VoterEmptyState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "No elections available right now",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "When an election becomes available, the polling officer will check you in before you vote and verify your receipt.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ElectionOverviewCard(
    sectionTitle: String,
    election: Election,
    emphasize: Boolean
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
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (emphasize) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (emphasize) 8.dp else 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = sectionTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Blockchain election overview",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusBadge(statusText = statusText)
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricChip(label = "Candidates", value = candidateCount.toString())
                MetricChip(label = "Eligible", value = eligibleCount.toString())
                MetricChip(label = "Checked-In", value = checkedInCount.toString())
                MetricChip(label = "Votes", value = voteCount.toString())
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            DetailRow(label = "Election ID", value = election.id)
            DetailRow(label = "Starts", value = formatDashboardDateTime(election.startTimeMillis))
            DetailRow(label = "Ends", value = formatDashboardDateTime(election.endTimeMillis))
        }
    }
}

@Composable
private fun VoterElectionCard(
    election: Election,
    emphasize: Boolean
) {
    val statusText = when {
        election.isClosed() -> "Closed"
        election.isActive() -> "Active"
        else -> "Scheduled"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (emphasize) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (emphasize) 8.dp else 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = election.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Review timing before voting",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusBadge(statusText = statusText)
            }

            DetailRow(label = "Election ID", value = election.id)
            DetailRow(label = "Starts", value = formatDashboardDateTime(election.startTimeMillis))
            DetailRow(label = "Ends", value = formatDashboardDateTime(election.endTimeMillis))
        }
    }
}

@Composable
private fun MetricChip(
    label: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
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
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = statusText,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DetailRow(
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