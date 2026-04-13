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
    val receipt by adminViewModel.latestReceipt.collectAsState()
    val voteReceipts by adminViewModel.voteReceipts.collectAsState()

    var transactionHashInput by rememberSaveable {
        mutableStateOf(initialTransactionHash.orEmpty())
    }
    var hasAttemptedVerification by rememberSaveable {
        mutableStateOf(!initialTransactionHash.isNullOrBlank())
    }

    LaunchedEffect(initialTransactionHash) {
        if (!initialTransactionHash.isNullOrBlank()) {
            transactionHashInput = initialTransactionHash
            hasAttemptedVerification = true
            adminViewModel.selectReceiptByTransactionHash(initialTransactionHash)
        }
    }

    val trimmedTransactionHash = transactionHashInput.trim()

    val verifiedReceipt: VoteReceipt? =
        if (hasAttemptedVerification && trimmedTransactionHash.isNotBlank()) {
            voteReceipts.firstOrNull { savedReceipt ->
                savedReceipt.transactionHash.equals(trimmedTransactionHash, ignoreCase = true)
            }
        } else {
            null
        }

    val currentReceipt =
        if (hasAttemptedVerification && trimmedTransactionHash.isNotBlank()) {
            verifiedReceipt
        } else {
            receipt
        }

    val shouldShowNotFoundState =
        hasAttemptedVerification &&
                trimmedTransactionHash.isNotBlank() &&
                verifiedReceipt == null

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
                text = "Review your latest receipt or verify a recorded vote using its transaction hash.",
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
                        onValueChange = { transactionHashInput = it },
                        label = { Text("Transaction Hash") },
                        placeholder = { Text("0x...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                transactionHashInput = ""
                                hasAttemptedVerification = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear")
                        }

                        Button(
                            onClick = {
                                hasAttemptedVerification = true
                                adminViewModel.selectReceiptByTransactionHash(transactionHashInput)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Verify")
                        }
                    }
                }
            }

            when {
                shouldShowNotFoundState -> {
                    NotFoundReceiptState()
                }

                currentReceipt == null -> {
                    EmptyReceiptState(
                        onBackClick = { navController.popBackStack() }
                    )
                }

                else -> {
                    val currentVerifiedReceipt = currentReceipt

                    Text(
                        text = if (hasAttemptedVerification && trimmedTransactionHash.isNotBlank()) {
                            "Verified transaction details found."
                        } else {
                            "Your vote has been recorded successfully."
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

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
                            ReceiptRow(
                                label = "Election ID",
                                value = currentVerifiedReceipt.electionId
                            )
                            ReceiptRow(
                                label = "Election Title",
                                value = currentVerifiedReceipt.electionTitle
                            )
                            ReceiptRow(
                                label = "Wallet Address",
                                value = currentVerifiedReceipt.voterId
                            )
                            ReceiptRow(
                                label = "Candidate",
                                value = currentVerifiedReceipt.candidateName
                            )
                            ReceiptRow(
                                label = "Timestamp",
                                value = formatTimestamp(currentVerifiedReceipt.timestamp)
                            )
                            ReceiptRow(
                                label = "Transaction Hash",
                                value = currentVerifiedReceipt.transactionHash
                            )
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
    }
}

@Composable
private fun EmptyReceiptState(
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No receipt available",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Submit a vote first or verify using a known transaction hash.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Go Back")
        }
    }
}

@Composable
private fun NotFoundReceiptState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Transaction hash not found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "No stored vote receipt matches the transaction hash you entered. Check the hash and try again.",
                style = MaterialTheme.typography.bodyMedium
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

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return formatter.format(Date(timestamp))
}