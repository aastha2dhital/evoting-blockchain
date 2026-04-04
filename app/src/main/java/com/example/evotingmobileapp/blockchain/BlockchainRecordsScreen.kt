package com.example.evotingmobileapp.blockchain

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evotingmobileapp.admin.AdminViewModel
import com.example.evotingmobileapp.model.VoteReceipt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BlockchainRecordsScreen(
    adminViewModel: AdminViewModel,
    blockchainViewModel: BlockchainViewModel = viewModel()
) {
    val context = LocalContext.current

    val voteReceipts by adminViewModel.voteReceipts.collectAsStateWithLifecycle()
    val latestBlock by blockchainViewModel.latestBlock.collectAsStateWithLifecycle()
    val contractSummary by blockchainViewModel.contractSummary.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        blockchainViewModel.loadBlockchainStatus(context)
    }

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Blockchain Records",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Text(
                    text = "Review the current contract connection status and the vote receipt audit trail collected from the app flow.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                BlockchainStatusCard(
                    contractSummary = displayValue(contractSummary),
                    latestBlock = displayValue(latestBlock)
                )
            }

            if (voteReceipts.isEmpty()) {
                item {
                    EmptyBlockchainRecordsState()
                }
            } else {
                item {
                    Text(
                        text = "Vote Receipt Audit Trail",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                items(
                    items = voteReceipts.asReversed(),
                    key = { receipt ->
                        "${receipt.transactionHash}-${receipt.timestamp}"
                    }
                ) { receipt ->
                    VoteReceiptCard(receipt = receipt)
                }
            }
        }
    }
}

@Composable
private fun BlockchainStatusCard(
    contractSummary: String,
    latestBlock: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Blockchain Connection Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            StatusBadge(statusText = "Connected Audit View")

            DetailItem(
                label = "Contract Summary",
                value = contractSummary
            )

            DetailItem(
                label = "Latest Block",
                value = latestBlock
            )
        }
    }
}

@Composable
private fun EmptyBlockchainRecordsState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "No blockchain records yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Once a vote is successfully submitted, its receipt details will appear here for audit review.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VoteReceiptCard(
    receipt: VoteReceipt
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = receipt.electionTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            StatusBadge(statusText = "Recorded")

            DetailItem(label = "Election ID", value = receipt.electionId)
            DetailItem(label = "Voter ID", value = receipt.voterId)
            DetailItem(label = "Candidate Name", value = receipt.candidateName)
            DetailItem(label = "Timestamp", value = formatReceiptTimestamp(receipt.timestamp))
            DetailItem(label = "Transaction Hash", value = receipt.transactionHash)
        }
    }
}

@Composable
private fun StatusBadge(
    statusText: String
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = statusText,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DetailItem(
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

private fun formatReceiptTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

private fun displayValue(value: Any?): String {
    val text = value?.toString()?.trim().orEmpty()
    return if (text.isBlank() || text == "null") "Not loaded" else text
}