package com.example.evotingmobileapp.blockchain

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evotingmobileapp.R
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
    val context = androidx.compose.ui.platform.LocalContext.current

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
                BlockchainRecordsHeroCard()
            }

            item {
                BlockchainStatusCard(
                    contractSummary = displayValue(
                        value = contractSummary,
                        fallback = stringResource(R.string.blockchain_records_not_loaded)
                    ),
                    latestBlock = displayValue(
                        value = latestBlock,
                        fallback = stringResource(R.string.blockchain_records_not_loaded)
                    )
                )
            }

            if (voteReceipts.isEmpty()) {
                item {
                    EmptyBlockchainRecordsState()
                }
            } else {
                item {
                    Text(
                        text = stringResource(R.string.blockchain_records_audit_trail_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(
                    items = voteReceipts.asReversed(),
                    key = { receipt -> "${receipt.transactionHash}-${receipt.timestamp}" }
                ) { receipt ->
                    VoteReceiptCard(receipt = receipt)
                }
            }
        }
    }
}

@Composable
private fun BlockchainRecordsHeroCard() {
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
                text = stringResource(R.string.blockchain_records_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = stringResource(R.string.blockchain_records_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f)
            )

            PillColumn(
                items = listOf(
                    stringResource(R.string.blockchain_records_pill_contract),
                    stringResource(R.string.blockchain_records_pill_receipts),
                    stringResource(R.string.blockchain_records_pill_audit)
                )
            )
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.blockchain_records_connection_status),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            StatusBadge(statusText = stringResource(R.string.blockchain_records_connected_audit_view))

            DetailItem(
                label = stringResource(R.string.blockchain_records_contract_summary),
                value = contractSummary
            )

            DetailItem(
                label = stringResource(R.string.blockchain_records_latest_block),
                value = latestBlock
            )
        }
    }
}

@Composable
private fun EmptyBlockchainRecordsState() {
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.blockchain_records_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.blockchain_records_empty_message),
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = receipt.electionTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            StatusBadge(statusText = stringResource(R.string.blockchain_records_recorded))

            DetailItem(label = stringResource(R.string.blockchain_records_election_id), value = receipt.electionId)
            DetailItem(label = stringResource(R.string.blockchain_records_voter_id), value = receipt.voterId)
            DetailItem(label = stringResource(R.string.blockchain_records_candidate_name), value = receipt.candidateName)
            DetailItem(label = stringResource(R.string.blockchain_records_timestamp), value = formatReceiptTimestamp(receipt.timestamp))
            DetailItem(label = stringResource(R.string.blockchain_records_transaction_hash), value = receipt.transactionHash)
        }
    }
}

@Composable
private fun StatusBadge(
    statusText: String
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(999.dp)
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

private fun formatReceiptTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

private fun displayValue(value: Any?, fallback: String): String {
    val text = value?.toString()?.trim().orEmpty()
    return if (text.isBlank() || text == "null") fallback else text
}