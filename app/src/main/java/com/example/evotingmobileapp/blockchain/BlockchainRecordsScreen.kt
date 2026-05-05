package com.example.evotingmobileapp.blockchain

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evotingmobileapp.R
import com.example.evotingmobileapp.admin.AdminViewModel
import com.example.evotingmobileapp.model.VoteReceipt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ChainNavy = Color(0xFF07133A)
private val ChainDeepBlue = Color(0xFF102A70)
private val ChainIndigo = Color(0xFF4F32F6)
private val ChainPurple = Color(0xFF7C3AED)
private val ChainBlue = Color(0xFF1479FF)
private val ChainTeal = Color(0xFF00B8A9)
private val ChainCoral = Color(0xFFFF5C7A)
private val ChainAmber = Color(0xFFFFB020)
private val ChainGreen = Color(0xFF17C964)
private val ChainCard = Color(0xFFF6F8FF)
private val ChainPanel = Color(0xFFEAF1FF)
private val ChainInk = Color(0xFF081229)
private val ChainMuted = Color(0xFF59627E)
private val ChainLine = Color(0xFFD6DDF4)

@Composable
fun BlockchainRecordsScreen(
    adminViewModel: AdminViewModel,
    blockchainViewModel: BlockchainViewModel = viewModel()
) {
    val context = LocalContext.current

    val voteReceipts by adminViewModel.voteReceipts.collectAsStateWithLifecycle()
    val latestBlock by blockchainViewModel.latestBlock.collectAsStateWithLifecycle()
    val contractSummary by blockchainViewModel.contractSummary.collectAsStateWithLifecycle()

    val notLoadedText = stringResource(R.string.blockchain_records_not_loaded)

    LaunchedEffect(Unit) {
        blockchainViewModel.loadBlockchainStatus(context)
    }

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ChainNavy,
                            ChainDeepBlue,
                            Color(0xFF3155D8),
                            Color(0xFFD8ECFF)
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            BlockchainDecorativeBackground()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(
                    start = 18.dp,
                    end = 18.dp,
                    top = 16.dp,
                    bottom = 28.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    BlockchainHeroCard(
                        recordsCount = voteReceipts.size,
                        latestBlock = displayValue(latestBlock, notLoadedText)
                    )
                }

                item {
                    BlockchainStatusCard(
                        contractSummary = displayValue(contractSummary, notLoadedText),
                        latestBlock = displayValue(latestBlock, notLoadedText)
                    )
                }

                if (voteReceipts.isEmpty()) {
                    item {
                        EmptyBlockchainRecordsState()
                    }
                } else {
                    item {
                        SectionHeader(
                            title = stringResource(R.string.blockchain_records_audit_trail_title),
                            subtitle = stringResource(R.string.blockchain_records_audit_trail_subtitle)
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

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun BlockchainDecorativeBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .size(230.dp)
                .offset(x = (-90).dp, y = 40.dp),
            shape = CircleShape,
            color = ChainTeal.copy(alpha = 0.16f)
        ) {}

        Surface(
            modifier = Modifier
                .size(190.dp)
                .align(Alignment.TopEnd)
                .offset(x = 72.dp, y = 88.dp),
            shape = CircleShape,
            color = ChainCoral.copy(alpha = 0.16f)
        ) {}

        Surface(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 98.dp, y = 36.dp),
            shape = CircleShape,
            color = ChainAmber.copy(alpha = 0.14f)
        ) {}

        Surface(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-130).dp, y = 110.dp),
            shape = CircleShape,
            color = ChainPurple.copy(alpha = 0.15f)
        ) {}
    }
}

@Composable
private fun BlockchainHeroCard(
    recordsCount: Int,
    latestBlock: String
) {
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
                color = ChainTeal.copy(alpha = 0.12f)
            ) {}

            Surface(
                modifier = Modifier
                    .size(78.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 30.dp, y = 24.dp),
                shape = CircleShape,
                color = ChainCoral.copy(alpha = 0.10f)
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
                        color = ChainIndigo,
                        shadowElevation = 8.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "SV",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Black
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
                            color = ChainIndigo,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = stringResource(R.string.blockchain_records_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = ChainInk,
                            fontWeight = FontWeight.Black
                        )
                    }

                    HeaderChip(
                        text = stringResource(R.string.blockchain_records_pill_audit).uppercase()
                    )
                }

                Text(
                    text = stringResource(R.string.blockchain_records_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ChainMuted
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HeroMetric(
                        label = stringResource(R.string.blockchain_records_records_metric),
                        value = recordsCount.toString(),
                        modifier = Modifier.weight(1f),
                        accent = ChainTeal
                    )

                    HeroMetric(
                        label = stringResource(R.string.blockchain_records_latest_block),
                        value = latestBlock.removePrefix("Latest Block: ").take(12),
                        modifier = Modifier.weight(1f),
                        accent = ChainCoral
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
        color = ChainCoral.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, ChainCoral.copy(alpha = 0.30f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelSmall,
            color = ChainCoral,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HeroMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = accent.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.20f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value.ifBlank { stringResource(R.string.blockchain_records_not_available) },
                style = MaterialTheme.typography.titleLarge,
                color = ChainInk,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = ChainMuted,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BlockchainStatusCard(
    contractSummary: String,
    latestBlock: String
) {
    ModernBlockchainCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(54.dp),
                shape = RoundedCornerShape(18.dp),
                color = ChainGreen.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, ChainGreen.copy(alpha = 0.30f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.titleMedium,
                        color = ChainGreen,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.blockchain_records_connection_status),
                    style = MaterialTheme.typography.titleLarge,
                    color = ChainInk,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = stringResource(R.string.blockchain_records_connection_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ChainMuted
                )
            }

            StatusBadge(
                statusText = stringResource(R.string.blockchain_records_connected),
                accent = ChainGreen
            )
        }

        HorizontalDivider(color = ChainLine.copy(alpha = 0.80f))

        DetailItem(
            label = stringResource(R.string.blockchain_records_contract_summary),
            value = contractSummary,
            accent = ChainIndigo
        )

        DetailItem(
            label = stringResource(R.string.blockchain_records_latest_block),
            value = latestBlock,
            accent = ChainBlue
        )
    }
}

@Composable
private fun EmptyBlockchainRecordsState() {
    ModernBlockchainCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = CircleShape,
                color = ChainIndigo.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, ChainIndigo.copy(alpha = 0.22f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "0",
                        style = MaterialTheme.typography.titleLarge,
                        color = ChainIndigo,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            StatusBadge(
                statusText = stringResource(R.string.blockchain_records_waiting_for_receipt),
                accent = ChainAmber
            )

            Text(
                text = stringResource(R.string.blockchain_records_empty_title),
                style = MaterialTheme.typography.titleLarge,
                color = ChainInk,
                fontWeight = FontWeight.Black
            )

            Text(
                text = stringResource(R.string.blockchain_records_empty_message),
                style = MaterialTheme.typography.bodyMedium,
                color = ChainMuted
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.18f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Black
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.84f)
            )
        }
    }
}

@Composable
private fun VoteReceiptCard(
    receipt: VoteReceipt
) {
    ModernBlockchainCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(54.dp),
                shape = RoundedCornerShape(18.dp),
                color = ChainIndigo.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, ChainIndigo.copy(alpha = 0.24f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "TX",
                        style = MaterialTheme.typography.labelLarge,
                        color = ChainIndigo,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = receipt.electionTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = ChainInk,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = stringResource(
                        R.string.blockchain_records_transaction_short,
                        shortenHash(receipt.transactionHash)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ChainMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = FontFamily.Monospace
                )
            }

            StatusBadge(
                statusText = stringResource(R.string.blockchain_records_recorded),
                accent = ChainGreen
            )
        }

        HorizontalDivider(color = ChainLine.copy(alpha = 0.80f))

        DetailItem(
            label = stringResource(R.string.blockchain_records_election_id),
            value = receipt.electionId,
            accent = ChainBlue
        )

        DetailItem(
            label = stringResource(R.string.blockchain_records_voter_id),
            value = shortenWallet(receipt.voterId),
            accent = ChainTeal
        )

        DetailItem(
            label = stringResource(R.string.blockchain_records_candidate_name),
            value = receipt.candidateName,
            accent = ChainCoral
        )

        DetailItem(
            label = stringResource(R.string.blockchain_records_timestamp),
            value = formatReceiptTimestamp(receipt.timestamp),
            accent = ChainAmber
        )

        TransactionHashPanel(transactionHash = receipt.transactionHash)
    }
}

@Composable
private fun TransactionHashPanel(transactionHash: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = ChainIndigo.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, ChainIndigo.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(
                text = stringResource(R.string.blockchain_records_transaction_hash),
                style = MaterialTheme.typography.labelLarge,
                color = ChainIndigo,
                fontWeight = FontWeight.Black
            )

            Text(
                text = transactionHash,
                style = MaterialTheme.typography.bodySmall,
                color = ChainInk,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun StatusBadge(
    statusText: String,
    accent: Color
) {
    Surface(
        color = accent.copy(alpha = 0.12f),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.28f))
    ) {
        Text(
            text = statusText,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = accent,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String,
    accent: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = ChainPanel.copy(alpha = 0.86f),
        border = BorderStroke(1.dp, ChainLine.copy(alpha = 0.72f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = accent,
                fontWeight = FontWeight.Black
            )

            Text(
                text = value.ifBlank { stringResource(R.string.blockchain_records_not_available) },
                style = MaterialTheme.typography.bodyMedium,
                color = ChainMuted,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ModernBlockchainCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = ChainCard.copy(alpha = 0.96f)),
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

private fun formatReceiptTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

private fun displayValue(
    value: Any?,
    fallback: String
): String {
    val text = value?.toString()?.trim().orEmpty()
    return if (text.isBlank() || text == "null") fallback else text
}

private fun shortenHash(hash: String): String {
    val trimmed = hash.trim()

    return if (trimmed.length <= 18) {
        trimmed
    } else {
        trimmed.take(10) + "..." + trimmed.takeLast(6)
    }
}

private fun shortenWallet(wallet: String): String {
    val trimmed = wallet.trim()

    return if (trimmed.length <= 18) {
        trimmed
    } else {
        trimmed.take(10) + "..." + trimmed.takeLast(6)
    }
}