package com.example.evotingmobileapp.screens

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.evotingmobileapp.BuildConfig
import com.example.evotingmobileapp.auth.AuthSessionViewModel
import com.example.evotingmobileapp.blockchain.DemoVoterProfile
import com.example.evotingmobileapp.blockchain.DemoWallets
import com.example.evotingmobileapp.navigation.AppRoutes
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.UUID

private const val SECURE_CHECK_IN_PREFIX = "SecureVoteCheckIn"
private const val CHECK_IN_QR_VALIDITY_MILLIS = 2 * 60 * 1000L

private val VoterNavy = Color(0xFF07133A)
private val VoterIndigo = Color(0xFF4F32F6)
private val VoterPurple = Color(0xFF7C3AED)
private val VoterBlue = Color(0xFF1479FF)
private val VoterTeal = Color(0xFF00B8A9)
private val VoterCoral = Color(0xFFFF5C7A)
private val VoterAmber = Color(0xFFFFB020)
private val VoterPanel = Color(0xFFEFF4FF)
private val VoterCard = Color(0xFFF5F8FF)
private val VoterInk = Color(0xFF0A102C)
private val VoterMuted = Color(0xFF5B6380)

@Composable
fun VoterAccessScreen(
    navController: NavHostController,
    authSessionViewModel: AuthSessionViewModel
) {
    var selectedVoterIndex by rememberSaveable { mutableIntStateOf(0) }
    var qrRefreshKey by rememberSaveable { mutableIntStateOf(0) }

    val voters = DemoWallets.voters

    if (voters.isEmpty()) {
        EmptyVoterAccessState(navController = navController)
        return
    }

    val safeSelectedIndex = selectedVoterIndex.coerceIn(0, voters.lastIndex)
    val selectedVoter = voters[safeSelectedIndex]
    val voterWalletAddress = selectedVoter.address

    val secureQrPass = remember(voterWalletAddress, qrRefreshKey) {
        createSecureCheckInQrPass(walletAddress = voterWalletAddress)
    }

    val qrBitmap = remember(secureQrPass.content) {
        generateWalletQrBitmap(secureQrPass.content)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        VoterNavy,
                        Color(0xFF172B78),
                        Color(0xFF3155D8),
                        Color(0xFFD8ECFF)
                    )
                )
            )
    ) {
        VoterDecorativeBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(top = 16.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            VoterAccessHeader()

            VoterSelectorCard(
                voters = voters,
                selectedIndex = safeSelectedIndex,
                onVoterSelected = { index ->
                    selectedVoterIndex = index
                    qrRefreshKey += 1
                }
            )

            VoterQrPassCard(
                qrBitmap = qrBitmap,
                voter = selectedVoter,
                secureQrPass = secureQrPass,
                onRefreshQr = {
                    qrRefreshKey += 1
                }
            )

            ContinueAsVoterCard(
                voter = selectedVoter,
                onContinue = {
                    authSessionViewModel.signInAsDemoVoter(selectedVoter.address)

                    navController.navigate(AppRoutes.VOTER_DASHBOARD) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )

            SecuritySummaryRow()

            if (BuildConfig.ENABLE_DEMO_WALLET_SHORTCUTS) {
                LocalWalletNotice()
            }

            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun EmptyVoterAccessState(
    navController: NavHostController
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        VoterNavy,
                        Color(0xFF172B78),
                        Color(0xFF3155D8),
                        Color(0xFFD8ECFF)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        VoterDecorativeBackground()

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = VoterCard
            ),
            border = BorderStroke(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.70f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                VoterIndigo.copy(alpha = 0.16f),
                                VoterCard,
                                VoterTeal.copy(alpha = 0.12f)
                            )
                        )
                    )
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SecureVoteMark(
                    initials = "SV",
                    modifier = Modifier.size(60.dp),
                    accent = VoterIndigo
                )

                Text(
                    text = "No voter wallets found",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = VoterInk,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Add voter wallet details to continue with the check-in flow.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VoterMuted,
                    textAlign = TextAlign.Center
                )

                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(
                        width = 1.5.dp,
                        color = VoterIndigo.copy(alpha = 0.42f)
                    )
                ) {
                    Text(
                        text = "Back to homepage",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = VoterIndigo
                    )
                }
            }
        }
    }
}

@Composable
private fun VoterDecorativeBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.TopEnd)
                .offset(x = 96.dp, y = (-46).dp),
            shape = CircleShape,
            color = VoterPurple.copy(alpha = 0.42f)
        ) {}

        Surface(
            modifier = Modifier
                .size(230.dp)
                .align(Alignment.TopStart)
                .offset(x = (-94).dp, y = 190.dp),
            shape = CircleShape,
            color = VoterTeal.copy(alpha = 0.34f)
        ) {}

        Surface(
            modifier = Modifier
                .size(210.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 78.dp, y = (-110).dp),
            shape = CircleShape,
            color = VoterAmber.copy(alpha = 0.30f)
        ) {}

        Surface(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 78.dp, y = 80.dp),
            shape = CircleShape,
            color = VoterCoral.copy(alpha = 0.20f)
        ) {}
    }
}

@Composable
private fun VoterAccessHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = VoterPanel
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.70f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
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
                .padding(20.dp)
        ) {
            VoterHeaderDecorations()

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SecureVoteMark(
                        initials = "SV",
                        modifier = Modifier.size(58.dp),
                        accent = VoterIndigo
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "SecureVote Nepal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = VoterIndigo
                        )

                        Text(
                            text = "Voter access",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = VoterInk
                        )
                    }

                    HeaderChip(text = "QR PASS")
                }

                Text(
                    text = "Select a registered voter, generate a time-limited QR pass, then continue to the voting area.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VoterMuted
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HeaderMiniChip(
                        modifier = Modifier.weight(1f),
                        title = "QR",
                        value = "Check-in",
                        accent = VoterIndigo
                    )

                    HeaderMiniChip(
                        modifier = Modifier.weight(1f),
                        title = "Wallet",
                        value = "Linked",
                        accent = VoterTeal
                    )

                    HeaderMiniChip(
                        modifier = Modifier.weight(1f),
                        title = "Vote",
                        value = "Once",
                        accent = VoterCoral
                    )
                }
            }
        }
    }
}

@Composable
private fun VoterHeaderDecorations() {
    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .size(132.dp)
                .align(Alignment.TopEnd)
                .offset(x = 52.dp, y = (-64).dp),
            shape = CircleShape,
            color = VoterPurple.copy(alpha = 0.14f)
        ) {}

        Surface(
            modifier = Modifier
                .size(96.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-46).dp, y = 120.dp),
            shape = CircleShape,
            color = VoterTeal.copy(alpha = 0.14f)
        ) {}
    }
}

@Composable
private fun SecureVoteMark(
    initials: String,
    modifier: Modifier = Modifier,
    accent: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier.background(
                Brush.linearGradient(
                    colors = listOf(accent, VoterBlue, VoterPurple)
                )
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

@Composable
private fun HeaderChip(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = VoterIndigo.copy(alpha = 0.12f),
        border = BorderStroke(
            width = 1.dp,
            color = VoterIndigo.copy(alpha = 0.24f)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = VoterIndigo
        )
    }
}

@Composable
private fun HeaderMiniChip(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    accent: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.66f),
        border = BorderStroke(
            width = 1.dp,
            color = accent.copy(alpha = 0.24f)
        ),
        shadowElevation = 3.dp
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
                color = VoterMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun VoterSelectorCard(
    voters: List<DemoVoterProfile>,
    selectedIndex: Int,
    onVoterSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = VoterCard
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.70f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            VoterIndigo.copy(alpha = 0.15f),
                            VoterCard
                        )
                    )
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader(
                title = "Choose registered voter",
                subtitle = "This selects the wallet used for the voter session."
            )

            voters.forEachIndexed { index, voter ->
                VoterChoiceRow(
                    voter = voter,
                    selected = index == selectedIndex,
                    onClick = { onVoterSelected(index) }
                )
            }
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
            color = VoterInk
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = VoterMuted
        )
    }
}

@Composable
private fun VoterChoiceRow(
    voter: DemoVoterProfile,
    selected: Boolean,
    onClick: () -> Unit
) {
    val accent = if (selected) VoterIndigo else VoterTeal

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = if (selected) {
            VoterIndigo.copy(alpha = 0.13f)
        } else {
            Color.White.copy(alpha = 0.62f)
        },
        border = BorderStroke(
            width = 1.dp,
            color = accent.copy(alpha = if (selected) 0.36f else 0.18f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = voter.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = VoterInk
                )

                Text(
                    text = shortenWalletAddress(voter.address),
                    style = MaterialTheme.typography.bodySmall,
                    color = VoterMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (selected) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = VoterIndigo.copy(alpha = 0.13f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = VoterIndigo.copy(alpha = 0.22f)
                    )
                ) {
                    Text(
                        text = "Selected",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = VoterIndigo
                    )
                }
            }
        }
    }
}

@Composable
private fun VoterQrPassCard(
    qrBitmap: Bitmap,
    voter: DemoVoterProfile,
    secureQrPass: SecureCheckInQrPass,
    onRefreshQr: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(34.dp),
        colors = CardDefaults.cardColors(
            containerColor = VoterPanel
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.72f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 18.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE6E2FF),
                            Color(0xFFE8F6FF),
                            Color(0xFFE0FFF8)
                        )
                    )
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = VoterTeal.copy(alpha = 0.14f),
                border = BorderStroke(
                    width = 1.dp,
                    color = VoterTeal.copy(alpha = 0.25f)
                )
            ) {
                Text(
                    text = "CHECK-IN PASS",
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = VoterTeal
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Scan this QR at the polling desk",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = VoterInk,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "The pass is linked to the selected wallet and expires after 2 minutes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VoterMuted,
                    textAlign = TextAlign.Center
                )
            }

            Surface(
                shape = RoundedCornerShape(30.dp),
                color = Color.White,
                border = BorderStroke(
                    width = 1.dp,
                    color = VoterIndigo.copy(alpha = 0.18f)
                ),
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier.padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Voter check-in QR pass",
                        modifier = Modifier.size(202.dp)
                    )
                }
            }

            WalletAddressBox(
                voterLabel = voter.label,
                voterWalletAddress = voter.address
            )

            SecureQrDetailsBox(secureQrPass = secureQrPass)

            OutlinedButton(
                onClick = onRefreshQr,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(
                    width = 1.5.dp,
                    color = VoterTeal.copy(alpha = 0.48f)
                )
            ) {
                Text(
                    text = "Refresh QR pass",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = VoterTeal
                )
            }
        }
    }
}

@Composable
private fun WalletAddressBox(
    voterLabel: String,
    voterWalletAddress: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = VoterIndigo.copy(alpha = 0.12f),
        border = BorderStroke(
            width = 1.dp,
            color = VoterIndigo.copy(alpha = 0.22f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = "Selected voter",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = VoterIndigo
            )

            Text(
                text = voterLabel,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Black,
                color = VoterInk
            )

            Text(
                text = shortenWalletAddress(voterWalletAddress),
                style = MaterialTheme.typography.bodySmall,
                color = VoterMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SecureQrDetailsBox(
    secureQrPass: SecureCheckInQrPass
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = VoterTeal.copy(alpha = 0.12f),
        border = BorderStroke(
            width = 1.dp,
            color = VoterTeal.copy(alpha = 0.22f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = "Pass security",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = VoterTeal
            )

            Text(
                text = "Time-limited QR with a refreshed session token.",
                style = MaterialTheme.typography.bodySmall,
                color = VoterInk
            )

            Text(
                text = "Valid for ${getQrValidityMinutes()} minutes from refresh.",
                style = MaterialTheme.typography.bodySmall,
                color = VoterInk
            )

            Text(
                text = "Bound wallet: ${shortenWalletAddress(secureQrPass.walletAddress)}",
                style = MaterialTheme.typography.bodySmall,
                color = VoterMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ContinueAsVoterCard(
    voter: DemoVoterProfile,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = VoterCard
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.70f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            VoterTeal.copy(alpha = 0.16f),
                            VoterCard,
                            VoterIndigo.copy(alpha = 0.08f)
                        )
                    )
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader(
                title = "Continue to voting",
                subtitle = "Use the selected wallet for the voter dashboard and ballot submission."
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.64f),
                border = BorderStroke(
                    width = 1.dp,
                    color = VoterIndigo.copy(alpha = 0.16f)
                )
            ) {
                Text(
                    text = "${voter.label}  |  ${shortenWalletAddress(voter.address)}",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = VoterInk,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VoterIndigo,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = "Continue as voter",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black
                )
            }

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(
                    width = 1.5.dp,
                    color = VoterIndigo.copy(alpha = 0.40f)
                )
            ) {
                Text(
                    text = "Back to homepage",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = VoterIndigo
                )
            }
        }
    }
}

@Composable
private fun SecuritySummaryRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MiniInfoTile(
            modifier = Modifier.weight(1f),
            title = "QR check-in",
            subtitle = "Required before vote",
            accent = VoterIndigo
        )

        MiniInfoTile(
            modifier = Modifier.weight(1f),
            title = "Receipt",
            subtitle = "Hash after voting",
            accent = VoterTeal
        )
    }
}

@Composable
private fun MiniInfoTile(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    accent: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.88f),
        border = BorderStroke(
            width = 1.dp,
            color = accent.copy(alpha = 0.24f)
        ),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = accent,
                textAlign = TextAlign.Center
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = VoterMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LocalWalletNotice() {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.86f),
        border = BorderStroke(
            width = 1.dp,
            color = VoterAmber.copy(alpha = 0.28f)
        ),
        shadowElevation = 7.dp
    ) {
        Text(
            text = "Local test wallets are enabled for this prototype build. Do not use these wallets outside the local Hardhat demonstration.",
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            style = MaterialTheme.typography.bodySmall,
            color = VoterInk,
            textAlign = TextAlign.Center
        )
    }
}

private data class SecureCheckInQrPass(
    val content: String,
    val walletAddress: String,
    val issuedAtMillis: Long,
    val expiresAtMillis: Long,
    val nonce: String
)

private fun createSecureCheckInQrPass(walletAddress: String): SecureCheckInQrPass {
    val issuedAtMillis = System.currentTimeMillis()
    val expiresAtMillis = issuedAtMillis + CHECK_IN_QR_VALIDITY_MILLIS
    val nonce = UUID.randomUUID().toString().replace("-", "")

    val content = buildString {
        append(SECURE_CHECK_IN_PREFIX)
        append("|wallet=")
        append(walletAddress)
        append("|issuedAt=")
        append(issuedAtMillis)
        append("|expiresAt=")
        append(expiresAtMillis)
        append("|nonce=")
        append(nonce)
    }

    return SecureCheckInQrPass(
        content = content,
        walletAddress = walletAddress,
        issuedAtMillis = issuedAtMillis,
        expiresAtMillis = expiresAtMillis,
        nonce = nonce
    )
}

private fun generateWalletQrBitmap(content: String, size: Int = 900): Bitmap {
    val hints = mapOf(
        EncodeHintType.MARGIN to 1,
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M
    )

    val bitMatrix = QRCodeWriter().encode(
        content,
        BarcodeFormat.QR_CODE,
        size,
        size,
        hints
    )

    val pixels = IntArray(size * size)

    for (y in 0 until size) {
        for (x in 0 until size) {
            pixels[y * size + x] = if (bitMatrix[x, y]) {
                AndroidColor.BLACK
            } else {
                AndroidColor.WHITE
            }
        }
    }

    return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
        setPixels(pixels, 0, size, 0, 0, size, size)
    }
}

private fun shortenWalletAddress(address: String): String {
    if (address.length <= 18) return address
    return "${address.take(10)}...${address.takeLast(8)}"
}

private fun getQrValidityMinutes(): Int {
    return (CHECK_IN_QR_VALIDITY_MILLIS / 60_000L).toInt()
}