package com.example.evotingmobileapp.qr

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.evotingmobileapp.admin.AdminViewModel
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    var lastScannedValue by rememberSaveable { mutableStateOf("") }
    var isCheckingIn by rememberSaveable { mutableStateOf(false) }

    val selectedElection = elections.find { it.id == selectedElectionId }
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    val scannerOptions = remember {
        GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom()
            .build()
    }

    LaunchedEffect(elections, selectedElectionId) {
        if (selectedElectionId.isNotBlank() && selectedElection == null) {
            selectedElectionId = ""
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            QRCheckInHeroCard()

            ElectionSelectionCard(
                elections = elections,
                selectedElectionId = selectedElectionId,
                onElectionSelected = { electionId ->
                    if (!isCheckingIn) {
                        selectedElectionId = electionId
                    }
                },
                isBusy = isCheckingIn
            )

            ScanWalletCard(
                voterWalletAddress = voterWalletAddress,
                lastScannedValue = lastScannedValue,
                isBusy = isCheckingIn,
                onWalletAddressChanged = { voterWalletAddress = it },
                onScanQr = {
                    if (activity == null) {
                        statusMessage =
                            "Scanner could not start because this screen is not attached to an activity."
                        coroutineScope.launch { snackBarHostState.showSnackbar(statusMessage) }
                        return@ScanWalletCard
                    }

                    val scanner = GmsBarcodeScanning.getClient(activity, scannerOptions)

                    scanner.startScan()
                        .addOnSuccessListener { barcode: Barcode ->
                            val scannedValue = barcode.rawValue?.trim().orEmpty()

                            if (scannedValue.isBlank()) {
                                statusMessage = "QR code scanned, but no readable value was returned."
                            } else {
                                voterWalletAddress = scannedValue
                                lastScannedValue = scannedValue
                                statusMessage =
                                    "QR scan successful. Voter wallet address loaded for check-in."
                            }

                            coroutineScope.launch {
                                snackBarHostState.showSnackbar(statusMessage)
                            }
                        }
                        .addOnCanceledListener {
                            statusMessage = "QR scan was canceled."
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar(statusMessage)
                            }
                        }
                        .addOnFailureListener { exception: Exception ->
                            statusMessage = exception.message ?: "QR scanning failed. Please try again."
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar(statusMessage)
                            }
                        }
                }
            )

            CheckInSummaryCard(
                selectedElectionTitle = selectedElection?.title,
                voterWalletAddress = voterWalletAddress,
                statusMessage = statusMessage,
                isCheckingIn = isCheckingIn,
                onCheckIn = {
                    val trimmedWalletAddress = voterWalletAddress.trim()

                    if (selectedElection == null) {
                        statusMessage = "Please select an election first."
                        coroutineScope.launch { snackBarHostState.showSnackbar(statusMessage) }
                        return@CheckInSummaryCard
                    }

                    if (trimmedWalletAddress.isBlank()) {
                        statusMessage = "Please scan or enter a voter wallet address first."
                        coroutineScope.launch { snackBarHostState.showSnackbar(statusMessage) }
                        return@CheckInSummaryCard
                    }

                    isCheckingIn = true
                    statusMessage = "Checking voter in on blockchain..."

                    coroutineScope.launch {
                        snackBarHostState.showSnackbar(statusMessage)

                        val result = withContext(Dispatchers.IO) {
                            adminViewModel.checkInVoterOnChain(
                                context = context,
                                electionId = selectedElection.id,
                                voterWalletAddress = trimmedWalletAddress
                            )
                        }

                        isCheckingIn = false

                        result.fold(
                            onSuccess = { message ->
                                statusMessage = message
                                snackBarHostState.showSnackbar(message)
                            },
                            onFailure = { exception ->
                                statusMessage = exception.message ?: "Blockchain check-in failed."
                                snackBarHostState.showSnackbar(statusMessage)
                            }
                        )
                    }
                }
            )

            TextButton(
                onClick = { navController.popBackStack() },
                enabled = !isCheckingIn
            ) {
                Text("Back")
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun QRCheckInHeroCard() {
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "QR Check-In",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Select the election, scan the voter QR, confirm the wallet address, and complete blockchain-backed voter check-in.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f)
            )

            PillColumn(
                items = listOf("1. Select Election", "2. Scan Wallet QR", "3. Complete Check-In")
            )
        }
    }
}

@Composable
private fun ElectionSelectionCard(
    elections: List<com.example.evotingmobileapp.model.Election>,
    selectedElectionId: String,
    onElectionSelected: (String) -> Unit,
    isBusy: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionTitle(
                title = "Select Election",
                subtitle = "Choose the election for this voter check-in."
            )

            if (elections.isEmpty()) {
                StatusInfoBadge(
                    text = "No elections available yet. Create an election first.",
                    positive = false,
                    usePrimaryText = false
                )
            } else {
                elections.forEach { election ->
                    SelectionCard(
                        title = election.title,
                        subtitle = "Election ID: ${election.id}",
                        selected = selectedElectionId == election.id,
                        onSelected = { onElectionSelected(election.id) },
                        enabled = !isBusy
                    )
                }
            }
        }
    }
}

@Composable
private fun ScanWalletCard(
    voterWalletAddress: String,
    lastScannedValue: String,
    isBusy: Boolean,
    onWalletAddressChanged: (String) -> Unit,
    onScanQr: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionTitle(
                title = "Scan Voter QR",
                subtitle = "Use the QR scanner or enter the voter wallet address manually."
            )

            OutlinedTextField(
                value = voterWalletAddress,
                onValueChange = onWalletAddressChanged,
                label = { Text("Voter Wallet Address") },
                placeholder = { Text("Scan QR or type voter wallet address") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isBusy
            )

            if (lastScannedValue.isNotBlank()) {
                StatusInfoBadge(
                    text = "Last scanned value: $lastScannedValue",
                    positive = true,
                    usePrimaryText = false
                )
            }

            Button(
                onClick = onScanQr,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isBusy,
                shape = RoundedCornerShape(18.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Scan QR Code",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun CheckInSummaryCard(
    selectedElectionTitle: String?,
    voterWalletAddress: String,
    statusMessage: String,
    isCheckingIn: Boolean,
    onCheckIn: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionTitle(
                title = "Complete Check-In",
                subtitle = "Review the selected election and scanned voter wallet before final check-in."
            )

            PillColumn(
                items = listOf(
                    "Election: ${selectedElectionTitle ?: "Not selected"}",
                    "Wallet: ${if (voterWalletAddress.isBlank()) "Not entered" else shortenWalletAddress(voterWalletAddress.trim())}"
                )
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Button(
                onClick = onCheckIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isCheckingIn,
                shape = RoundedCornerShape(18.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = if (isCheckingIn) "Checking In..." else "Check In Voter",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            if (statusMessage.isNotBlank()) {
                StatusInfoBadge(
                    text = statusMessage,
                    positive = statusMessage.contains("success", ignoreCase = true) ||
                            statusMessage.contains("checked in", ignoreCase = true),
                    usePrimaryText = true
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(
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
private fun SelectionCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onSelected: () -> Unit,
    enabled: Boolean
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 4.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onSelected,
                enabled = enabled
            )

            Column(
                modifier = Modifier.padding(start = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.82f)
                )
            }
        }
    }
}

@Composable
private fun StatusInfoBadge(
    text: String,
    positive: Boolean,
    usePrimaryText: Boolean
) {
    val containerColor = if (positive) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.tertiaryContainer
    }

    val contentColor = when {
        usePrimaryText && positive -> MaterialTheme.colorScheme.primary
        positive -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onTertiaryContainer
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = containerColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor
        )
    }
}

@Composable
private fun PillColumn(
    items: List<String>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEach { item ->
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f)
            ) {
                Text(
                    text = item,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun shortenWalletAddress(address: String): String {
    if (address.length <= 16) return address
    return "${address.take(10)}...${address.takeLast(8)}"
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}