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
import androidx.compose.foundation.layout.padding
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
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.26f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.16f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                QRHeroCard(
                    electionCount = elections.size,
                    selectedElectionTitle = selectedElection?.title,
                    walletAddress = voterWalletAddress
                )

                StepCard(
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

                StepCard(
                    step = "02",
                    title = "Load voter wallet",
                    subtitle = "Scan the voter QR pass or select a registered test wallet."
                ) {
                    InfoPanel(
                        title = "QR pass required",
                        message = "The voter should open Voter Access and show the current check-in QR pass.",
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
                        shape = RoundedCornerShape(18.dp)
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
                                .height(52.dp),
                            enabled = !isCheckingIn,
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text(
                                text = "Use registered voter",
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                voterWalletAddress = ""
                                lastScannedValue = ""
                                statusMessage = ""
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            enabled = !isCheckingIn,
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text(text = "Clear")
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
                            .height(54.dp),
                        enabled = !isCheckingIn,
                        shape = RoundedCornerShape(18.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                    ) {
                        Text(
                            text = "Scan voter QR pass",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
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
                        message = "This build validates expiry and repeated scans during the current admin session. A production version should use signed QR passes and persistent replay protection.",
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
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
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
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(
                        modifier = Modifier
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SV",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
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
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Polling check-in",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f)
                ) {
                    Text(
                        text = "ADMIN",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "Scan the voter's QR pass, confirm the election, and mark attendance on the blockchain.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HeroMetric(
                    label = "Elections",
                    value = electionCount.toString(),
                    modifier = Modifier.weight(1f)
                )

                HeroMetric(
                    label = "Wallet",
                    value = if (walletAddress.isBlank()) "Pending" else "Ready",
                    modifier = Modifier.weight(1f)
                )
            }

            InfoStrip(
                text = selectedElectionTitle ?: "No election selected yet"
            )
        }
    }
}

@Composable
private fun HeroMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.58f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun InfoStrip(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.76f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StepCard(
    step: String,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
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
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = step,
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)
            )

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
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.66f)
    }

    val border = if (selected) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.42f))
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onSelected() },
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = border
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onSelected,
                enabled = enabled
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = election.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Election ID: ${election.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (selected) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = "Selected",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
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
        Text(
            text = "Registered test voters",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        voters.forEach { voter ->
            OutlinedButton(
                onClick = { onSelected(voter) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                enabled = enabled,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "${voter.label} | ${shortenWalletAddress(voter.address)}",
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.68f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Complete check-in",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.SemiBold
            )

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
                    .height(54.dp),
                enabled = !isCheckingIn,
                shape = RoundedCornerShape(18.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
            ) {
                Text(
                    text = if (isCheckingIn) {
                        "Checking in..."
                    } else {
                        "Mark voter as checked-in"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
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

@Composable
private fun SummaryTile(
    label: String,
    value: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
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

@Composable
private fun InfoPanel(
    title: String,
    message: String,
    positive: Boolean
) {
    val containerColor = if (positive) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.58f)
    } else {
        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.62f)
    }

    val contentColor = if (positive) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onTertiaryContainer
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = containerColor
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.86f)
            )
        }
    }
}

@Composable
private fun StatusPanel(
    message: String,
    positive: Boolean
) {
    val containerColor = if (positive) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }

    val contentColor = if (positive) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = containerColor
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
            fontWeight = FontWeight.Medium
        )
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

private fun isValidEthereumAddress(address: String): Boolean {
    return Regex("^0x[a-fA-F0-9]{40}$").matches(address.trim())
}

private fun shortenWalletAddress(address: String): String {
    if (address.length <= 16) return address
    return "${address.take(10)}...${address.takeLast(8)}"
}