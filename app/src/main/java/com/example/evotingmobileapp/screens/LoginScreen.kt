package com.example.evotingmobileapp.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.evotingmobileapp.auth.AuthSessionViewModel
import com.example.evotingmobileapp.navigation.AppRoutes

private val LoginNavy = Color(0xFF07133A)
private val LoginIndigo = Color(0xFF3F32E8)
private val LoginPurple = Color(0xFF7C3AED)
private val LoginBlue = Color(0xFF1479FF)
private val LoginTeal = Color(0xFF00C2B2)
private val LoginCoral = Color(0xFFFF5C7A)
private val LoginAmber = Color(0xFFFFB020)
private val LoginSoftPanel = Color(0xFFEFF4FF)
private val LoginSoftCard = Color(0xFFF5F8FF)
private val LoginInk = Color(0xFF0A102C)
private val LoginMuted = Color(0xFF5B6380)

@Composable
fun LoginScreen(
    navController: NavHostController,
    authSessionViewModel: AuthSessionViewModel,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        authSessionViewModel.disconnectWallet()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        LoginNavy,
                        Color(0xFF172B78),
                        Color(0xFF3155D8),
                        Color(0xFFD8ECFF)
                    )
                )
            )
    ) {
        ModernBackgroundDecorations()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(top = 16.dp, bottom = 116.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            TopBrandBar()

            MainHeroCard(
                onAdminClick = { navController.navigate(AppRoutes.ADMIN_LOGIN) },
                onVoterClick = { navController.navigate(AppRoutes.VOTER_ACCESS) },
                onObserverClick = { navController.navigate(AppRoutes.OBSERVER_TURNOUT) }
            )

            SectionHeader(
                title = "Access portals",
                subtitle = "Choose the correct role to continue."
            )

            RoleAccessGrid(
                onAdminClick = { navController.navigate(AppRoutes.ADMIN_LOGIN) },
                onVoterClick = { navController.navigate(AppRoutes.VOTER_ACCESS) },
                onObserverClick = { navController.navigate(AppRoutes.OBSERVER_TURNOUT) }
            )

            SectionHeader(
                title = "Election flow",
                subtitle = "Check in, vote, verify, and review results."
            )

            FeatureStack()
        }

        BottomHomeBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            onAdminClick = { navController.navigate(AppRoutes.ADMIN_LOGIN) },
            onVoterClick = { navController.navigate(AppRoutes.VOTER_ACCESS) },
            onObserverClick = { navController.navigate(AppRoutes.OBSERVER_TURNOUT) }
        )
    }
}

@Composable
private fun ModernBackgroundDecorations() {
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.TopEnd)
                .offset(x = 96.dp, y = (-46).dp),
            shape = CircleShape,
            color = LoginPurple.copy(alpha = 0.42f)
        ) {}

        Surface(
            modifier = Modifier
                .size(230.dp)
                .align(Alignment.TopStart)
                .offset(x = (-94).dp, y = 190.dp),
            shape = CircleShape,
            color = LoginTeal.copy(alpha = 0.36f)
        ) {}

        Surface(
            modifier = Modifier
                .size(210.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 78.dp, y = (-120).dp),
            shape = CircleShape,
            color = LoginAmber.copy(alpha = 0.30f)
        ) {}

        Surface(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 78.dp, y = 60.dp),
            shape = CircleShape,
            color = LoginCoral.copy(alpha = 0.20f)
        ) {}
    }
}

@Composable
private fun TopBrandBar() {
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
                BrandMark(text = "SV")

                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = "SecureVote Nepal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = LoginInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "Blockchain voting prototype",
                        style = MaterialTheme.typography.labelSmall,
                        color = LoginMuted,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = LoginIndigo.copy(alpha = 0.12f),
                border = BorderStroke(
                    width = 1.dp,
                    color = LoginIndigo.copy(alpha = 0.20f)
                )
            ) {
                Text(
                    text = "Nepal",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = LoginIndigo,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun BrandMark(text: String) {
    Surface(
        modifier = Modifier.size(44.dp),
        shape = RoundedCornerShape(17.dp),
        color = Color.Transparent,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier.background(
                Brush.linearGradient(
                    colors = listOf(LoginIndigo, LoginBlue, LoginPurple)
                )
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

@Composable
private fun MainHeroCard(
    onAdminClick: () -> Unit,
    onVoterClick: () -> Unit,
    onObserverClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(34.dp),
                ambientColor = LoginNavy.copy(alpha = 0.26f),
                spotColor = LoginNavy.copy(alpha = 0.30f)
            ),
        shape = RoundedCornerShape(34.dp),
        colors = CardDefaults.cardColors(
            containerColor = LoginSoftPanel
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.70f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
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
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = LoginIndigo.copy(alpha = 0.12f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = LoginIndigo.copy(alpha = 0.24f)
                    )
                ) {
                    Text(
                        text = "SECURE  VERIFIED  TRANSPARENT",
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = LoginIndigo,
                        fontWeight = FontWeight.Black
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        Text(
                            text = "Digital voting with verified access",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = LoginInk
                        )

                        Text(
                            text = "Create elections, check in voters, cast votes, and verify receipts using a local blockchain prototype.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LoginMuted
                        )
                    }

                    Surface(
                        modifier = Modifier.size(76.dp),
                        shape = RoundedCornerShape(26.dp),
                        color = Color.Transparent,
                        shadowElevation = 10.dp
                    ) {
                        Box(
                            modifier = Modifier.background(
                                Brush.linearGradient(
                                    colors = listOf(LoginIndigo, LoginBlue, LoginPurple)
                                )
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "SV",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }

                HeroMetricRow()

                Button(
                    onClick = onVoterClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LoginIndigo,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        text = "Continue as Voter",
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onAdminClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(
                            width = 1.5.dp,
                            color = LoginIndigo.copy(alpha = 0.55f)
                        )
                    ) {
                        Text(
                            text = "Admin",
                            color = LoginIndigo,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = onObserverClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(
                            width = 1.5.dp,
                            color = LoginTeal.copy(alpha = 0.65f)
                        )
                    ) {
                        Text(
                            text = "Observer",
                            color = LoginTeal.copy(alpha = 0.95f),
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
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
                .offset(x = 54.dp, y = (-66).dp),
            shape = CircleShape,
            color = LoginPurple.copy(alpha = 0.14f)
        ) {}

        Surface(
            modifier = Modifier
                .size(104.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-48).dp, y = 142.dp),
            shape = CircleShape,
            color = LoginTeal.copy(alpha = 0.14f)
        ) {}
    }
}

@Composable
private fun HeroMetricRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HeroMetricCard(
            modifier = Modifier.weight(1f),
            label = "QR",
            value = "Check-in",
            accent = LoginIndigo
        )

        HeroMetricCard(
            modifier = Modifier.weight(1f),
            label = "Vote",
            value = "On-chain",
            accent = LoginTeal
        )

        HeroMetricCard(
            modifier = Modifier.weight(1f),
            label = "Result",
            value = "Verified",
            accent = LoginCoral
        )
    }
}

@Composable
private fun HeroMetricCard(
    modifier: Modifier,
    label: String,
    value: String,
    accent: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.66f),
        border = BorderStroke(
            width = 1.dp,
            color = accent.copy(alpha = 0.22f)
        ),
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = accent,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )

            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                color = LoginMuted,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = Color.White
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.78f)
        )
    }
}

@Composable
private fun RoleAccessGrid(
    onAdminClick: () -> Unit,
    onVoterClick: () -> Unit,
    onObserverClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            RoleAccessCard(
                modifier = Modifier.weight(1f),
                title = "Admin",
                subtitle = "Create elections and manage check-ins.",
                initials = "AD",
                badge = "Email OTP",
                accent = LoginIndigo,
                onClick = onAdminClick
            )

            RoleAccessCard(
                modifier = Modifier.weight(1f),
                title = "Voter",
                subtitle = "Check in and cast one verified vote.",
                initials = "VT",
                badge = "Ballot",
                accent = LoginTeal,
                onClick = onVoterClick
            )
        }

        RoleAccessCard(
            modifier = Modifier.fillMaxWidth(),
            title = "Observer",
            subtitle = "View public turnout and closed-election results.",
            initials = "OB",
            badge = "Read only",
            accent = LoginCoral,
            onClick = onObserverClick
        )
    }
}

@Composable
private fun RoleAccessCard(
    modifier: Modifier,
    title: String,
    subtitle: String,
    initials: String,
    badge: String,
    accent: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = LoginSoftCard
        ),
        border = BorderStroke(
            width = 1.dp,
            color = accent.copy(alpha = 0.32f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 9.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.20f),
                            LoginSoftCard
                        )
                    )
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = accent.copy(alpha = 0.13f)
            ) {
                Text(
                    text = badge,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = accent,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                modifier = Modifier.size(60.dp),
                shape = RoundedCornerShape(23.dp),
                color = accent,
                shadowElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = LoginInk,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = LoginMuted,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FeatureStack() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeatureRow(
            label = "01",
            title = "QR check-in",
            description = "Only eligible checked-in voters can continue.",
            accent = LoginIndigo
        )

        FeatureRow(
            label = "02",
            title = "Blockchain vote",
            description = "Votes are submitted to the deployed smart contract.",
            accent = LoginTeal
        )

        FeatureRow(
            label = "03",
            title = "Receipt verification",
            description = "Transaction hashes can be checked after voting.",
            accent = LoginCoral
        )

        FeatureRow(
            label = "04",
            title = "Final results",
            description = "Results are shown clearly after election closure.",
            accent = LoginAmber
        )
    }
}

@Composable
private fun FeatureRow(
    label: String,
    title: String,
    description: String,
    accent: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = LoginSoftCard
        ),
        border = BorderStroke(
            width = 1.dp,
            color = accent.copy(alpha = 0.30f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 7.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.20f),
                            LoginSoftCard
                        )
                    )
                )
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(19.dp),
                color = accent,
                shadowElevation = 5.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = LoginInk
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = LoginMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun BottomHomeBar(
    modifier: Modifier = Modifier,
    onAdminClick: () -> Unit,
    onVoterClick: () -> Unit,
    onObserverClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            color = Color.White.copy(alpha = 0.94f),
            shadowElevation = 18.dp,
            tonalElevation = 6.dp,
            border = BorderStroke(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.65f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                LoginIndigo.copy(alpha = 0.10f),
                                Color.White.copy(alpha = 0.88f),
                                LoginTeal.copy(alpha = 0.12f)
                            )
                        )
                    )
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomItem(
                    modifier = Modifier.weight(1f),
                    label = "Home",
                    selected = true,
                    onClick = {}
                )

                BottomItem(
                    modifier = Modifier.weight(1f),
                    label = "Admin",
                    selected = false,
                    onClick = onAdminClick
                )

                BottomItem(
                    modifier = Modifier.weight(1f),
                    label = "Voter",
                    selected = false,
                    onClick = onVoterClick
                )

                BottomItem(
                    modifier = Modifier.weight(1f),
                    label = "Observer",
                    selected = false,
                    onClick = onObserverClick
                )
            }
        }
    }
}

@Composable
private fun BottomItem(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = if (selected) {
                LoginIndigo.copy(alpha = 0.14f)
            } else {
                Color.Transparent
            }
        ) {
            Text(
                text = label.take(1),
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 5.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = if (selected) LoginIndigo else LoginMuted,
                textAlign = TextAlign.Center
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold,
            color = if (selected) LoginInk else LoginMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}