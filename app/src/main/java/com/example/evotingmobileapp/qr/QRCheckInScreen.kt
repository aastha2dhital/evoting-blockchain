package com.example.evotingmobileapp.qr

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
    val connectedWalletAddress by adminViewModel.connectedWalletAddress.collectAsState()
    val walletConnected by adminViewModel.walletConnected.collectAsState()

    var selectedElectionId by rememberSaveable { mutableStateOf("") }
    var voterWalletAddress by rememberSaveable { mutableStateOf("") }
    var statusMessage by rememberSaveable { mutableStateOf("") }
    var lastScannedValue by rememberSaveable { mutableStateOf("") }
    var isCheckingIn by rememberSaveable { mutableStateOf(false) }

    val selectedElection = elections.find { it.id == selectedElectionId }
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val coroutineScope = rememberCoroutineScope()

    val scannerOptions = remember {
        GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom()
            .build()
    }

    LaunchedEffect(walletConnected, connectedWalletAddress) {
        if (walletConnected && connectedWalletAddress.isNotBlank() && voterWalletAddress.isBlank()) {
            voterWalletAddress = connectedWalletAddress
        }
    }

    LaunchedEffect(elections, selectedElectionId) {
        if (selectedElectionId.isNotBlank() && selectedElection == null) {
            selectedElectionId = ""
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "QR Check-In",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Select the election, scan the voter QR code, then complete check-in using the scanned wallet address.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select Election",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                if (elections.isEmpty()) {
                    Text(
                        text = "No elections available yet. Create an election first.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    elections.forEach { election ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedElectionId == election.id,
                                    onClick = {
                                        if (!isCheckingIn) {
                                            selectedElectionId = election.id
                                        }
                                    },
                                    enabled = !isCheckingIn
                                )

                                Column(
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = election.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Election ID: ${election.id}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Voter QR Scan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                if (walletConnected && connectedWalletAddress.isNotBlank()) {
                    Text(
                        text = "Current shared wallet: ${shortenWalletAddress(connectedWalletAddress)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedTextField(
                    value = voterWalletAddress,
                    onValueChange = { voterWalletAddress = it },
                    label = { Text("Wallet Address") },
                    placeholder = { Text("Scan QR or type wallet address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCheckingIn
                )

                if (lastScannedValue.isNotBlank()) {
                    Text(
                        text = "Last scanned value: $lastScannedValue",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (activity == null) {
                                statusMessage =
                                    "Scanner could not start because this screen is not attached to an activity."
                                return@Button
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
                                        adminViewModel.setConnectedWalletAddress(scannedValue)
                                        statusMessage =
                                            "QR scan successful. Wallet address loaded for check-in."
                                    }
                                }
                                .addOnCanceledListener {
                                    statusMessage = "QR scan was canceled."
                                }
                                .addOnFailureListener { exception: Exception ->
                                    statusMessage = exception.message
                                        ?: "QR scanning failed. Please try again."
                                }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isCheckingIn
                    ) {
                        Text("Scan QR Code")
                    }

                    OutlinedButton(
                        onClick = {
                            if (connectedWalletAddress.isNotBlank()) {
                                voterWalletAddress = connectedWalletAddress
                                statusMessage = "Shared wallet restored into the wallet field."
                            } else {
                                statusMessage = "No shared wallet is connected yet."
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isCheckingIn
                    ) {
                        Text("Use Shared Wallet")
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Complete Check-In",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                selectedElection?.let { election ->
                    Text(
                        text = "Selected election: ${election.title}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } ?: Text(
                    text = "No election selected yet.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Button(
                    onClick = {
                        val trimmedWalletAddress = voterWalletAddress.trim()

                        if (selectedElection == null) {
                            statusMessage = "Please select an election first."
                            return@Button
                        }

                        if (trimmedWalletAddress.isBlank()) {
                            statusMessage = "Please scan or enter a wallet address first."
                            return@Button
                        }

                        adminViewModel.setConnectedWalletAddress(trimmedWalletAddress)
                        isCheckingIn = true
                        statusMessage = "Checking voter in on blockchain..."

                        coroutineScope.launch {
                            val result = runCatching {
                                withContext(Dispatchers.IO) {
                                    adminViewModel.checkInVoter(
                                        electionId = selectedElection.id,
                                        voterId = trimmedWalletAddress
                                    )
                                }
                            }

                            isCheckingIn = false

                            result.onSuccess { message ->
                                statusMessage = message
                            }.onFailure { exception ->
                                statusMessage = exception.message ?: "Blockchain check-in failed."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCheckingIn
                ) {
                    Text(if (isCheckingIn) "Checking In..." else "Check In Voter")
                }

                if (statusMessage.isNotBlank()) {
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (
                            statusMessage.contains("success", ignoreCase = true) ||
                            statusMessage.contains("checked in", ignoreCase = true)
                        ) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }

        TextButton(
            onClick = { navController.popBackStack() },
            enabled = !isCheckingIn
        ) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(8.dp))
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