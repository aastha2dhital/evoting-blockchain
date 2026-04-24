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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.evotingmobileapp.R
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
    var statusIsPositive by rememberSaveable { mutableStateOf(false) }
    var lastScannedValue by rememberSaveable { mutableStateOf("") }
    var isCheckingIn by rememberSaveable { mutableStateOf(false) }

    val selectedElection = elections.find { it.id == selectedElectionId }
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    val scannerNoActivityMessage = stringResource(R.string.qr_check_in_scanner_no_activity)
    val qrNoReadableValueMessage = stringResource(R.string.qr_check_in_error_no_readable_value)
    val qrScanSuccessMessage = stringResource(R.string.qr_check_in_scan_success)
    val qrScanCanceledMessage = stringResource(R.string.qr_check_in_scan_canceled)
    val qrScanFailedMessage = stringResource(R.string.qr_check_in_scan_failed)
    val selectElectionFirstMessage = stringResource(R.string.qr_check_in_error_select_election)
    val enterWalletFirstMessage = stringResource(R.string.qr_check_in_error_enter_wallet)
    val checkingBlockchainMessage = stringResource(R.string.qr_check_in_checking_blockchain)
    val blockchainFailedMessage = stringResource(R.string.qr_check_in_error_blockchain_failed)

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
                        statusMessage = scannerNoActivityMessage
                        statusIsPositive = false
                        coroutineScope.launch { snackBarHostState.showSnackbar(statusMessage) }
                        return@ScanWalletCard
                    }

                    val scanner = GmsBarcodeScanning.getClient(activity, scannerOptions)

                    scanner.startScan()
                        .addOnSuccessListener { barcode: Barcode ->
                            val scannedValue = barcode.rawValue?.trim().orEmpty()

                            if (scannedValue.isBlank()) {
                                statusMessage = qrNoReadableValueMessage
                                statusIsPositive = false
                            } else {
                                voterWalletAddress = scannedValue
                                lastScannedValue = scannedValue
                                statusMessage = qrScanSuccessMessage
                                statusIsPositive = true
                            }

                            coroutineScope.launch {
                                snackBarHostState.showSnackbar(statusMessage)
                            }
                        }
                        .addOnCanceledListener {
                            statusMessage = qrScanCanceledMessage
                            statusIsPositive = false
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar(statusMessage)
                            }
                        }
                        .addOnFailureListener { exception: Exception ->
                            statusMessage = exception.message ?: qrScanFailedMessage
                            statusIsPositive = false
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
                statusIsPositive = statusIsPositive,
                isCheckingIn = isCheckingIn,
                onCheckIn = {
                    val trimmedWalletAddress = voterWalletAddress.trim()

                    if (selectedElection == null) {
                        statusMessage = selectElectionFirstMessage
                        statusIsPositive = false
                        coroutineScope.launch { snackBarHostState.showSnackbar(statusMessage) }
                        return@CheckInSummaryCard
                    }

                    if (trimmedWalletAddress.isBlank()) {
                        statusMessage = enterWalletFirstMessage
                        statusIsPositive = false
                        coroutineScope.launch { snackBarHostState.showSnackbar(statusMessage) }
                        return@CheckInSummaryCard
                    }

                    isCheckingIn = true
                    statusMessage = checkingBlockchainMessage
                    statusIsPositive = false

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
                                statusIsPositive = true
                                snackBarHostState.showSnackbar(message)
                            },
                            onFailure = { exception ->
                                statusMessage = exception.message ?: blockchainFailedMessage
                                statusIsPositive = false
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
                Text(text = stringResource(R.string.qr_check_in_back_button))
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
                text = stringResource(R.string.qr_check_in_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = stringResource(R.string.qr_check_in_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f)
            )

            PillColumn(
                items = listOf(
                    stringResource(R.string.qr_check_in_step_select_election),
                    stringResource(R.string.qr_check_in_step_scan_wallet),
                    stringResource(R.string.qr_check_in_step_complete_check_in)
                )
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
                title = stringResource(R.string.qr_check_in_select_election_title),
                subtitle = stringResource(R.string.qr_check_in_select_election_subtitle)
            )

            if (elections.isEmpty()) {
                StatusInfoBadge(
                    text = stringResource(R.string.qr_check_in_no_elections),
                    positive = false,
                    usePrimaryText = false
                )
            } else {
                elections.forEach { election ->
                    SelectionCard(
                        title = election.title,
                        subtitle = stringResource(R.string.qr_check_in_election_id, election.id),
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
                title = stringResource(R.string.qr_check_in_scan_wallet_title),
                subtitle = stringResource(R.string.qr_check_in_scan_wallet_subtitle)
            )

            OutlinedTextField(
                value = voterWalletAddress,
                onValueChange = onWalletAddressChanged,
                label = { Text(text = stringResource(R.string.qr_check_in_wallet_label)) },
                placeholder = { Text(text = stringResource(R.string.qr_check_in_wallet_placeholder)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isBusy
            )

            if (lastScannedValue.isNotBlank()) {
                StatusInfoBadge(
                    text = stringResource(R.string.qr_check_in_last_scanned, lastScannedValue),
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
                    text = stringResource(R.string.qr_check_in_scan_qr_button),
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
    statusIsPositive: Boolean,
    isCheckingIn: Boolean,
    onCheckIn: () -> Unit
) {
    val electionText = selectedElectionTitle ?: stringResource(R.string.qr_check_in_not_selected)
    val walletText = if (voterWalletAddress.isBlank()) {
        stringResource(R.string.qr_check_in_not_entered)
    } else {
        shortenWalletAddress(voterWalletAddress.trim())
    }

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
                title = stringResource(R.string.qr_check_in_complete_title),
                subtitle = stringResource(R.string.qr_check_in_complete_subtitle)
            )

            PillColumn(
                items = listOf(
                    stringResource(R.string.qr_check_in_summary_election, electionText),
                    stringResource(R.string.qr_check_in_summary_wallet, walletText)
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
                    text = if (isCheckingIn) {
                        stringResource(R.string.qr_check_in_checking_button)
                    } else {
                        stringResource(R.string.qr_check_in_check_in_button)
                    },
                    style = MaterialTheme.typography.labelLarge
                )
            }

            if (statusMessage.isNotBlank()) {
                StatusInfoBadge(
                    text = statusMessage,
                    positive = statusIsPositive,
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