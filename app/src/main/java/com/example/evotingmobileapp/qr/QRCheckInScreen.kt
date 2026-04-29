package com.example.evotingmobileapp.qr

import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.evotingmobileapp.R
import com.example.evotingmobileapp.admin.AdminViewModel
import com.example.evotingmobileapp.blockchain.DemoVoterProfile
import com.example.evotingmobileapp.blockchain.DemoWallets
import com.example.evotingmobileapp.model.Election
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
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
    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    val qrNoReadableValueMessage = stringResource(R.string.qr_check_in_error_no_readable_value)
    val qrScanSuccessMessage = stringResource(R.string.qr_check_in_scan_success)
    val qrScanCanceledMessage = stringResource(R.string.qr_check_in_scan_canceled)
    val selectElectionFirstMessage = stringResource(R.string.qr_check_in_error_select_election)
    val enterWalletFirstMessage = stringResource(R.string.qr_check_in_error_enter_wallet)
    val checkingBlockchainMessage = stringResource(R.string.qr_check_in_checking_blockchain)
    val blockchainFailedMessage = stringResource(R.string.qr_check_in_error_blockchain_failed)
    val registeredVoterAddedMessage = stringResource(R.string.qr_check_in_registered_voter_added)

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        val scannedValue = result.contents?.trim().orEmpty()

        if (scannedValue.isBlank()) {
            statusMessage = if (result.contents == null) {
                qrScanCanceledMessage
            } else {
                qrNoReadableValueMessage
            }
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

    val scanOptions = remember {
        ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("Scan voter wallet QR code")
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
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.24f),
                            MaterialTheme.colorScheme.background
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

                FancyStepCard(
                    step = "01",
                    title = stringResource(R.string.qr_check_in_select_election_title),
                    subtitle = stringResource(R.string.qr_check_in_select_election_subtitle)
                ) {
                    if (elections.isEmpty()) {
                        InfoPanel(
                            title = stringResource(R.string.qr_check_in_no_elections_title),
                            message = stringResource(R.string.qr_check_in_no_elections),
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

                FancyStepCard(
                    step = "02",
                    title = stringResource(R.string.qr_check_in_scan_wallet_title),
                    subtitle = stringResource(R.string.qr_check_in_scan_wallet_subtitle)
                ) {
                    OutlinedTextField(
                        value = voterWalletAddress,
                        onValueChange = {
                            voterWalletAddress = it
                            statusMessage = ""
                        },
                        label = { Text(text = stringResource(R.string.qr_check_in_wallet_label)) },
                        placeholder = { Text(text = stringResource(R.string.qr_check_in_wallet_placeholder)) },
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
                                statusMessage = registeredVoterAddedMessage
                                statusIsPositive = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            enabled = !isCheckingIn,
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.qr_check_in_use_registered_voter),
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
                            Text(text = stringResource(R.string.qr_check_in_clear_button))
                        }
                    }

                    DemoVoterQuickSelect(
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
                            text = stringResource(R.string.qr_check_in_scan_qr_button),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (lastScannedValue.isNotBlank()) {
                        InfoPanel(
                            title = stringResource(R.string.qr_check_in_last_scanned_wallet_title),
                            message = shortenWalletAddress(lastScannedValue),
                            positive = true
                        )
                    }
                }

                CheckInActionCard(
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

                            coroutineScope.launch {
                                snackBarHostState.showSnackbar(statusMessage)
                            }

                            return@CheckInActionCard
                        }

                        if (trimmedWalletAddress.isBlank()) {
                            statusMessage = enterWalletFirstMessage
                            statusIsPositive = false

                            coroutineScope.launch {
                                snackBarHostState.showSnackbar(statusMessage)
                            }

                            return@CheckInActionCard
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
                    enabled = !isCheckingIn,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.qr_check_in_back_button),
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
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = stringResource(R.string.qr_check_in_polling_officer_badge),
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = stringResource(R.string.qr_check_in_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = stringResource(R.string.qr_check_in_hero_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HeroMetric(
                    label = stringResource(R.string.qr_check_in_metric_elections),
                    value = electionCount.toString(),
                    modifier = Modifier.weight(1f)
                )

                HeroMetric(
                    label = stringResource(R.string.qr_check_in_metric_wallet),
                    value = if (walletAddress.isBlank()) {
                        stringResource(R.string.qr_check_in_wallet_pending)
                    } else {
                        stringResource(R.string.qr_check_in_wallet_ready)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            InfoStrip(
                text = selectedElectionTitle ?: stringResource(R.string.qr_check_in_no_election_selected_yet)
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
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.62f)
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
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.78f)
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
private fun FancyStepCard(
    step: String,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
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
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.84f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.70f)
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onSelected() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 3.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
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
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = stringResource(R.string.qr_check_in_election_id, election.id),
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (selected) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = stringResource(R.string.qr_check_in_selected_badge),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun DemoVoterQuickSelect(
    voters: List<DemoVoterProfile>,
    enabled: Boolean,
    onSelected: (DemoVoterProfile) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.qr_check_in_demo_voters_title),
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
                    text = "${voter.label} • ${shortenWalletAddress(voter.address)}",
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
    val electionText = selectedElectionTitle ?: stringResource(R.string.qr_check_in_not_selected)
    val walletText = if (voterWalletAddress.isBlank()) {
        stringResource(R.string.qr_check_in_not_entered)
    } else {
        shortenWalletAddress(voterWalletAddress.trim())
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.70f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.qr_check_in_complete_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.SemiBold
            )

            SummaryTile(
                label = stringResource(R.string.qr_check_in_summary_election_label),
                value = electionText
            )

            SummaryTile(
                label = stringResource(R.string.qr_check_in_summary_wallet_label),
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
                        stringResource(R.string.qr_check_in_checking_button)
                    } else {
                        stringResource(R.string.qr_check_in_check_in_button)
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
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.70f)
    } else {
        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.70f)
    }

    val contentColor = if (positive) {
        MaterialTheme.colorScheme.onSecondaryContainer
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

private fun shortenWalletAddress(address: String): String {
    if (address.length <= 16) return address
    return "${address.take(10)}...${address.takeLast(8)}"
}