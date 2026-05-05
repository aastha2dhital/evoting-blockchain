package com.example.evotingmobileapp.receipt

import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
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

private val ReceiptNavy = Color(0xFF07133A)
private val ReceiptDeepBlue = Color(0xFF102A70)
private val ReceiptIndigo = Color(0xFF4F32F6)
private val ReceiptPurple = Color(0xFF7C3AED)
private val ReceiptBlue = Color(0xFF1479FF)
private val ReceiptTeal = Color(0xFF00B8A9)
private val ReceiptCoral = Color(0xFFFF5C7A)
private val ReceiptAmber = Color(0xFFFFB020)
private val ReceiptGreen = Color(0xFF17C964)
private val ReceiptRed = Color(0xFFE5484D)
private val ReceiptCard = Color(0xFFF6F8FF)
private val ReceiptPanel = Color(0xFFEAF1FF)
private val ReceiptInk = Color(0xFF081229)
private val ReceiptMuted = Color(0xFF59627E)
private val ReceiptLine = Color(0xFFD6DDF4)

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

    val receiptQrScanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        val scannedContent = result.contents?.trim().orEmpty()

        if (scannedContent.isBlank()) {
            inputError = "No readable transaction hash was found."
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

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ReceiptNavy,
                            ReceiptDeepBlue,
                            Color(0xFF3155D8),
                            Color(0xFFD8ECFF)
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            ReceiptDecorativeBackground()

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
                ReceiptHeroCard()

                VerificationInputCard(
                    transactionHashInput = transactionHashInput,
                    inputError = inputError,
                    verificationInProgress = verificationInProgress,
                    onValueChanged = { newValue ->
                        transactionHashInput = newValue
                        inputError = null

                        if (
                            hasAttemptedVerification ||
                            verifiedOnChainTransaction != null ||
                            !verificationError.isNullOrBlank()
                        ) {
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
                            setPrompt("Scan transaction hash QR")
                            setBeepEnabled(true)
                            setOrientationLocked(false)
                            setBarcodeImageEnabled(false)
                        }

                        receiptQrScanLauncher.launch(options)
                    },
                    onShareHash = {
                        val hashToShare = transactionHashInput.trim()

                        if (hashToShare.isBlank()) {
                            inputError = "Enter a transaction hash first."
                        } else {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "SecureVote Nepal receipt")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "SecureVote Nepal transaction hash:\n$hashToShare\n\nVerify this hash in the receipt verification screen."
                                )
                            }

                            context.startActivity(
                                Intent.createChooser(shareIntent, "Share transaction hash")
                            )
                        }
                    },
                    onVerify = {
                        if (trimmedHash.isBlank()) {
                            inputError = "Enter a transaction hash first."
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
                            title = "Receipt verified",
                            message = "This transaction was found on the blockchain and matches a successful contract transaction.",
                            positive = true
                        )

                        OnChainVerificationCard(
                            verification = verifiedOnChainTransaction!!
                        )

                        if (matchedLocalReceipt != null) {
                            ReceiptDetailsCard(
                                title = "Matched local vote receipt",
                                supportingText = "This device also has the saved voting receipt for this hash.",
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
                                title = "Local receipt found",
                                supportingText = "A saved receipt matches this hash, but on-chain verification did not complete successfully.",
                                receipt = matchedLocalReceipt
                            )
                        }
                    }

                    latestReceipt != null -> {
                        StatusBanner(
                            title = "Latest receipt available",
                            message = "Your most recent vote receipt is shown below.",
                            positive = true
                        )

                        ReceiptDetailsCard(
                            title = "Latest local receipt",
                            supportingText = "Use the transaction hash below for audit verification.",
                            receipt = latestReceipt!!
                        )
                    }

                    else -> {
                        EmptyReceiptState()
                    }
                }

                ReceiptFooterActions(
                    verificationInProgress = verificationInProgress,
                    onBack = { navController.popBackStack() },
                    onDone = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun ReceiptDecorativeBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .size(230.dp)
                .offset(x = (-90).dp, y = 40.dp),
            shape = CircleShape,
            color = ReceiptTeal.copy(alpha = 0.16f)
        ) {}

        Surface(
            modifier = Modifier
                .size(190.dp)
                .align(Alignment.TopEnd)
                .offset(x = 72.dp, y = 88.dp),
            shape = CircleShape,
            color = ReceiptCoral.copy(alpha = 0.16f)
        ) {}

        Surface(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 98.dp, y = 36.dp),
            shape = CircleShape,
            color = ReceiptAmber.copy(alpha = 0.14f)
        ) {}

        Surface(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-130).dp, y = 110.dp),
            shape = CircleShape,
            color = ReceiptPurple.copy(alpha = 0.15f)
        ) {}
    }
}

@Composable
private fun ReceiptHeroCard() {
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
                color = ReceiptTeal.copy(alpha = 0.12f)
            ) {}

            Surface(
                modifier = Modifier
                    .size(74.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 30.dp, y = 22.dp),
                shape = CircleShape,
                color = ReceiptCoral.copy(alpha = 0.10f)
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
                        color = ReceiptIndigo,
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
                            color = ReceiptIndigo,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Blockchain receipt",
                            style = MaterialTheme.typography.headlineSmall,
                            color = ReceiptInk,
                            fontWeight = FontWeight.Black
                        )
                    }

                    HeaderChip(text = "VERIFY")
                }

                Text(
                    text = "View, share, scan, and verify the blockchain transaction hash for a submitted vote.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ReceiptMuted
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MiniHeroTile(
                        modifier = Modifier.weight(1f),
                        title = "Hash",
                        subtitle = "Receipt proof",
                        accent = ReceiptBlue
                    )

                    MiniHeroTile(
                        modifier = Modifier.weight(1f),
                        title = "Chain",
                        subtitle = "Audit check",
                        accent = ReceiptTeal
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderChip(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = ReceiptCoral.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, ReceiptCoral.copy(alpha = 0.30f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = ReceiptCoral
        )
    }
}

@Composable
private fun MiniHeroTile(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    accent: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = accent.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.20f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = ReceiptInk,
                textAlign = TextAlign.Center
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = ReceiptMuted,
                textAlign = TextAlign.Center
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
    ModernReceiptCard {
        SectionTitle(
            eyebrow = "VERIFICATION",
            title = "Verify transaction hash",
            subtitle = "Enter, scan, or share a receipt hash for blockchain verification."
        )

        OutlinedTextField(
            value = transactionHashInput,
            onValueChange = onValueChanged,
            label = { Text(text = "Transaction hash") },
            placeholder = { Text(text = "0x...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            isError = inputError != null,
            supportingText = {
                Text(
                    text = inputError ?: "Use the hash shown after a successful vote."
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
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp),
                enabled = !verificationInProgress
            ) {
                Text(
                    text = "Scan QR",
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = onShareHash,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp),
                enabled = !verificationInProgress && transactionHashInput.trim().isNotBlank()
            ) {
                Text(
                    text = "Share",
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
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp),
                enabled = !verificationInProgress
            ) {
                Text(
                    text = "Clear",
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onVerify,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp),
                enabled = !verificationInProgress,
                colors = ButtonDefaults.buttonColors(containerColor = ReceiptIndigo),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 5.dp)
            ) {
                Text(
                    text = if (verificationInProgress) {
                        "Verifying..."
                    } else {
                        "Verify"
                    },
                    fontWeight = FontWeight.Black
                )
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
        shape = RoundedCornerShape(22.dp),
        color = ReceiptIndigo.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, ReceiptIndigo.copy(alpha = 0.20f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.size(34.dp),
                    shape = CircleShape,
                    color = ReceiptGreen.copy(alpha = 0.16f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "✓",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = ReceiptGreen
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Hash ready",
                        style = MaterialTheme.typography.labelLarge,
                        color = ReceiptInk,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = shortenHash(hash),
                        style = MaterialTheme.typography.bodySmall,
                        color = ReceiptMuted,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 4.dp,
                border = BorderStroke(1.dp, ReceiptLine)
            ) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "Transaction hash QR code",
                    modifier = Modifier
                        .size(176.dp)
                        .padding(14.dp)
                )
            }

            Text(
                text = "This QR contains the same transaction hash for quick verification.",
                style = MaterialTheme.typography.bodySmall,
                color = ReceiptMuted,
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
    ModernReceiptCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = ReceiptIndigo)

            Text(
                text = "Verifying on blockchain",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                color = ReceiptInk
            )

            if (transactionHash.isNotBlank()) {
                Text(
                    text = shortenHash(transactionHash),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = ReceiptMuted,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = "Checking the transaction receipt from the connected blockchain network.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = ReceiptMuted
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
    val accent = if (positive) ReceiptGreen else ReceiptRed
    val iconText = if (positive) "OK" else "!"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (positive) {
                Color(0xFF0F7A44)
            } else {
                Color(0xFF9F1D2D)
            }
        ),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.32f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.24f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = iconText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
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
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.88f)
                )
            }
        }
    }
}

@Composable
private fun OnChainVerificationCard(
    verification: OnChainTransactionVerification
) {
    val statusText = if (verification.status == "0x1") "Success" else "Failed"

    ModernReceiptCard {
        SectionTitle(
            eyebrow = "ON-CHAIN AUDIT",
            title = "Blockchain verification",
            subtitle = "Blockchain receipt details for the submitted transaction."
        )

        AuditSummaryPanel(
            status = statusText,
            blockNumber = verification.blockNumber.toString(),
            gasUsed = verification.gasUsed.toString()
        )

        HorizontalDivider(color = ReceiptLine.copy(alpha = 0.80f))

        ReceiptRow(
            label = "Transaction hash",
            value = verification.transactionHash,
            mono = true
        )

        ReceiptRow(
            label = "Block number",
            value = verification.blockNumber.toString()
        )

        ReceiptRow(
            label = "Status",
            value = statusText
        )

        ReceiptRow(
            label = "From address",
            value = verification.fromAddress,
            mono = true
        )

        ReceiptRow(
            label = "To address",
            value = verification.toAddress ?: "-",
            mono = true
        )

        ReceiptRow(
            label = "Gas used",
            value = verification.gasUsed.toString()
        )
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
        shape = RoundedCornerShape(22.dp),
        color = ReceiptIndigo.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, ReceiptIndigo.copy(alpha = 0.20f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Confirmation summary",
                style = MaterialTheme.typography.titleMedium,
                color = ReceiptInk,
                fontWeight = FontWeight.Black
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MiniAuditTile(
                    modifier = Modifier.weight(1f),
                    label = "Status",
                    value = status,
                    accent = if (status == "Success") ReceiptGreen else ReceiptRed
                )

                MiniAuditTile(
                    modifier = Modifier.weight(1f),
                    label = "Block",
                    value = blockNumber,
                    accent = ReceiptBlue
                )
            }

            MiniAuditTile(
                modifier = Modifier.fillMaxWidth(),
                label = "Gas used",
                value = gasUsed,
                accent = ReceiptTeal
            )
        }
    }
}

@Composable
private fun MiniAuditTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.78f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = accent,
                fontWeight = FontWeight.Black
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = ReceiptInk,
                fontWeight = FontWeight.Black,
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
    ModernReceiptCard {
        SectionTitle(
            eyebrow = "LOCAL RECEIPT",
            title = title,
            subtitle = supportingText
        )

        LocalReceiptSummaryPanel(receipt = receipt)

        HorizontalDivider(color = ReceiptLine.copy(alpha = 0.80f))

        ReceiptRow(
            label = "Election ID",
            value = receipt.electionId
        )

        ReceiptRow(
            label = "Election title",
            value = receipt.electionTitle
        )

        ReceiptRow(
            label = "Wallet address",
            value = receipt.voterId,
            mono = true
        )

        ReceiptRow(
            label = "Candidate",
            value = receipt.candidateName
        )

        ReceiptRow(
            label = "Timestamp",
            value = formatTimestamp(receipt.timestamp)
        )

        ReceiptRow(
            label = "Transaction hash",
            value = receipt.transactionHash,
            mono = true
        )
    }
}

@Composable
private fun LocalReceiptSummaryPanel(
    receipt: VoteReceipt
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = ReceiptTeal.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, ReceiptTeal.copy(alpha = 0.24f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Receipt summary",
                style = MaterialTheme.typography.titleMedium,
                color = ReceiptInk,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "Election: ${receipt.electionTitle}",
                style = MaterialTheme.typography.bodyMedium,
                color = ReceiptInk,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Candidate: ${receipt.candidateName}",
                style = MaterialTheme.typography.bodyMedium,
                color = ReceiptInk,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Hash: ${shortenHash(receipt.transactionHash)}",
                style = MaterialTheme.typography.bodySmall,
                color = ReceiptMuted,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun EmptyReceiptState() {
    ModernReceiptCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = CircleShape,
                color = ReceiptIndigo.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, ReceiptIndigo.copy(alpha = 0.22f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "#",
                        style = MaterialTheme.typography.titleLarge,
                        color = ReceiptIndigo,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Text(
                text = "No receipt selected",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                color = ReceiptInk
            )

            Text(
                text = "Submit a vote or enter a transaction hash to verify a receipt.",
                style = MaterialTheme.typography.bodyMedium,
                color = ReceiptMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NoLocalReceiptMatchCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.18f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.24f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "No local receipt match",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Text(
                text = "The transaction was verified on-chain, but this device does not have a saved local vote receipt for it.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.86f)
            )
        }
    }
}

@Composable
private fun VerificationErrorCard(
    message: String
) {
    StatusBanner(
        title = "Verification failed",
        message = message.ifBlank { "The transaction could not be verified." },
        positive = false
    )
}

@Composable
private fun ReceiptFooterActions(
    verificationInProgress: Boolean,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .weight(1f)
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            enabled = !verificationInProgress
        ) {
            Text(
                text = "Back",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Button(
            onClick = onDone,
            modifier = Modifier
                .weight(1f)
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            enabled = !verificationInProgress,
            colors = ButtonDefaults.buttonColors(containerColor = ReceiptCoral),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 5.dp)
        ) {
            Text(
                text = "Done",
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun ModernReceiptCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = ReceiptCard.copy(alpha = 0.96f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content
        )
    }
}

@Composable
private fun SectionTitle(
    eyebrow: String,
    title: String,
    subtitle: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = eyebrow,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = ReceiptTeal
        )

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = ReceiptInk
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = ReceiptMuted
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
        color = ReceiptPanel.copy(alpha = 0.86f),
        border = BorderStroke(1.dp, ReceiptLine.copy(alpha = 0.72f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = ReceiptIndigo,
                fontWeight = FontWeight.Black
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = ReceiptMuted,
                fontWeight = FontWeight.Medium,
                fontFamily = if (mono) FontFamily.Monospace else FontFamily.Default,
                maxLines = if (mono) 3 else 2,
                overflow = TextOverflow.Ellipsis
            )
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