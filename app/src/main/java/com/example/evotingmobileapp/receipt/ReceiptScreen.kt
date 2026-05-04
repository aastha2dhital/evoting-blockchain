package com.example.evotingmobileapp.receipt

import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.evotingmobileapp.R
import com.example.evotingmobileapp.admin.AdminViewModel
import com.example.evotingmobileapp.blockchain.OnChainTransactionVerification
import com.example.evotingmobileapp.model.VoteReceipt
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.EnumMap
import java.util.Locale

@Composable
fun ReceiptScreen(
    navController: NavHostController,
    adminViewModel: AdminViewModel,
    initialTransactionHash: String? = null
) {
    val context = LocalContext.current

    val latestReceipt by adminViewModel.latestReceipt.collectAsState()
    val voteReceipts by adminViewModel.voteReceipts.collectAsState()
    val verifiedOnChainTransaction by adminViewModel.verifiedOnChainTransaction.collectAsState()
    val verificationInProgress by adminViewModel.verificationInProgress.collectAsState()
    val verificationError by adminViewModel.verificationError.collectAsState()

    var transactionHashInput by rememberSaveable {
        mutableStateOf(initialTransactionHash?.trim().orEmpty())
    }

    var inputError by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var hasAttemptedVerification by rememberSaveable {
        mutableStateOf(!initialTransactionHash.isNullOrBlank())
    }

    var lastSubmittedTransactionHash by rememberSaveable {
        mutableStateOf(initialTransactionHash?.trim().orEmpty())
    }

    val enterHashError = stringResource(R.string.receipt_error_enter_hash)

    val receiptQrScanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        val scannedContent = result.contents?.trim().orEmpty()

        if (scannedContent.isBlank()) {
            inputError = "Receipt QR scan cancelled or no readable transaction hash was found."
        } else {
            val extractedHash = extractTransactionHashFromQr(scannedContent)

            transactionHashInput = extractedHash
            inputError = null
            hasAttemptedVerification = false
            lastSubmittedTransactionHash = ""
            adminViewModel.clearOnChainVerification()
        }
    }

    LaunchedEffect(initialTransactionHash) {
        val hash = initialTransactionHash?.trim().orEmpty()

        if (hash.isNotBlank()) {
            transactionHashInput = hash
            lastSubmittedTransactionHash = hash
            inputError = null
            hasAttemptedVerification = true

            adminViewModel.clearOnChainVerification()
            adminViewModel.verifyTransactionReceiptOnChain(
                context = context,
                transactionHash = hash
            )
        }
    }

    val trimmedHash = transactionHashInput.trim()

    val matchedLocalReceipt = if (trimmedHash.isNotBlank()) {
        voteReceipts.firstOrNull { receipt ->
            receipt.transactionHash.equals(trimmedHash, ignoreCase = true)
        }
    } else {
        null
    }

    val shouldShowSuccess =
        hasAttemptedVerification &&
                !verificationInProgress &&
                verificationError.isNullOrBlank() &&
                verifiedOnChainTransaction != null &&
                lastSubmittedTransactionHash.equals(trimmedHash, ignoreCase = true)

    val shouldShowError =
        hasAttemptedVerification &&
                !verificationInProgress &&
                !verificationError.isNullOrBlank() &&
                inputError == null

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f),
            MaterialTheme.colorScheme.background
        )
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            ReceiptHeroCard()

            VerificationInputCard(
                transactionHashInput = transactionHashInput,
                inputError = inputError,
                verificationInProgress = verificationInProgress,
                onValueChanged = { newValue ->
                    transactionHashInput = newValue
                    inputError = null

                    if (hasAttemptedVerification || verifiedOnChainTransaction != null || !verificationError.isNullOrBlank()) {
                        hasAttemptedVerification = false
                        lastSubmittedTransactionHash = ""
                        adminViewModel.clearOnChainVerification()
                    }
                },
                onClear = {
                    transactionHashInput = ""
                    inputError = null
                    hasAttemptedVerification = false
                    lastSubmittedTransactionHash = ""
                    adminViewModel.clearOnChainVerification()
                },
                onScanQr = {
                    val options = ScanOptions().apply {
                        setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                        setPrompt("Scan transaction hash QR code")
                        setBeepEnabled(true)
                        setOrientationLocked(false)
                        setBarcodeImageEnabled(false)
                    }

                    receiptQrScanLauncher.launch(options)
                },
                onShareHash = {
                    val hashToShare = transactionHashInput.trim()

                    if (hashToShare.isBlank()) {
                        inputError = enterHashError
                    } else {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "EVoting transaction receipt")
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "SecureVote Nepal transaction receipt hash:\n$hashToShare\n\nVerify this hash in the EVoting receipt verification screen."
                            )
                        }

                        context.startActivity(
                            Intent.createChooser(shareIntent, "Share transaction hash")
                        )
                    }
                },
                onVerify = {
                    if (trimmedHash.isBlank()) {
                        inputError = enterHashError
                        hasAttemptedVerification = false
                        lastSubmittedTransactionHash = ""
                        adminViewModel.clearOnChainVerification()
                    } else {
                        inputError = null
                        hasAttemptedVerification = true
                        lastSubmittedTransactionHash = trimmedHash
                        adminViewModel.clearOnChainVerification()
                        adminViewModel.verifyTransactionReceiptOnChain(
                            context = context,
                            transactionHash = trimmedHash
                        )
                    }
                }
            )

            when {
                verificationInProgress -> {
                    VerificationLoadingState(
                        transactionHash = lastSubmittedTransactionHash.ifBlank { trimmedHash }
                    )
                }

                shouldShowSuccess && verifiedOnChainTransaction != null -> {
                    StatusBanner(
                        title = stringResource(R.string.receipt_verification_success_title),
                        message = stringResource(R.string.receipt_verification_success_message),
                        positive = true
                    )

                    OnChainVerificationCard(
                        verification = verifiedOnChainTransaction!!
                    )

                    if (matchedLocalReceipt != null) {
                        ReceiptDetailsCard(
                            title = stringResource(R.string.receipt_matched_local_title),
                            supportingText = stringResource(R.string.receipt_matched_local_subtitle),
                            receipt = matchedLocalReceipt
                        )
                    } else {
                        NoLocalReceiptMatchCard()
                    }
                }

                shouldShowError -> {
                    VerificationErrorCard(
                        message = verificationError.orEmpty()
                    )

                    if (matchedLocalReceipt != null) {
                        ReceiptDetailsCard(
                            title = stringResource(R.string.receipt_local_match_title),
                            supportingText = stringResource(R.string.receipt_local_match_subtitle),
                            receipt = matchedLocalReceipt
                        )
                    }
                }

                latestReceipt != null -> {
                    StatusBanner(
                        title = stringResource(R.string.receipt_latest_saved_title),
                        message = stringResource(R.string.receipt_latest_saved_message),
                        positive = true
                    )

                    ReceiptDetailsCard(
                        title = stringResource(R.string.receipt_latest_local_title),
                        supportingText = stringResource(R.string.receipt_latest_local_subtitle),
                        receipt = latestReceipt!!
                    )
                }

                else -> {
                    EmptyReceiptState()
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(text = stringResource(R.string.receipt_back_button))
                }

                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.receipt_done_button),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ReceiptHeroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f)
            ) {
                Text(
                    text = "Blockchain Audit Receipt",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Text(
                text = stringResource(R.string.receipt_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = stringResource(R.string.receipt_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f)
            )

            PillColumn(
                items = listOf(
                    stringResource(R.string.receipt_step_enter_hash),
                    stringResource(R.string.receipt_step_verify_on_chain),
                    stringResource(R.string.receipt_step_review_receipt)
                )
            )
        }
    }
}

@Composable
private fun VerificationInputCard(
    transactionHashInput: String,
    inputError: String?,
    verificationInProgress: Boolean,
    onValueChanged: (String) -> Unit,
    onClear: () -> Unit,
    onScanQr: () -> Unit,
    onShareHash: () -> Unit,
    onVerify: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionTitle(
                title = stringResource(R.string.receipt_verify_hash_title),
                subtitle = stringResource(R.string.receipt_verify_hash_subtitle)
            )

            OutlinedTextField(
                value = transactionHashInput,
                onValueChange = onValueChanged,
                label = { Text(text = stringResource(R.string.receipt_transaction_hash_label)) },
                placeholder = { Text(text = stringResource(R.string.receipt_transaction_hash_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                isError = inputError != null,
                supportingText = {
                    Text(
                        text = inputError
                            ?: stringResource(R.string.receipt_transaction_hash_supporting)
                    )
                },
                shape = RoundedCornerShape(18.dp),
                enabled = !verificationInProgress
            )

            if (transactionHashInput.trim().isNotBlank()) {
                HashPreviewPanel(hash = transactionHashInput.trim())
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onScanQr,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    enabled = !verificationInProgress
                ) {
                    Text(
                        text = "Scan TX QR",
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onShareHash,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    enabled = !verificationInProgress && transactionHashInput.trim().isNotBlank()
                ) {
                    Text(
                        text = "Share Hash",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onClear,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    enabled = !verificationInProgress
                ) {
                    Text(text = stringResource(R.string.receipt_clear_button))
                }

                Button(
                    onClick = onVerify,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    enabled = !verificationInProgress,
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = if (verificationInProgress) {
                            stringResource(R.string.receipt_verifying_button)
                        } else {
                            stringResource(R.string.receipt_verify_button)
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun HashPreviewPanel(hash: String) {
    val qrBitmap = remember(hash) {
        generateTransactionHashQrBitmap(hash)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.50f)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Hash ready for verification",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = shortenHash(hash),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Surface(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 3.dp
            ) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "Transaction hash QR code",
                    modifier = Modifier
                        .size(184.dp)
                        .padding(14.dp)
                )
            }

            Text(
                text = "Scan this QR from another device to verify the same blockchain transaction hash.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun VerificationLoadingState(
    transactionHash: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()

            Text(
                text = stringResource(R.string.receipt_verifying_blockchain),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            if (transactionHash.isNotBlank()) {
                Text(
                    text = shortenHash(transactionHash),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = "Checking the transaction receipt directly from the blockchain network.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatusBanner(
    title: String,
    message: String,
    positive: Boolean
) {
    val containerColor = if (positive) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }

    val contentColor = if (positive) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = contentColor.copy(alpha = 0.16f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (positive) "✓" else "!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = contentColor
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = contentColor
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor
                )
            }
        }
    }
}

@Composable
private fun OnChainVerificationCard(
    verification: OnChainTransactionVerification
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionTitle(
                title = stringResource(R.string.receipt_verified_on_chain_title),
                subtitle = stringResource(R.string.receipt_verified_on_chain_subtitle)
            )

            AuditSummaryPanel(
                status = if (verification.status == "0x1") {
                    stringResource(R.string.receipt_status_success)
                } else {
                    stringResource(R.string.receipt_status_failed)
                },
                blockNumber = verification.blockNumber.toString(),
                gasUsed = verification.gasUsed.toString()
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            ReceiptRow(
                label = stringResource(R.string.receipt_label_transaction_hash),
                value = verification.transactionHash,
                mono = true
            )

            ReceiptRow(
                label = stringResource(R.string.receipt_label_block_number),
                value = verification.blockNumber.toString()
            )

            ReceiptRow(
                label = stringResource(R.string.receipt_label_status),
                value = if (verification.status == "0x1") {
                    stringResource(R.string.receipt_status_success)
                } else {
                    stringResource(R.string.receipt_status_failed)
                }
            )

            ReceiptRow(
                label = stringResource(R.string.receipt_label_from_address),
                value = verification.fromAddress,
                mono = true
            )

            ReceiptRow(
                label = stringResource(R.string.receipt_label_to_address),
                value = verification.toAddress ?: "-",
                mono = true
            )

            ReceiptRow(
                label = stringResource(R.string.receipt_label_gas_used),
                value = verification.gasUsed.toString()
            )
        }
    }
}

@Composable
private fun AuditSummaryPanel(
    status: String,
    blockNumber: String,
    gasUsed: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "On-chain confirmation",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.ExtraBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MiniAuditTile(
                    modifier = Modifier.weight(1f),
                    label = "Status",
                    value = status
                )

                MiniAuditTile(
                    modifier = Modifier.weight(1f),
                    label = "Block",
                    value = blockNumber
                )
            }

            MiniAuditTile(
                modifier = Modifier.fillMaxWidth(),
                label = "Gas used",
                value = gasUsed
            )
        }
    }
}

@Composable
private fun MiniAuditTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ReceiptDetailsCard(
    title: String,
    supportingText: String,
    receipt: VoteReceipt
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionTitle(
                title = title,
                subtitle = supportingText
            )

            LocalReceiptSummaryPanel(receipt = receipt)

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            ReceiptRow(
                label = stringResource(R.string.receipt_label_election_id),
                value = receipt.electionId
            )

            ReceiptRow(
                label = stringResource(R.string.receipt_label_election_title),
                value = receipt.electionTitle
            )

            ReceiptRow(
                label = stringResource(R.string.receipt_label_wallet_address),
                value = receipt.voterId,
                mono = true
            )

            ReceiptRow(
                label = stringResource(R.string.receipt_label_candidate),
                value = receipt.candidateName
            )

            ReceiptRow(
                label = stringResource(R.string.receipt_label_timestamp),
                value = formatTimestamp(receipt.timestamp)
            )

            ReceiptRow(
                label = stringResource(R.string.receipt_label_transaction_hash),
                value = receipt.transactionHash,
                mono = true
            )
        }
    }
}

@Composable
private fun LocalReceiptSummaryPanel(
    receipt: VoteReceipt
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.82f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Vote receipt summary",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Election: ${receipt.electionTitle}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Selected candidate: ${receipt.candidateName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Receipt hash: ${shortenHash(receipt.transactionHash)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.84f),
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun EmptyReceiptState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "#",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Text(
                text = stringResource(R.string.receipt_empty_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.receipt_empty_message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NoLocalReceiptMatchCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.receipt_no_local_match_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Text(
                text = stringResource(R.string.receipt_no_local_match_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun VerificationErrorCard(
    message: String
) {
    StatusBanner(
        title = stringResource(R.string.receipt_verification_failed_title),
        message = message,
        positive = false
    )
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
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    mono: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.78f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                fontFamily = if (mono) FontFamily.Monospace else FontFamily.Default,
                maxLines = if (mono) 3 else 2,
                overflow = TextOverflow.Ellipsis
            )
        }
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

private fun generateTransactionHashQrBitmap(content: String, size: Int = 900): Bitmap {
    val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
        put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M)
        put(EncodeHintType.MARGIN, 2)
        put(EncodeHintType.CHARACTER_SET, "UTF-8")
    }

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
                android.graphics.Color.BLACK
            } else {
                android.graphics.Color.WHITE
            }
        }
    }

    return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
        setPixels(pixels, 0, size, 0, 0, size, size)
    }
}

private fun extractTransactionHashFromQr(rawContent: String): String {
    val transactionHashPattern = Regex("0x[a-fA-F0-9]{64}")
    return transactionHashPattern.find(rawContent.trim())?.value ?: rawContent.trim()
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

private fun shortenHash(hash: String): String {
    if (hash.length <= 18) return hash
    return "${hash.take(10)}...${hash.takeLast(8)}"
}