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

private val DashboardNavy = Color(0xFF07133A)
private val DashboardIndigo = Color(0xFF4F32F6)
private val DashboardPurple = Color(0xFF7C3AED)
private val DashboardBlue = Color(0xFF1479FF)
private val DashboardTeal = Color(0xFF00B8A9)
private val DashboardCoral = Color(0xFFFF5C7A)
private val DashboardAmber = Color(0xFFFFB020)
private val DashboardPanel = Color(0xFFEFF4FF)
private val DashboardCard = Color(0xFFF5F8FF)
private val DashboardInk = Color(0xFF0A102C)
private val DashboardMuted = Color(0xFF5B6380)

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
        containerColor = DashboardNavy
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DashboardNavy,
                            Color(0xFF172B78),
                            Color(0xFF3155D8),
                            Color(0xFFD8ECFF)
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
                    .padding(horizontal = 18.dp, vertical = 16.dp),
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
                .size(280.dp)
                .align(Alignment.TopEnd)
                .offset(x = 96.dp, y = (-46).dp),
            shape = CircleShape,
            color = DashboardPurple.copy(alpha = 0.42f)
        ) {}

        Surface(
            modifier = Modifier
                .size(230.dp)
                .align(Alignment.TopStart)
                .offset(x = (-94).dp, y = 190.dp),
            shape = CircleShape,
            color = DashboardTeal.copy(alpha = 0.34f)
        ) {}

        Surface(
            modifier = Modifier
                .size(210.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 78.dp, y = (-110).dp),
            shape = CircleShape,
            color = DashboardAmber.copy(alpha = 0.30f)
        ) {}

        Surface(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 78.dp, y = 80.dp),
            shape = CircleShape,
            color = DashboardCoral.copy(alpha = 0.20f)
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
        color = Color.White.copy(alpha = 0.92f),
        tonalElevation = 6.dp,
        shadowElevation = 12.dp,
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.65f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(17.dp),
                    color = Color.Transparent,
                    shadowElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier.background(
                            Brush.linearGradient(
                                colors = listOf(DashboardIndigo, DashboardBlue, DashboardPurple)
                            )
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (dashboardMode) {
                                DashboardMode.ADMIN -> "AD"
                                DashboardMode.VOTER -> "VT"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White
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
                        color = DashboardInk,
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
                        color = DashboardMuted,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = DashboardIndigo.copy(alpha = 0.12f),
                border = BorderStroke(
                    width = 1.dp,
                    color = DashboardIndigo.copy(alpha = 0.20f)
                )
            ) {
                Text(
                    text = when (dashboardMode) {
                        DashboardMode.ADMIN -> "Admin"
                        DashboardMode.VOTER -> "Voter"
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = DashboardIndigo,
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
        colors = CardDefaults.cardColors(containerColor = DashboardPanel),
        elevation = CardDefaults.cardElevation(defaultElevation = 18.dp),
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.70f)
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFE6E2FF),
                            Color(0xFFE8F6FF),
                            Color(0xFFE0FFF8)
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
                    color = DashboardIndigo.copy(alpha = 0.12f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = DashboardIndigo.copy(alpha = 0.24f)
                    )
                ) {
                    Text(
                        text = when (dashboardMode) {
                            DashboardMode.ADMIN -> "CONTROL CENTER"
                            DashboardMode.VOTER -> "VOTER ACCESS"
                        },
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = DashboardIndigo,
                        fontWeight = FontWeight.Black
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = DashboardInk,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DashboardMuted
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HeroMetricBox(
                        modifier = Modifier.weight(1f),
                        value = electionsCount.toString(),
                        label = "Elections",
                        accent = DashboardIndigo
                    )

                    HeroMetricBox(
                        modifier = Modifier.weight(1f),
                        value = when (dashboardMode) {
                            DashboardMode.ADMIN -> "Admin"
                            DashboardMode.VOTER -> "Voter"
                        },
                        label = "Role",
                        accent = DashboardTeal
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
                .size(138.dp)
                .align(Alignment.TopEnd)
                .offset(x = 52.dp, y = (-62).dp),
            shape = CircleShape,
            color = DashboardPurple.copy(alpha = 0.14f)
        ) {}

        Surface(
            modifier = Modifier
                .size(104.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-46).dp, y = 128.dp),
            shape = CircleShape,
            color = DashboardTeal.copy(alpha = 0.14f)
        ) {}
    }
}

@Composable
private fun HeroMetricBox(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    accent: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.68f),
        border = BorderStroke(
            width = 1.dp,
            color = accent.copy(alpha = 0.24f)
        ),
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = DashboardInk,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = DashboardMuted,
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
        color = DashboardIndigo.copy(alpha = 0.12f),
        border = BorderStroke(
            width = 1.dp,
            color = DashboardIndigo.copy(alpha = 0.22f)
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelLarge,
            color = DashboardIndigo,
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
                accent = DashboardIndigo,
                onClick = { navController.navigate(AppRoutes.CREATE_ELECTION) }
            )

            ActionTile(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.dashboard_qr_checkin),
                subtitle = "Check voters",
                icon = "QR",
                accent = DashboardTeal,
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
                accent = DashboardCoral,
                onClick = { navController.navigate(AppRoutes.RESULTS) }
            )

            ActionTile(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.dashboard_blockchain_records),
                subtitle = "Audit trail",
                icon = "TX",
                accent = DashboardAmber,
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
                width = 1.5.dp,
                color = Color.White.copy(alpha = 0.72f)
            )
        ) {
            Text(
                text = stringResource(R.string.dashboard_verify_receipt),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = Color.White
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
    accent: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(142.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = DashboardCard
        ),
        border = BorderStroke(
            width = 1.dp,
            color = accent.copy(alpha = 0.32f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 9.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.20f),
                            DashboardCard
                        )
                    )
                )
                .padding(15.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(18.dp),
                color = accent,
                shadowElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = icon,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = DashboardInk,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = DashboardMuted,
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
        colors = ButtonDefaults.buttonColors(
            containerColor = DashboardIndigo,
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
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
            color = Color.White,
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
            color = Color.White,
            fontWeight = FontWeight.Black
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.78f)
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
            containerColor = DashboardCard
        ),
        border = BorderStroke(
            width = 1.dp,
            color = DashboardAmber.copy(alpha = 0.34f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 9.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DashboardAmber.copy(alpha = 0.22f),
                            DashboardCard
                        )
                    )
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.dashboard_empty_admin_title),
                style = MaterialTheme.typography.titleLarge,
                color = DashboardInk,
                fontWeight = FontWeight.Black
            )

            Text(
                text = stringResource(R.string.dashboard_empty_admin_description),
                style = MaterialTheme.typography.bodyMedium,
                color = DashboardMuted
            )

            Button(
                onClick = onCreateElectionClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DashboardIndigo,
                    contentColor = Color.White
                )
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
            containerColor = DashboardCard
        ),
        border = BorderStroke(
            width = 1.dp,
            color = DashboardAmber.copy(alpha = 0.34f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 9.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DashboardAmber.copy(alpha = 0.22f),
                            DashboardCard
                        )
                    )
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.dashboard_empty_voter_title),
                style = MaterialTheme.typography.titleLarge,
                color = DashboardInk,
                fontWeight = FontWeight.Black
            )

            Text(
                text = stringResource(R.string.dashboard_empty_voter_description),
                style = MaterialTheme.typography.bodyMedium,
                color = DashboardMuted
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
            containerColor = DashboardCard
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (emphasize) {
                DashboardIndigo.copy(alpha = 0.34f)
            } else {
                Color.White.copy(alpha = 0.40f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (emphasize) 10.dp else 7.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            if (emphasize) {
                                DashboardIndigo.copy(alpha = 0.18f)
                            } else {
                                DashboardTeal.copy(alpha = 0.12f)
                            },
                            DashboardCard
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
                    accent = DashboardIndigo
                )

                MetricChip(
                    label = stringResource(R.string.dashboard_eligible),
                    value = eligibleCount.toString(),
                    modifier = Modifier.weight(1f),
                    accent = DashboardTeal
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
                    accent = DashboardCoral
                )

                MetricChip(
                    label = stringResource(R.string.dashboard_votes),
                    value = voteCount.toString(),
                    modifier = Modifier.weight(1f),
                    accent = DashboardAmber
                )
            }

            HorizontalDivider(color = DashboardIndigo.copy(alpha = 0.16f))

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
            containerColor = DashboardCard
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (emphasize) {
                DashboardTeal.copy(alpha = 0.34f)
            } else {
                Color.White.copy(alpha = 0.40f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (emphasize) 10.dp else 7.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            if (emphasize) {
                                DashboardTeal.copy(alpha = 0.18f)
                            } else {
                                DashboardIndigo.copy(alpha = 0.12f)
                            },
                            DashboardCard
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
                    accent = DashboardIndigo
                )

                MetricChip(
                    label = stringResource(R.string.dashboard_votes),
                    value = totalVotes.toString(),
                    modifier = Modifier.weight(1f),
                    accent = DashboardTeal
                )
            }

            HorizontalDivider(color = DashboardTeal.copy(alpha = 0.18f))

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
                color = DashboardInk,
                fontWeight = FontWeight.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = DashboardMuted
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
    accent: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = accent.copy(alpha = 0.12f),
        border = BorderStroke(
            width = 1.dp,
            color = accent.copy(alpha = 0.20f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = DashboardInk,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = DashboardMuted,
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
        DashboardElectionStatus.ACTIVE -> DashboardTeal.copy(alpha = 0.16f)
        DashboardElectionStatus.CLOSED -> DashboardCoral.copy(alpha = 0.16f)
        DashboardElectionStatus.SCHEDULED -> DashboardAmber.copy(alpha = 0.18f)
    }

    val contentColor = when (status) {
        DashboardElectionStatus.ACTIVE -> DashboardTeal
        DashboardElectionStatus.CLOSED -> DashboardCoral
        DashboardElectionStatus.SCHEDULED -> DashboardAmber.copy(alpha = 0.95f)
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
            color = contentColor.copy(alpha = 0.20f)
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
        color = Color.White.copy(alpha = 0.60f),
        border = BorderStroke(
            width = 1.dp,
            color = DashboardIndigo.copy(alpha = 0.10f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = DashboardIndigo,
                fontWeight = FontWeight.Black
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = DashboardInk,
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