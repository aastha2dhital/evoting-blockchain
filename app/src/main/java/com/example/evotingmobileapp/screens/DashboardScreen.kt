package com.example.evotingmobileapp.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    val authUiState by authSessionViewModel.uiState.collectAsState()
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
        DashboardMode.ADMIN -> "Manage elections, check-ins, results, and blockchain records."
        DashboardMode.VOTER -> "Review available elections and continue to verified voting."
    }

    val onLogout = {
        authSessionViewModel.disconnectWallet()
        navController.navigate(AppRoutes.LOGIN) {
            popUpTo(AppRoutes.LOGIN) { inclusive = true }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.14f)
                        )
                    )
                )
        ) {
            DashboardBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                DashboardTopBar(
                    dashboardMode = dashboardMode,
                    walletAddress = authUiState.walletAddress
                )

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
                            SectionHeading(
                                title = stringResource(R.string.dashboard_latest_overview_title),
                                subtitle = "Latest election summary."
                            )

                            ElectionOverviewCard(
                                sectionTitle = latestElection.title,
                                election = latestElection,
                                emphasize = true
                            )
                        }

                        if (elections.isNotEmpty()) {
                            SectionHeading(
                                title = stringResource(R.string.dashboard_all_elections_title),
                                subtitle = "All created elections."
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
                            SectionHeading(
                                title = stringResource(R.string.dashboard_available_elections_title),
                                subtitle = "Election currently available to voters."
                            )

                            VoterElectionCard(
                                election = latestElection,
                                emphasize = true
                            )
                        }

                        if (elections.isNotEmpty()) {
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

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun DashboardBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .size(210.dp)
                .align(Alignment.TopEnd)
                .offset(x = 82.dp, y = 34.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)
        ) {}

        Surface(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.TopStart)
                .offset(x = (-62).dp, y = 260.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.07f)
        ) {}

        Surface(
            modifier = Modifier
                .size(165.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 66.dp, y = (-110).dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.09f)
        ) {}
    }
}

@Composable
private fun DashboardTopBar(
    dashboardMode: DashboardMode,
    walletAddress: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        tonalElevation = 4.dp,
        shadowElevation = 7.dp,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 5.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = when (dashboardMode) {
                                DashboardMode.ADMIN -> "AD"
                                DashboardMode.VOTER -> "VT"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = when (dashboardMode) {
                            DashboardMode.ADMIN -> "Admin Dashboard"
                            DashboardMode.VOTER -> "Voter Dashboard"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = if (walletAddress.isBlank()) {
                            "Secure session"
                        } else {
                            shortenDashboardText(walletAddress)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                )
            ) {
                Text(
                    text = when (dashboardMode) {
                        DashboardMode.ADMIN -> "Admin"
                        DashboardMode.VOTER -> "Voter"
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(34.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 11.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.76f),
                            Color.White,
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.34f)
                        )
                    )
                )
                .padding(22.dp)
        ) {
            HeroDecorationLayer()

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(17.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                    )
                ) {
                    Text(
                        text = when (dashboardMode) {
                            DashboardMode.ADMIN -> "CONTROL CENTER"
                            DashboardMode.VOTER -> "VOTER ACCESS"
                        },
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HeroMetricBox(
                        modifier = Modifier.weight(1f),
                        value = electionsCount.toString(),
                        label = "Elections"
                    )

                    HeroMetricBox(
                        modifier = Modifier.weight(1f),
                        value = when (dashboardMode) {
                            DashboardMode.ADMIN -> "Admin"
                            DashboardMode.VOTER -> "Voter"
                        },
                        label = "Role"
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
private fun HeroDecorationLayer() {
    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .size(126.dp)
                .align(Alignment.TopEnd)
                .offset(x = 44.dp, y = (-56).dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)
        ) {}

        Surface(
            modifier = Modifier
                .size(90.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-44).dp, y = 128.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.07f)
        ) {}
    }
}

@Composable
private fun HeroMetricBox(
    modifier: Modifier = Modifier,
    value: String,
    label: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.86f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)
        ),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun HeroPill(
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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
        subtitle = "Main election controls."
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionTile(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.dashboard_create_election),
                subtitle = "New election",
                icon = "+",
                primary = true,
                onClick = { navController.navigate(AppRoutes.CREATE_ELECTION) }
            )

            ActionTile(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.dashboard_qr_checkin),
                subtitle = "Check voters",
                icon = "QR",
                primary = false,
                onClick = { navController.navigate(AppRoutes.QR_CHECK_IN) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionTile(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.dashboard_view_results),
                subtitle = "Winner totals",
                icon = "WIN",
                primary = false,
                onClick = { navController.navigate(AppRoutes.RESULTS) }
            )

            ActionTile(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.dashboard_blockchain_records),
                subtitle = "Audit trail",
                icon = "TX",
                primary = false,
                onClick = { navController.navigate(AppRoutes.BLOCKCHAIN_RECORDS) }
            )
        }

        LogoutButton(onLogout = onLogout)
    }
}

@Composable
private fun VoterDashboardActions(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    SectionHeading(
        title = stringResource(R.string.dashboard_quick_actions_title),
        subtitle = "Continue to voting or verify a receipt."
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DashboardPrimaryActionButton(
            text = stringResource(R.string.dashboard_vote_now),
            icon = "OK",
            onClick = { navController.navigate(AppRoutes.VOTING) }
        )

        OutlinedButton(
            onClick = { navController.navigate(AppRoutes.RECEIPT) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.34f)
            )
        ) {
            Text(
                text = stringResource(R.string.dashboard_verify_receipt),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black
            )
        }

        LogoutButton(onLogout = onLogout)
    }
}

@Composable
private fun ActionTile(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: String,
    primary: Boolean,
    onClick: () -> Unit
) {
    val accentColor = if (primary) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondary
    }

    Card(
        modifier = modifier
            .height(138.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = accentColor.copy(alpha = 0.23f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(15.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(18.dp),
                color = accentColor,
                shadowElevation = 5.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = icon,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DashboardPrimaryActionButton(
    text: String,
    icon: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 5.dp)
    ) {
        Text(
            text = "$icon  $text",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun LogoutButton(
    onLogout: () -> Unit
) {
    TextButton(
        onClick = onLogout,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.dashboard_logout),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun SectionHeading(
    title: String,
    subtitle: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Black
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
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.24f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.38f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.dashboard_empty_admin_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black
            )

            Text(
                text = stringResource(R.string.dashboard_empty_admin_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onCreateElectionClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = stringResource(R.string.dashboard_create_election),
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun VoterEmptyState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.24f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.36f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.dashboard_empty_voter_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black
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
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (emphasize) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.23f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.26f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (emphasize) 8.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            if (emphasize) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.32f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
                            },
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElectionCardHeader(
                title = sectionTitle,
                subtitle = "Blockchain election overview",
                status = status
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricChip(
                    label = stringResource(R.string.dashboard_candidates),
                    value = candidateCount.toString(),
                    modifier = Modifier.weight(1f),
                    strong = emphasize
                )

                MetricChip(
                    label = stringResource(R.string.dashboard_eligible),
                    value = eligibleCount.toString(),
                    modifier = Modifier.weight(1f),
                    strong = emphasize
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricChip(
                    label = stringResource(R.string.dashboard_checked_in),
                    value = checkedInCount.toString(),
                    modifier = Modifier.weight(1f),
                    strong = emphasize
                )

                MetricChip(
                    label = stringResource(R.string.dashboard_votes),
                    value = voteCount.toString(),
                    modifier = Modifier.weight(1f),
                    strong = emphasize
                )
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
    val candidateCount = election.candidates.size
    val totalVotes = election.voteCounts.values.sum()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (emphasize) {
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.23f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.26f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (emphasize) 8.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            if (emphasize) {
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.30f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
                            },
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElectionCardHeader(
                title = election.title,
                subtitle = stringResource(R.string.dashboard_review_timing),
                status = status
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricChip(
                    label = stringResource(R.string.dashboard_candidates),
                    value = candidateCount.toString(),
                    modifier = Modifier.weight(1f),
                    strong = emphasize
                )

                MetricChip(
                    label = stringResource(R.string.dashboard_votes),
                    value = totalVotes.toString(),
                    modifier = Modifier.weight(1f),
                    strong = emphasize
                )
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
private fun ElectionCardHeader(
    title: String,
    subtitle: String,
    status: DashboardElectionStatus
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        StatusBadge(status = status)
    }
}

@Composable
private fun MetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    strong: Boolean
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = if (strong) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.70f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.76f)
        },
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.20f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = if (strong) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (strong) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.76f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(
            width = 1.dp,
            color = contentColor.copy(alpha = 0.12f)
        )
    ) {
        Text(
            text = statusText,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            fontWeight = FontWeight.Black,
            maxLines = 1
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
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

private fun shortenDashboardText(value: String): String {
    if (value.length <= 18) return value
    return "${value.take(10)}...${value.takeLast(8)}"
}