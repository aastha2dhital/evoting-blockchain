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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.evotingmobileapp.auth.AuthSessionViewModel
import com.example.evotingmobileapp.navigation.AppRoutes

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
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.30f),
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.26f)
                    )
                )
            )
    ) {
        CivicBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 118.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            TopBrandBar()

            MainHeroCard(
                onAdminClick = { navController.navigate(AppRoutes.ADMIN_LOGIN) },
                onVoterClick = { navController.navigate(AppRoutes.VOTER_ACCESS) },
                onObserverClick = { navController.navigate(AppRoutes.OBSERVER_TURNOUT) }
            )

            SectionTitle(
                title = "Choose your role",
                subtitle = "Select the correct portal to continue securely."
            )

            RoleAccessGrid(
                onAdminClick = { navController.navigate(AppRoutes.ADMIN_LOGIN) },
                onVoterClick = { navController.navigate(AppRoutes.VOTER_ACCESS) },
                onObserverClick = { navController.navigate(AppRoutes.OBSERVER_TURNOUT) }
            )

            SectionTitle(
                title = "How SecureVote Nepal works",
                subtitle = "A simple flow from voter check-in to verified final results."
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
private fun CivicBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.TopEnd)
                .offset(x = 82.dp, y = 62.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        ) {}

        Surface(
            modifier = Modifier
                .size(168.dp)
                .align(Alignment.TopStart)
                .offset(x = (-70).dp, y = 260.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f)
        ) {}

        Surface(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 70.dp, y = (-160).dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f)
        ) {}
    }
}

@Composable
private fun TopBrandBar() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 5.dp,
        shadowElevation = 8.dp,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.42f)
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
                BrandMark(text = "VN")

                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = "SecureVote Nepal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Secure Digital Voting",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "Verified",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun BrandMark(text: String) {
    Surface(
        modifier = Modifier.size(42.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 5.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimary
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(34.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.13f)
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
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                    )
                ) {
                    Text(
                        text = "SECURE • VERIFIED • TRANSPARENT",
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
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
                            text = "SecureVote Nepal",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = MaterialTheme.typography.displaySmall.lineHeight
                        )

                        Text(
                            text = "A modern election platform for secure check-in, trusted voting, observer turnout, and clear blockchain-backed results.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        modifier = Modifier.size(78.dp),
                        shape = RoundedCornerShape(26.dp),
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 8.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "VN",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                HeroMetricRow()

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onVoterClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 5.dp)
                    ) {
                        Text(
                            text = "Continue as Voter",
                            fontWeight = FontWeight.ExtraBold,
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
                                .height(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.42f)
                            )
                        ) {
                            Text(
                                text = "Admin OTP",
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        OutlinedButton(
                            onClick = onObserverClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.46f)
                            )
                        ) {
                            Text(
                                text = "Observer View",
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
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
                .size(130.dp)
                .align(Alignment.TopEnd)
                .offset(x = 46.dp, y = (-56).dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ) {}

        Surface(
            modifier = Modifier
                .size(92.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-46).dp, y = 142.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
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
            value = "Check-in"
        )

        HeroMetricCard(
            modifier = Modifier.weight(1f),
            label = "Vote",
            value = "Verified"
        )

        HeroMetricCard(
            modifier = Modifier.weight(1f),
            label = "Observe",
            value = "Turnout"
        )
    }
}

@Composable
private fun HeroMetricCard(
    modifier: Modifier,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.36f)
        ),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )

            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                title = "Admin Portal",
                subtitle = "Use OTP verification to create elections, check in voters, and review admin controls.",
                initials = "AD",
                badge = "Election Control",
                usePrimary = true,
                onClick = onAdminClick
            )

            RoleAccessCard(
                modifier = Modifier.weight(1f),
                title = "Voter Portal",
                subtitle = "Choose your voter profile, complete check-in, and cast your vote securely.",
                initials = "VT",
                badge = "Secure Voting",
                usePrimary = false,
                onClick = onVoterClick
            )
        }

        RoleAccessCard(
            modifier = Modifier.fillMaxWidth(),
            title = "Observer View",
            subtitle = "Read-only turnout and results visibility without election creation, voting, check-in, or admin controls.",
            initials = "OB",
            badge = "Public Turnout",
            usePrimary = false,
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
    usePrimary: Boolean,
    onClick: () -> Unit
) {
    val accentColor = if (usePrimary) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondary
    }

    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = accentColor.copy(alpha = 0.25f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 7.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.16f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = accentColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = badge,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(24.dp),
                color = accentColor,
                shadowElevation = 5.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FeatureStack() {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FeatureRow(
            icon = "QR",
            title = "QR Check-in",
            description = "Verify eligible voters before the ballot opens."
        )

        FeatureRow(
            icon = "VOTE",
            title = "Trusted Vote",
            description = "Cast one secure vote and receive a transaction receipt with QR sharing."
        )

        FeatureRow(
            icon = "OBS",
            title = "Observer Turnout",
            description = "Allow observers to view turnout and closed-election results without admin access."
        )

        FeatureRow(
            icon = "WIN",
            title = "Clear Results",
            description = "View the winner, total votes, and vote share clearly after election closure."
        )
    }
}

@Composable
private fun FeatureRow(
    icon: String,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.36f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.48f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.32f)
                        )
                    )
                )
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.size(54.dp),
                shape = RoundedCornerShape(19.dp),
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = icon,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
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
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
            shadowElevation = 18.dp,
            tonalElevation = 6.dp,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.34f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.40f),
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.30f)
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
                    icon = "⌂",
                    selected = true,
                    onClick = {}
                )

                BottomItem(
                    modifier = Modifier.weight(1f),
                    label = "Admin",
                    icon = "A",
                    selected = false,
                    onClick = onAdminClick
                )

                BottomItem(
                    modifier = Modifier.weight(1f),
                    label = "Voter",
                    icon = "V",
                    selected = false,
                    onClick = onVoterClick
                )

                BottomItem(
                    modifier = Modifier.weight(1f),
                    label = "Observer",
                    icon = "O",
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
    icon: String,
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
                MaterialTheme.colorScheme.primaryContainer
            } else {
                Color.Transparent
            }
        ) {
            Text(
                text = icon,
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 5.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (selected) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}