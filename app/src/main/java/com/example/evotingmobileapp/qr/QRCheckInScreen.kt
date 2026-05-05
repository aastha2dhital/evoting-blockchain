package com.example.evotingmobileapp.qr

import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.evotingmobileapp.admin.AdminViewModel
import com.example.evotingmobileapp.blockchain.DemoVoterProfile
import com.example.evotingmobileapp.blockchain.DemoWallets
import com.example.evotingmobileapp.model.Election
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val SECURE_CHECK_IN_PREFIX = "SecureVoteCheckIn"
private const val QR_CLOCK_SKEW_TOLERANCE_MILLIS = 5 * 60 * 1000L

private val QrNavy = Color(0xFF07133A)
private val QrDeepBlue = Color(0xFF102A70)
private val QrIndigo = Color(0xFF4F32F6)
private val QrPurple = Color(0xFF7C3AED)
private val QrBlue = Color(0xFF1479FF)
private val QrTeal = Color(0xFF00B8A9)
private val QrCoral = Color(0xFFFF5C7A)
private val QrAmber = Color(0xFFFFB020)
private val QrGreen = Color(0xFF17C964)
private val QrCard = Color(0xFFF6F8FF)
private val QrPanel = Color(0xFFEAF1FF)
private val QrInk = Color(0xFF081229)
private val QrMuted = Color(0xFF59627E)
private val QrLine = Color(0xFFD6DDF4)

@Composable
fun QRCheckInScreen(
    navController: NavHostController,
    adminViewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val elections by adminViewModel.elections.collectAsState()

    var selectedElectionId by rememberSaveable { mutableStateOf("") }
    var voterWalletAddress by rememberSaveable { mutableStateOf("") }
    var statusMessage by rememberSaveable { mutableStateOf("") }
    var statusIsPositive by rememberSaveable { mutableStateOf(false) }
    var lastScannedValue by rememberSaveable { mutableStateOf("") }
    var isCheckingIn by rememberSaveable { mutableStateOf(false) }

    val usedQrNonces = remember { mutableStateListOf<String>() }
    val selectedElection = elections.find { it.id == selectedElectionId }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        val scannedValue = result.contents?.trim().orEmpty()

        if (scannedValue.isBlank()) {
            statusMessage = if (result.contents == null) {
                "QR scan cancelled."
            } else {
                "No readable QR value was found."
            }
            statusIsPositive = false
            lastScannedValue = ""
        } else {
            when (val parsedPayload = parseSecureCheckInPayload(scannedValue)) {
                is SecureCheckInParseResult.Valid -> {
                    if (usedQrNonces.contains(parsedPayload.payload.nonce)) {
                        voterWalletAddress = ""
                        lastScannedValue = scannedValue
                        statusMessage = "This QR pass has already been scanned in this session. Ask the voter to refresh their pass."
                        statusIsPositive = false
                    } else {
                        usedQrNonces.add(parsedPayload.payload.nonce)
                        voterWalletAddress = parsedPayload.payload.walletAddress
                        lastScannedValue = scannedValue
                        statusMessage = "QR pass accepted. Voter wallet is ready for check-in."
                        statusIsPositive = true
                    }
                }

                is SecureCheckInParseResult.Invalid -> {
                    voterWalletAddress = ""
                    lastScannedValue = scannedValue
                    statusMessage = parsedPayload.reason
                    statusIsPositive = false
                }
            }
        }

        coroutineScope.launch {
            snackBarHostState.showSnackbar(statusMessage)
        }
    }

    val scanOptions = remember {
        ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("Scan voter check-in QR pass")
            setCameraId(0)
            setBeepEnabled(true)
            setBarcodeImageEnabled(false)
            setOrientationLocked(false)
        }
    }

    LaunchedEffect(elections, selectedElectionId) {
        if (selectedElectionId.isNotBlank() && selectedElection == null) {
            selectedElectionId = ""
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            QrNavy,
                            QrDeepBlue,
                            Color(0xFF3155D8),
                            Color(0xFFD8ECFF)
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            QRDecorativeBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp)
                    .padding(top = 16.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                QRHeroCard(
                    electionCount = elections.size,
                    selectedElectionTitle = selectedElection?.title,
                    walletAddress = voterWalletAddress
                )

                QRProgressCard(
                    hasElection = selectedElection != null,
                    hasWallet = voterWalletAddress.trim().isNotBlank(),
                    hasValidStatus = statusMessage.isNotBlank() && statusIsPositive,
                    isCheckingIn = isCheckingIn
                )

                ModernStepCard(
                    step = "01",
                    title = "Select election",
                    subtitle = "Choose the election where this voter is being checked in."
                ) {
                    if (elections.isEmpty()) {
                        InfoPanel(
                            title = "No elections available",
                            message = "Create an election from the admin dashboard before scanning voter QR passes.",
                            positive = false
                        )
                    } else {
                        elections.forEach { election ->
                            ElectionChoiceCard(
                                election = election,
                                selected = selectedElectionId == election.id,
                                enabled = !isCheckingIn,
                                onSelected = {
                                    if (!isCheckingIn) {
                                        selectedElectionId = election.id
                                    }
                                }
                            )
                        }
                    }
                }

                ModernStepCard(
                    step = "02",
                    title = "Load voter wallet",
                    subtitle = "Scan the voter QR pass or select a registered demo voter."
                ) {
                    InfoPanel(
                        title = "QR pass required",
                        message = "The voter should open Voter Access and show the current two-minute check-in QR pass.",
                        positive = true
                    )

                    OutlinedTextField(
                        value = voterWalletAddress,
                        onValueChange = {
                            voterWalletAddress = it
                            statusMessage = ""
                        },
                        label = { Text(text = "Voter wallet address") },
                        placeholder = { Text(text = "0x...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isCheckingIn,
                        shape = RoundedCornerShape(20.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                voterWalletAddress = DemoWallets.defaultVoterAddress
                                lastScannedValue = ""
                                statusMessage = "Registered voter wallet loaded."
                                statusIsPositive = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            enabled = !isCheckingIn,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = QrIndigo)
                        ) {
                            Text(
                                text = "Use voter",
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                voterWalletAddress = ""
                                lastScannedValue = ""
                                statusMessage = ""
                                statusIsPositive = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            enabled = !isCheckingIn,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "Clear",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    RegisteredVoterQuickSelect(
                        voters = DemoWallets.voters,
                        enabled = !isCheckingIn,
                        onSelected = { voter ->
                            voterWalletAddress = voter.address
                            lastScannedValue = ""
                            statusMessage = "${voter.label} loaded for check-in."
                            statusIsPositive = true
                        }
                    )

                    Button(
                        onClick = {
                            scanLauncher.launch(scanOptions)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        enabled = !isCheckingIn,
                        shape = RoundedCornerShape(21.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = QrTeal),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    ) {
                        Text(
                            text = "Scan voter QR pass",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black
                        )
                    }

                    if (lastScannedValue.isNotBlank()) {
                        InfoPanel(
                            title = "Last scan",
                            message = if (voterWalletAddress.isNotBlank()) {
                                "Accepted wallet: ${shortenWalletAddress(voterWalletAddress)}"
                            } else {
                                "The scanned QR pass was rejected."
                            },
                            positive = voterWalletAddress.isNotBlank()
                        )
                    }

                    InfoPanel(
                        title = "Prototype note",
                        message = "This build validates QR expiry and repeated scans during the current admin session. A production version should use signed QR passes and persistent replay protection.",
                        positive = false
                    )
                }

                CheckInActionCard(
                    selectedElectionTitle = selectedElection?.title,
                    voterWalletAddress = voterWalletAddress,
                    statusMessage = statusMessage,
                    statusIsPositive = statusIsPositive,
                    isCheckingIn = isCheckingIn,
                    onCheckIn = {
                        val electionForCheckIn = selectedElection
                        val trimmedWalletAddress = voterWalletAddress.trim()

                        if (electionForCheckIn == null) {
                            statusMessage = "Select an election first."
                            statusIsPositive = false

                            coroutineScope.launch {
                                snackBarHostState.showSnackbar(statusMessage)
                            }

                            return@CheckInActionCard
                        }

                        if (trimmedWalletAddress.isBlank()) {
                            statusMessage = "Enter or scan a voter wallet first."
                            statusIsPositive = false

                            coroutineScope.launch {
                                snackBarHostState.showSnackbar(statusMessage)
                            }

                            return@CheckInActionCard
                        }

                        if (!isValidEthereumAddress(trimmedWalletAddress)) {
                            statusMessage = "Enter or scan a valid Ethereum wallet address."
                            statusIsPositive = false

                            coroutineScope.launch {
                                snackBarHostState.showSnackbar(statusMessage)
                            }

                            return@CheckInActionCard
                        }

                        isCheckingIn = true
                        statusMessage = "Checking voter on blockchain..."
                        statusIsPositive = false

                        coroutineScope.launch {
                            snackBarHostState.showSnackbar(statusMessage)

                            val result = withContext(Dispatchers.IO) {
                                adminViewModel.checkInVoterOnChain(
                                    context = context,
                                    electionId = electionForCheckIn.id,
                                    voterWalletAddress = trimmedWalletAddress
                                )
                            }

                            isCheckingIn = false

                            result.fold(
                                onSuccess = { message ->
                                    statusMessage = message
                                    statusIsPositive = true
                                    snackBarHostState.showSnackbar(message)
                                },
                                onFailure = { exception ->
                                    statusMessage = exception.message ?: "Blockchain check-in failed."
                                    statusIsPositive = false
                                    snackBarHostState.showSnackbar(statusMessage)
                                }
                            )
                        }
                    }
                )

                TextButton(
                    onClick = { navController.popBackStack() },
                    enabled = !isCheckingIn,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Back to admin dashboard",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun QRDecorativeBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .size(230.dp)
                .offset(x = (-94).dp, y = 42.dp),
            shape = CircleShape,
            color = QrTeal.copy(alpha = 0.16f)
        ) {}

        Surface(
            modifier = Modifier
                .size(190.dp)
                .align(Alignment.TopEnd)
                .offset(x = 76.dp, y = 92.dp),
            shape = CircleShape,
            color = QrCoral.copy(alpha = 0.16f)
        ) {}

        Surface(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 92.dp, y = 36.dp),
            shape = CircleShape,
            color = QrAmber.copy(alpha = 0.14f)
        ) {}

        Surface(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-136).dp, y = 116.dp),
            shape = CircleShape,
            color = QrPurple.copy(alpha = 0.15f)
        ) {}
    }
}

@Composable
private fun QRHeroCard(
    electionCount: Int,
    selectedElectionTitle: String?,
    walletAddress: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.95f),
                            Color(0xFFEFF4FF).copy(alpha = 0.92f)
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
        ) {
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 28.dp, y = (-32).dp),
                shape = CircleShape,
                color = QrTeal.copy(alpha = 0.12f)
            ) {}

            Surface(
                modifier = Modifier
                    .size(76.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 32.dp, y = 22.dp),
                shape = CircleShape,
                color = QrCoral.copy(alpha = 0.10f)
            ) {}

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(58.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = QrIndigo,
                        shadowElevation = 8.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "SV",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "SecureVote Nepal",
                            style = MaterialTheme.typography.titleSmall,
                            color = QrIndigo,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Polling check-in",
                            style = MaterialTheme.typography.headlineSmall,
                            color = QrInk,
                            fontWeight = FontWeight.Black
                        )
                    }

                    AdminChip()
                }

                Text(
                    text = "Scan the voter QR pass, confirm the election, and mark attendance on the blockchain before voting.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = QrMuted
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HeroMetric(
                        modifier = Modifier.weight(1f),
                        label = "Elections",
                        value = electionCount.toString(),
                        accent = QrBlue
                    )

                    HeroMetric(
                        modifier = Modifier.weight(1f),
                        label = "Wallet",
                        value = if (walletAddress.isBlank()) "Pending" else "Ready",
                        accent = QrTeal
                    )
                }

                InfoStrip(
                    text = selectedElectionTitle ?: "No election selected yet"
                )
            }
        }
    }
}

@Composable
private fun AdminChip() {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = QrCoral.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, QrCoral.copy(alpha = 0.30f))
    ) {
        Text(
            text = "ADMIN",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelSmall,
            color = QrCoral,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun HeroMetric(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = accent.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = QrMuted
            )

            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = QrInk,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun InfoStrip(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, QrLine.copy(alpha = 0.82f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = QrInk,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun QRProgressCard(
    hasElection: Boolean,
    hasWallet: Boolean,
    hasValidStatus: Boolean,
    isCheckingIn: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.18f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Polling officer checklist",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProgressStep(
                    modifier = Modifier.weight(1f),
                    number = "1",
                    label = "Election",
                    done = hasElection
                )

                ProgressStep(
                    modifier = Modifier.weight(1f),
                    number = "2",
                    label = "Wallet",
                    done = hasWallet
                )

                ProgressStep(
                    modifier = Modifier.weight(1f),
                    number = "3",
                    label = "QR",
                    done = hasValidStatus
                )

                ProgressStep(
                    modifier = Modifier.weight(1f),
                    number = "4",
                    label = "Chain",
                    done = isCheckingIn
                )
            }
        }
    }
}

@Composable
private fun ProgressStep(
    modifier: Modifier = Modifier,
    number: String,
    label: String,
    done: Boolean
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = if (done) QrGreen.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.12f),
        border = BorderStroke(
            width = 1.dp,
            color = if (done) QrGreen.copy(alpha = 0.50f) else Color.White.copy(alpha = 0.18f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                color = if (done) QrGreen else Color.White.copy(alpha = 0.16f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (done) "✓" else number,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.92f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ModernStepCard(
    step: String,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = QrCard.copy(alpha = 0.96f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(17.dp),
                    color = QrIndigo,
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = step,
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = QrInk
                    )

                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = QrMuted
                    )
                }
            }

            HorizontalDivider(color = QrLine.copy(alpha = 0.78f))

            content()
        }
    }
}

@Composable
private fun ElectionChoiceCard(
    election: Election,
    selected: Boolean,
    enabled: Boolean,
    onSelected: () -> Unit
) {
    val accentColor = when {
        election.isActive() -> QrGreen
        election.isClosed() -> QrCoral
        else -> QrAmber
    }

    val containerColor = if (selected) {
        QrIndigo.copy(alpha = 0.12f)
    } else {
        QrPanel.copy(alpha = 0.86f)
    }

    val border = if (selected) {
        BorderStroke(1.4.dp, QrIndigo.copy(alpha = 0.50f))
    } else {
        BorderStroke(1.dp, QrLine.copy(alpha = 0.72f))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onSelected() },
        shape = RoundedCornerShape(22.dp),
        color = containerColor,
        border = border
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(17.dp),
                color = if (selected) QrIndigo else Color.White.copy(alpha = 0.88f),
                border = BorderStroke(1.dp, if (selected) QrIndigo else QrLine)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "E",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = if (selected) Color.White else QrIndigo
                    )
                }
            }

            RadioButton(
                selected = selected,
                onClick = onSelected,
                enabled = enabled
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = election.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = QrInk,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Election ID: ${election.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = QrMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = electionStatusText(election),
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    fontWeight = FontWeight.Black
                )
            }

            if (selected) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = QrGreen.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, QrGreen.copy(alpha = 0.28f))
                ) {
                    Text(
                        text = "Selected",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = QrGreen,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun RegisteredVoterQuickSelect(
    voters: List<DemoVoterProfile>,
    enabled: Boolean,
    onSelected: (DemoVoterProfile) -> Unit
) {
    if (voters.isEmpty()) return

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Registered test voters",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = QrIndigo
            )

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = QrTeal.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, QrTeal.copy(alpha = 0.22f))
            ) {
                Text(
                    text = "${voters.size} wallets",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = QrTeal
                )
            }
        }

        voters.forEachIndexed { index, voter ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled) { onSelected(voter) },
                shape = RoundedCornerShape(18.dp),
                color = Color.White.copy(alpha = 0.78f),
                border = BorderStroke(1.dp, QrLine.copy(alpha = 0.75f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(38.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = QrBlue.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, QrBlue.copy(alpha = 0.20f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = (index + 1).toString(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Black,
                                color = QrBlue
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = voter.label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = QrInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = shortenWalletAddress(voter.address),
                            style = MaterialTheme.typography.bodySmall,
                            color = QrMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "Load",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = QrIndigo
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckInActionCard(
    selectedElectionTitle: String?,
    voterWalletAddress: String,
    statusMessage: String,
    statusIsPositive: Boolean,
    isCheckingIn: Boolean,
    onCheckIn: () -> Unit
) {
    val electionText = selectedElectionTitle ?: "Not selected"
    val walletText = if (voterWalletAddress.isBlank()) {
        "Not loaded"
    } else {
        shortenWalletAddress(voterWalletAddress.trim())
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            QrIndigo.copy(alpha = 0.96f),
                            QrBlue.copy(alpha = 0.92f),
                            QrTeal.copy(alpha = 0.82f)
                        )
                    ),
                    shape = RoundedCornerShape(30.dp)
                )
        ) {
            Surface(
                modifier = Modifier
                    .size(110.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 42.dp, y = (-34).dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.14f)
            ) {}

            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "FINAL STEP",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.78f),
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = "Complete check-in",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = "Confirm the selected election and wallet before writing the check-in status to blockchain.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.82f)
                    )
                }

                SummaryTile(
                    label = "Election",
                    value = electionText
                )

                SummaryTile(
                    label = "Voter wallet",
                    value = walletText
                )

                Button(
                    onClick = onCheckIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    enabled = !isCheckingIn,
                    shape = RoundedCornerShape(21.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = QrCoral,
                        disabledContainerColor = Color.White.copy(alpha = 0.22f),
                        disabledContentColor = Color.White.copy(alpha = 0.72f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        text = if (isCheckingIn) {
                            "Checking in..."
                        } else {
                            "Mark voter as checked-in"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black
                    )
                }

                if (statusMessage.isNotBlank()) {
                    StatusPanel(
                        message = statusMessage,
                        positive = statusIsPositive
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryTile(
    label: String,
    value: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.17f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.80f),
                fontWeight = FontWeight.Black
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun InfoPanel(
    title: String,
    message: String,
    positive: Boolean
) {
    val accentColor = if (positive) QrTeal else QrAmber

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = accentColor.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.size(30.dp),
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.16f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (positive) "✓" else "!",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = accentColor
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = QrInk,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = QrMuted
                )
            }
        }
    }
}

@Composable
private fun StatusPanel(
    message: String,
    positive: Boolean
) {
    val accentColor = if (positive) QrGreen else QrCoral

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.42f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.20f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (positive) "✓" else "!",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private data class SecureCheckInPayload(
    val walletAddress: String,
    val issuedAtMillis: Long,
    val expiresAtMillis: Long,
    val nonce: String
)

private sealed class SecureCheckInParseResult {
    data class Valid(val payload: SecureCheckInPayload) : SecureCheckInParseResult()
    data class Invalid(val reason: String) : SecureCheckInParseResult()
}

private fun parseSecureCheckInPayload(rawContent: String): SecureCheckInParseResult {
    val parts = rawContent.trim().split("|")

    if (parts.firstOrNull() != SECURE_CHECK_IN_PREFIX) {
        return SecureCheckInParseResult.Invalid(
            "Invalid QR format. Scan the voter check-in pass from the Voter Access screen."
        )
    }

    val fields = parts
        .drop(1)
        .mapNotNull { item ->
            val key = item.substringBefore("=", missingDelimiterValue = "").trim()
            val value = item.substringAfter("=", missingDelimiterValue = "").trim()

            if (key.isBlank() || value.isBlank()) {
                null
            } else {
                key to value
            }
        }
        .toMap()

    val walletAddress = fields["wallet"].orEmpty()
    val issuedAtMillis = fields["issuedAt"]?.toLongOrNull()
    val expiresAtMillis = fields["expiresAt"]?.toLongOrNull()
    val nonce = fields["nonce"].orEmpty()

    if (!isValidEthereumAddress(walletAddress)) {
        return SecureCheckInParseResult.Invalid(
            "Invalid QR wallet address. Ask the voter to refresh their QR pass."
        )
    }

    if (issuedAtMillis == null || expiresAtMillis == null) {
        return SecureCheckInParseResult.Invalid(
            "Invalid QR timing data. Ask the voter to refresh their QR pass."
        )
    }

    if (expiresAtMillis <= issuedAtMillis) {
        return SecureCheckInParseResult.Invalid(
            "Invalid QR expiry data. Ask the voter to refresh their QR pass."
        )
    }

    if (nonce.length < 8) {
        return SecureCheckInParseResult.Invalid(
            "Invalid QR session token. Ask the voter to refresh their QR pass."
        )
    }

    val nowMillis = System.currentTimeMillis()

    if (issuedAtMillis > nowMillis + QR_CLOCK_SKEW_TOLERANCE_MILLIS) {
        return SecureCheckInParseResult.Invalid(
            "This QR pass appears to be from the future. Check device time and refresh the pass."
        )
    }

    if (nowMillis > expiresAtMillis) {
        return SecureCheckInParseResult.Invalid(
            "This QR pass has expired. Ask the voter to refresh it."
        )
    }

    return SecureCheckInParseResult.Valid(
        SecureCheckInPayload(
            walletAddress = walletAddress,
            issuedAtMillis = issuedAtMillis,
            expiresAtMillis = expiresAtMillis,
            nonce = nonce
        )
    )
}

private fun electionStatusText(election: Election): String {
    return when {
        election.isClosed() -> "Closed"
        election.isActive() -> "Active"
        else -> "Not started"
    }
}

private fun isValidEthereumAddress(address: String): Boolean {
    return Regex("^0x[a-fA-F0-9]{40}$").matches(address.trim())
}

private fun shortenWalletAddress(address: String): String {
    if (address.length <= 16) return address
    return "${address.take(10)}...${address.takeLast(8)}"
}
