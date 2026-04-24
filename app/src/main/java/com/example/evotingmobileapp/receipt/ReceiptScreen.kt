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
    var hasAttemptedVerification by rememberSaveable {
        mutableStateOf(!initialTransactionHash.isNullOrBlank())
    }
    var lastSubmittedTransactionHash by rememberSaveable {
        mutableStateOf(initialTransactionHash?.trim().orEmpty())
    }
    var inputError by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    val enterHashError = stringResource(R.string.receipt_error_enter_hash)
    val verifiedSuccessTitle = stringResource(R.string.receipt_verification_success_title)
    val verifiedSuccessMessage = stringResource(R.string.receipt_verification_success_message)
    val matchedLocalReceiptTitle = stringResource(R.string.receipt_matched_local_title)
    val matchedLocalReceiptSubtitle = stringResource(R.string.receipt_matched_local_subtitle)
    val localReceiptMatchTitle = stringResource(R.string.receipt_local_match_title)
    val localReceiptMatchSubtitle = stringResource(R.string.receipt_local_match_subtitle)
    val latestSavedTitle = stringResource(R.string.receipt_latest_saved_title)
    val latestSavedMessage = stringResource(R.string.receipt_latest_saved_message)
    val latestLocalTitle = stringResource(R.string.receipt_latest_local_title)
    val latestLocalSubtitle = stringResource(R.string.receipt_latest_local_subtitle)

    LaunchedEffect(initialTransactionHash) {
        val startingHash = initialTransactionHash?.trim().orEmpty()
        if (startingHash.isNotBlank()) {
            transactionHashInput = startingHash
            hasAttemptedVerification = true
            lastSubmittedTransactionHash = startingHash
            inputError = null
            adminViewModel.clearOnChainVerification()
            adminViewModel.verifyTransactionReceiptOnChain(context, startingHash)
        }
    }

    val trimmedTransactionHash = transactionHashInput.trim()

    val matchedLocalReceipt: VoteReceipt? =
        if (trimmedTransactionHash.isNotBlank()) {
            voteReceipts.firstOrNull { savedReceipt ->
                savedReceipt.transactionHash.equals(trimmedTransactionHash, ignoreCase = true)
            }
        } else {
            null
        }

    val currentLatestReceipt = latestReceipt
    val currentVerifiedOnChainTransaction = verifiedOnChainTransaction

    val shouldShowOnChainSuccess =
        hasAttemptedVerification &&
                !verificationInProgress &&
                verificationError.isNullOrBlank() &&
                currentVerifiedOnChainTransaction != null &&
                lastSubmittedTransactionHash.equals(trimmedTransactionHash, ignoreCase = true)

    val shouldShowVerificationError =
        hasAttemptedVerification &&
                !verificationInProgress &&
                inputError == null &&
                !verificationError.isNullOrBlank()

    val verifiedTransactionForUi =
        if (shouldShowOnChainSuccess) currentVerifiedOnChainTransaction else null

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            ReceiptHeroCard()

            VerificationInputCard(
                transactionHashInput = transactionHashInput,
                inputError = inputError,
                verificationInProgress = verificationInProgress,
                currentVerifiedOnChainTransaction = currentVerifiedOnChainTransaction,
                verificationError = verificationError,
                onValueChanged = { newValue ->
                    transactionHashInput = newValue
                    inputError = null

                    if (
                        hasAttemptedVerification ||
                        currentVerifiedOnChainTransaction != null ||
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
                onVerify = {
                    if (trimmedTransactionHash.isBlank()) {
                        inputError = enterHashError
                        hasAttemptedVerification = false
                        lastSubmittedTransactionHash = ""
                        adminViewModel.clearOnChainVerification()
                    } else {
                        inputError = null
                        hasAttemptedVerification = true
                        lastSubmittedTransactionHash = trimmedTransactionHash
                        adminViewModel.clearOnChainVerification()
                        adminViewModel.verifyTransactionReceiptOnChain(
                            context = context,
                            transactionHash = trimmedTransactionHash
                        )
                    }
                }
            )

            when {
                verificationInProgress -> {
                    VerificationLoadingState(
                        transactionHash = lastSubmittedTransactionHash.ifBlank { trimmedTransactionHash }
                    )
                }

                verifiedTransactionForUi != null -> {
                    StatusBanner(
                        title = verifiedSuccessTitle,
                        message = verifiedSuccessMessage,
                        positive = true
                    )

                    OnChainVerificationCard(
                        verification = verifiedTransactionForUi
                    )

                    if (matchedLocalReceipt != null) {
                        ReceiptDetailsCard(
                            title = matchedLocalReceiptTitle,
                            supportingText = matchedLocalReceiptSubtitle,
                            receipt = matchedLocalReceipt
                        )
                    } else {
                        NoLocalReceiptMatchCard()
                    }
                }

                shouldShowVerificationError -> {
                    VerificationErrorCard(
                        message = verificationError.orEmpty()
                    )

                    if (matchedLocalReceipt != null) {
                        ReceiptDetailsCard(
                            title = localReceiptMatchTitle,
                            supportingText = localReceiptMatchSubtitle,
                            receipt = matchedLocalReceipt
                        )
                    }
                }

                currentLatestReceipt != null -> {
                    StatusBanner(
                        title = latestSavedTitle,
                        message = latestSavedMessage,
                        positive = true
                    )

                    ReceiptDetailsCard(
                        title = latestLocalTitle,
                        supportingText = latestLocalSubtitle,
                        receipt = currentLatestReceipt
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
    currentVerifiedOnChainTransaction: Any?,
    verificationError: String?,
    onValueChanged: (String) -> Unit,
    onClear: () -> Unit,
    onVerify: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
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
                }
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
                        }
                    )
                }
            }

            if (currentVerifiedOnChainTransaction != null || !verificationError.isNullOrBlank()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
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
        shape = RoundedCornerShape(24.dp),
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
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            if (transactionHash.isNotBlank()) {
                Text(
                    text = transactionHash,
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

    val titleColor = if (positive) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
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
                color = titleColor
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = titleColor
            )
        }
    }
}

@Composable
private fun OnChainVerificationCard(
    verification: Any
) {
    val labels = ReceiptVerificationLabels(
        transactionHash = stringResource(R.string.receipt_label_transaction_hash),
        blockNumber = stringResource(R.string.receipt_label_block_number),
        status = stringResource(R.string.receipt_label_status),
        fromAddress = stringResource(R.string.receipt_label_from_address),
        toAddress = stringResource(R.string.receipt_label_to_address),
        gasUsed = stringResource(R.string.receipt_label_gas_used),
        verificationResult = stringResource(R.string.receipt_label_verification_result),
        success = stringResource(R.string.receipt_status_success),
        failed = stringResource(R.string.receipt_status_failed)
    )

    val detailRows = buildOnChainVerificationRows(
        verification = verification,
        labels = labels
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
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

            detailRows.forEach { (label, value) ->
                ReceiptRow(
                    label = label,
                    value = value
                )
            }
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
        shape = RoundedCornerShape(24.dp),
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
        shape = RoundedCornerShape(24.dp),
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
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.receipt_no_local_match_message),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun VerificationErrorCard(
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.receipt_verification_failed_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
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

private data class ReceiptVerificationLabels(
    val transactionHash: String,
    val blockNumber: String,
    val status: String,
    val fromAddress: String,
    val toAddress: String,
    val gasUsed: String,
    val verificationResult: String,
    val success: String,
    val failed: String
)

private fun buildOnChainVerificationRows(
    verification: Any,
    labels: ReceiptVerificationLabels
): List<Pair<String, String>> {
    val transactionHash = readPropertyValue(
        target = verification,
        "transactionHash",
        "txHash",
        "hash",
        labels = labels
    )

    val blockNumber = readPropertyValue(
        target = verification,
        "blockNumber",
        "blockNo",
        labels = labels
    )

    val status = normalizeStatusValue(
        rawStatus = readPropertyValue(
            target = verification,
            "status",
            "receiptStatus",
            "successful",
            "success",
            "isSuccessful",
            labels = labels
        ),
        labels = labels
    )

    val fromAddress = readPropertyValue(
        target = verification,
        "fromAddress",
        "from",
        "senderAddress",
        "sender",
        labels = labels
    )

    val toAddress = readPropertyValue(
        target = verification,
        "toAddress",
        "to",
        "contractAddress",
        "receiverAddress",
        labels = labels
    )

    val gasUsed = readPropertyValue(
        target = verification,
        "gasUsed",
        "cumulativeGasUsed",
        labels = labels
    )

    return listOfNotNull(
        transactionHash?.let { labels.transactionHash to it },
        blockNumber?.let { labels.blockNumber to it },
        status?.let { labels.status to it },
        fromAddress?.let { labels.fromAddress to it },
        toAddress?.let { labels.toAddress to it },
        gasUsed?.let { labels.gasUsed to it }
    ).ifEmpty {
        listOf(labels.verificationResult to verification.toString())
    }
}

private fun readPropertyValue(
    target: Any,
    vararg propertyNames: String,
    labels: ReceiptVerificationLabels
): String? {
    for (propertyName in propertyNames) {
        val getterCandidates = listOf(
            "get${propertyName.replaceFirstChar { it.uppercase() }}",
            "is${propertyName.replaceFirstChar { it.uppercase() }}"
        )

        getterCandidates.forEach { getterName ->
            runCatching {
                target.javaClass.methods.firstOrNull { method ->
                    method.parameterCount == 0 && method.name.equals(getterName, ignoreCase = true)
                }?.invoke(target)
            }.getOrNull()?.let { value ->
                return convertValueToString(value, labels)
            }
        }

        runCatching {
            target.javaClass.declaredFields.firstOrNull { field ->
                field.name.equals(propertyName, ignoreCase = true)
            }?.apply {
                isAccessible = true
            }?.get(target)
        }.getOrNull()?.let { value ->
            return convertValueToString(value, labels)
        }
    }

    return null
}

private fun convertValueToString(
    value: Any?,
    labels: ReceiptVerificationLabels
): String? {
    return when (value) {
        null -> null
        is Boolean -> if (value) labels.success else labels.failed
        else -> value.toString().takeIf { it.isNotBlank() }
    }
}

private fun normalizeStatusValue(
    rawStatus: String?,
    labels: ReceiptVerificationLabels
): String? {
    return when (rawStatus?.trim()?.lowercase(Locale.getDefault())) {
        null, "" -> null
        "true", "1", "0x1", "success", "successful", "succeeded" -> labels.success
        "false", "0", "0x0", "failure", "failed" -> labels.failed
        else -> rawStatus
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
