package com.example.evotingmobileapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.evotingmobileapp.R
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

private enum class DashboardElectionStatus {
    ACTIVE,
    CLOSED,
    SCHEDULED
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
        DashboardMode.ADMIN -> stringResource(R.string.dashboard_admin_title)
        DashboardMode.VOTER -> stringResource(R.string.dashboard_voter_title)
    }

    val subtitle = when (dashboardMode) {
        DashboardMode.ADMIN -> stringResource(R.string.dashboard_admin_subtitle)
        DashboardMode.VOTER -> stringResource(R.string.dashboard_voter_subtitle)
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
                            sectionTitle = stringResource(R.string.dashboard_latest_overview_title),
                            election = latestElection,
                            emphasize = true
                        )
                    }

                    if (elections.isNotEmpty()) {
                        SectionHeading(
                            title = stringResource(R.string.dashboard_all_elections_title),
                            subtitle = stringResource(R.string.dashboard_all_elections_subtitle)
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
                            title = stringResource(R.string.dashboard_available_elections_title),
                            subtitle = stringResource(R.string.dashboard_available_elections_subtitle)
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

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HeroPill(
                        label = if (dashboardMode == DashboardMode.ADMIN) {
                            stringResource(R.string.dashboard_role_admin)
                        } else {
                            stringResource(R.string.dashboard_role_voter)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    HeroPill(
                        label = stringResource(R.string.dashboard_elections_count, electionsCount),
                        modifier = Modifier.weight(1f)
                    )
                }

                HeroPill(
                    label = latestElectionTitle?.let {
                        stringResource(R.string.dashboard_latest_election, it)
                    } ?: stringResource(R.string.dashboard_no_elections)
                )
            }
        }
    }
}

@Composable
private fun HeroPill(
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
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
        title = stringResource(R.string.dashboard_quick_actions_title),
        subtitle = stringResource(R.string.dashboard_admin_actions_subtitle)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DashboardPrimaryActionButton(
            text = stringResource(R.string.dashboard_create_election),
            onClick = { navController.navigate(AppRoutes.CREATE_ELECTION) }
        )

        DashboardPrimaryActionButton(
            text = stringResource(R.string.dashboard_qr_checkin),
            onClick = { navController.navigate(AppRoutes.QR_CHECK_IN) }
        )

        DashboardPrimaryActionButton(
            text = stringResource(R.string.dashboard_view_results),
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
                text = stringResource(R.string.dashboard_blockchain_records),
                style = MaterialTheme.typography.labelLarge
            )
        }

        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.dashboard_logout))
        }
    }
}

@Composable
private fun VoterDashboardActions(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    SectionHeading(
        title = stringResource(R.string.dashboard_quick_actions_title),
        subtitle = stringResource(R.string.dashboard_voter_actions_subtitle)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DashboardPrimaryActionButton(
            text = stringResource(R.string.dashboard_vote_now),
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
                text = stringResource(R.string.dashboard_verify_receipt),
                style = MaterialTheme.typography.labelLarge
            )
        }

        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.dashboard_logout))
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
                text = stringResource(R.string.dashboard_empty_admin_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.dashboard_empty_admin_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TextButton(onClick = onCreateElectionClick) {
                Text(stringResource(R.string.dashboard_create_election))
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
                text = stringResource(R.string.dashboard_empty_voter_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.dashboard_empty_voter_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ElectionOverviewCard(
    sectionTitle: String,
    election: Election,
    emphasize: Boolean
) {
    val status = dashboardStatusFor(election)

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
                        text = stringResource(R.string.dashboard_blockchain_election_overview),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusBadge(status = status)
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricChip(
                        label = stringResource(R.string.dashboard_candidates),
                        value = candidateCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        label = stringResource(R.string.dashboard_eligible),
                        value = eligibleCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricChip(
                        label = stringResource(R.string.dashboard_checked_in),
                        value = checkedInCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        label = stringResource(R.string.dashboard_votes),
                        value = voteCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            DetailRow(
                label = stringResource(R.string.dashboard_election_id),
                value = election.id
            )
            DetailRow(
                label = stringResource(R.string.dashboard_starts),
                value = formatDashboardDateTime(election.startTimeMillis)
            )
            DetailRow(
                label = stringResource(R.string.dashboard_ends),
                value = formatDashboardDateTime(election.endTimeMillis)
            )
        }
    }
}

@Composable
private fun VoterElectionCard(
    election: Election,
    emphasize: Boolean
) {
    val status = dashboardStatusFor(election)

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
                        text = stringResource(R.string.dashboard_review_timing),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusBadge(status = status)
            }

            DetailRow(
                label = stringResource(R.string.dashboard_election_id),
                value = election.id
            )
            DetailRow(
                label = stringResource(R.string.dashboard_starts),
                value = formatDashboardDateTime(election.startTimeMillis)
            )
            DetailRow(
                label = stringResource(R.string.dashboard_ends),
                value = formatDashboardDateTime(election.endTimeMillis)
            )
        }
    }
}

@Composable
private fun MetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
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
    status: DashboardElectionStatus
) {
    val containerColor = when (status) {
        DashboardElectionStatus.ACTIVE -> MaterialTheme.colorScheme.primaryContainer
        DashboardElectionStatus.CLOSED -> MaterialTheme.colorScheme.secondaryContainer
        DashboardElectionStatus.SCHEDULED -> MaterialTheme.colorScheme.tertiaryContainer
    }

    val contentColor = when (status) {
        DashboardElectionStatus.ACTIVE -> MaterialTheme.colorScheme.onPrimaryContainer
        DashboardElectionStatus.CLOSED -> MaterialTheme.colorScheme.onSecondaryContainer
        DashboardElectionStatus.SCHEDULED -> MaterialTheme.colorScheme.onTertiaryContainer
    }

    val statusText = when (status) {
        DashboardElectionStatus.ACTIVE -> stringResource(R.string.dashboard_status_active)
        DashboardElectionStatus.CLOSED -> stringResource(R.string.dashboard_status_closed)
        DashboardElectionStatus.SCHEDULED -> stringResource(R.string.dashboard_status_scheduled)
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

private fun dashboardStatusFor(election: Election): DashboardElectionStatus {
    return when {
        election.isClosed() -> DashboardElectionStatus.CLOSED
        election.isActive() -> DashboardElectionStatus.ACTIVE
        else -> DashboardElectionStatus.SCHEDULED
    }
}

private fun formatDashboardDateTime(timeInMillis: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return formatter.format(Date(timeInMillis))
}