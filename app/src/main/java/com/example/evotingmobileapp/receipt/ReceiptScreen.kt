package com.example.evotingmobileapp.receipt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Voting Receipt",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Review your latest receipt or verify a recorded vote using its transaction hash. On-chain verification below checks the real blockchain transaction result.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Verify by Transaction Hash",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = transactionHashInput,
                        onValueChange = { newValue ->
                            transactionHashInput = newValue
                            inputError = null

                            if (hasAttemptedVerification ||
                                currentVerifiedOnChainTransaction != null ||
                                !verificationError.isNullOrBlank()
                            ) {
                                hasAttemptedVerification = false
                                lastSubmittedTransactionHash = ""
                                adminViewModel.clearOnChainVerification()
                            }
                        },
                        label = { Text("Transaction Hash") },
                        placeholder = { Text("0x...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        isError = inputError != null,
                        supportingText = {
                            Text(
                                text = inputError
                                    ?: "Enter a blockchain transaction hash to confirm that the vote receipt exists on-chain."
                            )
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                transactionHashInput = ""
                                inputError = null
                                hasAttemptedVerification = false
                                lastSubmittedTransactionHash = ""
                                adminViewModel.clearOnChainVerification()
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !verificationInProgress
                        ) {
                            Text("Clear")
                        }

                        Button(
                            onClick = {
                                if (trimmedTransactionHash.isBlank()) {
                                    inputError = "Enter a transaction hash before verifying."
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
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !verificationInProgress
                        ) {
                            Text(if (verificationInProgress) "Verifying..." else "Verify")
                        }
                    }
                }
            }

            when {
                verificationInProgress -> {
                    VerificationLoadingState(
                        transactionHash = lastSubmittedTransactionHash.ifBlank { trimmedTransactionHash }
                    )
                }

                verifiedTransactionForUi != null -> {
                    Text(
                        text = "On-chain verification succeeded. This transaction was found on the blockchain.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OnChainVerificationCard(
                        verification = verifiedTransactionForUi
                    )

                    if (matchedLocalReceipt != null) {
                        ReceiptDetailsCard(
                            title = "Matched Local Receipt",
                            supportingText = "This app receipt matches the verified blockchain transaction hash.",
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
                            title = "Local Receipt Match",
                            supportingText = "A local receipt with this hash exists in app storage, but blockchain verification did not succeed. Treat the on-chain result as the source of truth.",
                            receipt = matchedLocalReceipt
                        )
                    }
                }

                currentLatestReceipt != null -> {
                    Text(
                        text = "Your latest recorded receipt is shown below. Use the verification section above to confirm the transaction on-chain.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    ReceiptDetailsCard(
                        title = "Latest Local Receipt",
                        supportingText = "This is the latest receipt saved in the app. It is useful for review, but it is not the same as on-chain verification.",
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
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Back")
                }

                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Done")
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()

            Text(
                text = "Verifying transaction on blockchain...",
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
private fun OnChainVerificationCard(
    verification: Any
) {
    val detailRows = buildOnChainVerificationRows(verification)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Verified On-Chain Transaction",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "These values come from the blockchain verification result, not just local app receipt storage.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ReceiptRow(
                label = "Election ID",
                value = receipt.electionId
            )
            ReceiptRow(
                label = "Election Title",
                value = receipt.electionTitle
            )
            ReceiptRow(
                label = "Wallet Address",
                value = receipt.voterId
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
                label = "Transaction Hash",
                value = receipt.transactionHash
            )
        }
    }
}

@Composable
private fun EmptyReceiptState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No receipt available",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Submit a vote first or verify a known transaction hash to check whether it exists on-chain.",
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "No local receipt match",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "The blockchain transaction was verified successfully, but no matching receipt was found in the app's current local receipt list.",
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "On-chain verification failed",
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
private fun ReceiptRow(
    label: String,
    value: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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

private fun buildOnChainVerificationRows(
    verification: Any
): List<Pair<String, String>> {
    val transactionHash = readPropertyValue(
        target = verification,
        "transactionHash",
        "txHash",
        "hash"
    )

    val blockNumber = readPropertyValue(
        target = verification,
        "blockNumber",
        "blockNo"
    )

    val status = normalizeStatusValue(
        readPropertyValue(
            target = verification,
            "status",
            "receiptStatus",
            "successful",
            "success",
            "isSuccessful"
        )
    )

    val fromAddress = readPropertyValue(
        target = verification,
        "fromAddress",
        "from",
        "senderAddress",
        "sender"
    )

    val toAddress = readPropertyValue(
        target = verification,
        "toAddress",
        "to",
        "contractAddress",
        "receiverAddress"
    )

    val gasUsed = readPropertyValue(
        target = verification,
        "gasUsed",
        "cumulativeGasUsed"
    )

    return listOfNotNull(
        transactionHash?.let { "Transaction Hash" to it },
        blockNumber?.let { "Block Number" to it },
        status?.let { "Status" to it },
        fromAddress?.let { "From Address" to it },
        toAddress?.let { "To Address" to it },
        gasUsed?.let { "Gas Used" to it }
    ).ifEmpty {
        listOf("Verification Result" to verification.toString())
    }
}

private fun readPropertyValue(
    target: Any,
    vararg propertyNames: String
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
                return convertValueToString(value)
            }
        }

        runCatching {
            target.javaClass.declaredFields.firstOrNull { field ->
                field.name.equals(propertyName, ignoreCase = true)
            }?.apply {
                isAccessible = true
            }?.get(target)
        }.getOrNull()?.let { value ->
            return convertValueToString(value)
        }
    }

    return null
}

private fun convertValueToString(value: Any?): String? {
    return when (value) {
        null -> null
        is Boolean -> if (value) "Success" else "Failed"
        else -> value.toString().takeIf { it.isNotBlank() }
    }
}

private fun normalizeStatusValue(rawStatus: String?): String? {
    return when (rawStatus?.trim()?.lowercase(Locale.getDefault())) {
        null, "" -> null
        "true", "1", "0x1", "success", "successful", "succeeded" -> "Success"
        "false", "0", "0x0", "failure", "failed" -> "Failed"
        else -> rawStatus
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return formatter.format(Date(timestamp))
}