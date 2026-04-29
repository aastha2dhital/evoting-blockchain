package com.example.evotingmobileapp.receipt

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.evotingmobileapp.R
import com.example.evotingmobileapp.admin.AdminViewModel
import com.example.evotingmobileapp.blockchain.OnChainTransactionVerification
import com.example.evotingmobileapp.model.VoteReceipt
import java.text.SimpleDateFormat
import java.util.Date
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

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                    Text(text = stringResource(R.string.receipt_done_button))
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
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
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionTitle(
                title = stringResource(R.string.receipt_verified_on_chain_title),
                subtitle = stringResource(R.string.receipt_verified_on_chain_subtitle)
            )

            ReceiptRow(
                label = stringResource(R.string.receipt_label_transaction_hash),
                value = verification.transactionHash
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
                value = verification.fromAddress
            )

            ReceiptRow(
                label = stringResource(R.string.receipt_label_to_address),
                value = verification.toAddress ?: "-"
            )

            ReceiptRow(
                label = stringResource(R.string.receipt_label_gas_used),
                value = verification.gasUsed.toString()
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionTitle(
                title = title,
                subtitle = supportingText
            )

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
                value = receipt.voterId
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
                value = receipt.transactionHash
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
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.receipt_empty_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
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
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.receipt_no_local_match_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
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
private fun ReceiptRow(
    label: String,
    value: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
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

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

private fun shortenHash(hash: String): String {
    if (hash.length <= 18) return hash
    return "${hash.take(10)}...${hash.takeLast(8)}"
}