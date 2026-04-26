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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.evotingmobileapp.R
import com.example.evotingmobileapp.auth.AuthSessionViewModel
import com.example.evotingmobileapp.navigation.AppRoutes

@Composable
fun LoginScreen(
    navController: NavHostController,
    authSessionViewModel: AuthSessionViewModel,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(authSessionViewModel) {
        authSessionViewModel.disconnectWallet()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.30f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.20f),
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.18f)
                    )
                )
            )
    ) {
        DecorativeBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 122.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            HomeTopBar()

            SearchLikeBar(
                modifier = Modifier.padding(end = 104.dp)
            )

            PremiumHeroCard(
                onVoteClick = {
                    navController.navigate(AppRoutes.VOTER_ACCESS)
                }
            )

            SectionHeader(
                title = stringResource(R.string.login_access_section_title),
                subtitle = "Secure role-based access for election officials and voters"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                AccessTile(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.login_access_admin_title),
                    subtitle = stringResource(R.string.login_access_admin_subtitle),
                    icon = stringResource(R.string.admin_avatar_initials),
                    badge = "Officer",
                    primary = true,
                    onClick = {
                        navController.navigate(AppRoutes.ADMIN_LOGIN)
                    }
                )

                AccessTile(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.login_access_voter_title),
                    subtitle = stringResource(R.string.login_access_voter_subtitle),
                    icon = stringResource(R.string.voter_avatar_initials),
                    badge = "Citizen",
                    primary = false,
                    onClick = {
                        navController.navigate(AppRoutes.VOTER_ACCESS)
                    }
                )
            }

            SectionHeader(
                title = stringResource(R.string.login_info_section_title),
                subtitle = "Every step is designed for transparency, attendance, and verification"
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureRow(
                    icon = "QR",
                    title = stringResource(R.string.login_info_qr_checkin),
                    description = "Polling officer confirms voter attendance before voting."
                )

                FeatureRow(
                    icon = "TX",
                    title = stringResource(R.string.login_info_vote_receipt),
                    description = "A blockchain transaction hash is generated after voting."
                )

                FeatureRow(
                    icon = "✓",
                    title = stringResource(R.string.login_info_results),
                    description = "Election results are shown after the election closes."
                )
            }
        }

        BottomHomeBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            onAdminClick = {
                navController.navigate(AppRoutes.ADMIN_LOGIN)
            },
            onVoterClick = {
                navController.navigate(AppRoutes.VOTER_ACCESS)
            }
        )
    }
}

@Composable
private fun DecorativeBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .size(190.dp)
                .align(Alignment.TopEnd)
                .offset(x = 68.dp, y = 80.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ) {}

        Surface(
            modifier = Modifier
                .size(158.dp)
                .align(Alignment.TopStart)
                .offset(x = (-58).dp, y = 236.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.07f)
        ) {}

        Surface(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 55.dp, y = (-116).dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f)
        ) {}

        Surface(
            modifier = Modifier
                .size(112.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-36).dp, y = (-34).dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
        ) {}
    }
}

@Composable
private fun HomeTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            tonalElevation = 4.dp,
            shadowElevation = 5.dp,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.32f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircleIcon(text = "NP")
                Text(
                    text = "Vote Nepal",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            CircleIcon(text = "BC")
            CircleIcon(text = "?")
        }
    }
}

@Composable
private fun CircleIcon(text: String) {
    Surface(
        modifier = Modifier.size(36.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SearchLikeBar(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 3.dp,
        shadowElevation = 6.dp,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.34f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⌕",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(9.dp))

            Text(
                text = stringResource(R.string.login_search_placeholder),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PremiumHeroCard(
    onVoteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.96f),
                            Color.White,
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            HeroDecorations()

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(17.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                            )
                        ) {
                            Text(
                                text = "BLOCKCHAIN + QR CHECK-IN",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = stringResource(R.string.login_brand_title),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "A secure mobile voting prototype for transparent, auditable, and attendance-verified elections.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        modifier = Modifier.size(68.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 6.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "EV",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HeroChip(
                        modifier = Modifier.weight(1f),
                        title = "On-chain",
                        value = "Verified"
                    )
                    HeroChip(
                        modifier = Modifier.weight(1f),
                        title = "Check-in",
                        value = "Required"
                    )
                    HeroChip(
                        modifier = Modifier.weight(1f),
                        title = "Receipt",
                        value = "Enabled"
                    )
                }

                ElectionCountdownStrip()

                Button(
                    onClick = onVoteClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.dashboard_vote_now),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroDecorations() {
    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .size(124.dp)
                .align(Alignment.TopEnd)
                .offset(x = 46.dp, y = (-50).dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ) {}

        Surface(
            modifier = Modifier
                .size(84.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-44).dp, y = 136.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
        ) {}
    }
}

@Composable
private fun HeroChip(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.86f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.13f)
        ),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ElectionCountdownStrip() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.88f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
        ),
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(13.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.login_election_card_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
                    )
                ) {
                    Text(
                        text = "LIVE",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CountdownBox(
                    modifier = Modifier.weight(1f),
                    number = "03",
                    label = stringResource(R.string.login_countdown_days)
                )
                CountdownBox(
                    modifier = Modifier.weight(1f),
                    number = "02",
                    label = stringResource(R.string.login_countdown_hours)
                )
                CountdownBox(
                    modifier = Modifier.weight(1f),
                    number = "38",
                    label = stringResource(R.string.login_countdown_minutes)
                )
                CountdownBox(
                    modifier = Modifier.weight(1f),
                    number = "12",
                    label = stringResource(R.string.login_countdown_seconds)
                )
            }
        }
    }
}

@Composable
private fun CountdownBox(
    modifier: Modifier = Modifier,
    number: String,
    label: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(15.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.13f)
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 9.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AccessTile(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: String,
    badge: String,
    primary: Boolean,
    onClick: () -> Unit
) {
    val accentColor = if (primary) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondary
    }

    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = accentColor.copy(alpha = 0.20f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 7.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.14f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(horizontal = 14.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = accentColor.copy(alpha = 0.12f),
                border = BorderStroke(
                    width = 1.dp,
                    color = accentColor.copy(alpha = 0.18f)
                )
            ) {
                Text(
                    text = badge,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = accentColor
                )
            }

            Surface(
                modifier = Modifier.size(60.dp),
                shape = CircleShape,
                color = accentColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = icon,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
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
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.34f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.52f),
                            Color.White,
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.30f)
                        )
                    )
                )
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(17.dp),
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = icon,
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

            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "›",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomHomeBar(
    modifier: Modifier = Modifier,
    onAdminClick: () -> Unit,
    onVoterClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
            shadowElevation = 18.dp,
            tonalElevation = 6.dp,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f),
                                Color.White,
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.24f)
                            )
                        )
                    )
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomItem(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.login_bottom_home),
                    icon = "⌂",
                    selected = true,
                    onClick = {}
                )
                BottomItem(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.login_bottom_admin),
                    icon = "A",
                    selected = false,
                    onClick = onAdminClick
                )
                BottomItem(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.login_bottom_voter),
                    icon = "V",
                    selected = false,
                    onClick = onVoterClick
                )
                BottomItem(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.login_bottom_profile),
                    icon = "●",
                    selected = false,
                    onClick = {}
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
        verticalArrangement = Arrangement.spacedBy(4.dp)
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
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
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
            fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold,
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