package com.example.evotingmobileapp.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.evotingmobileapp.admin.AdminViewModel
import com.example.evotingmobileapp.auth.AuthSessionViewModel
import com.example.evotingmobileapp.data.VoteValidationResult
import com.example.evotingmobileapp.model.Election
import com.example.evotingmobileapp.navigation.AppRoutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun VotingScreen(
    navController: NavHostController,
    adminViewModel: AdminViewModel,
    authSessionViewModel: AuthSessionViewModel,
    modifier: Modifier = Modifier
) {
    val elections by adminViewModel.elections.collectAsState()
    val votingAccessUiState by adminViewModel.votingAccessUiState.collectAsState()
    val authUiState by authSessionViewModel.uiState.collectAsState()

    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var selectedElectionId by rememberSaveable { mutableStateOf("") }
    var selectedCandidate by rememberSaveable { mutableStateOf("") }
    var isSubmittingVote by rememberSaveable { mutableStateOf(false) }
    var showVoteSuccessDialog by rememberSaveable { mutableStateOf(false) }
    var successReceiptTransactionHash by rememberSaveable { mutableStateOf("") }

    val selectedElection = elections.find { it.id == selectedElectionId }
    val voterWalletAddress = authUiState.walletAddress.trim()

    val votingAccessMatchesCurrentSelection =
        selectedElection != null &&
                votingAccessUiState.electionId == selectedElection.id &&
                votingAccessUiState.voterId.equals(voterWalletAddress, ignoreCase = true)

    val votingAccessText = when {
        selectedElection == null -> "Select an election to continue."

        !authUiState.canAccessVoter() || voterWalletAddress.isBlank() ->
            "No active voter wallet session found."

        votingAccessUiState.isLoading &&
                votingAccessUiState.electionId == selectedElection.id ->
            votingAccessUiState.message.ifBlank { "Checking voting access..." }

        votingAccessMatchesCurrentSelection && votingAccessUiState.canVote ->
            "Ready to vote. Eligibility and check-in are confirmed."

        votingAccessMatchesCurrentSelection ->
            votingAccessUiState.message.ifBlank { "Voting access could not be confirmed." }

        else -> "Checking voting access..."
    }

    val votingAccessSuccess =
        votingAccessMatchesCurrentSelection &&
                !votingAccessUiState.isLoading &&
                votingAccessUiState.canVote

    val isBusy = isSubmittingVote || votingAccessUiState.isLoading

    LaunchedEffect(elections, selectedElectionId) {
        if (selectedElectionId.isNotBlank() && selectedElection == null) {
            selectedElectionId = ""
        }

        if (selectedElection == null) {
            selectedCandidate = ""
        } else if (
            selectedCandidate.isNotBlank() &&
            !selectedElection.candidates.contains(selectedCandidate)
        ) {
            selectedCandidate = ""
        }
    }

    LaunchedEffect(
        selectedElectionId,
        voterWalletAddress,
        authUiState.canAccessVoter()
    ) {
        val election = selectedElection

        if (election == null) {
            adminViewModel.clearVotingAccessState()
            return@LaunchedEffect
        }

        if (!authUiState.canAccessVoter() || voterWalletAddress.isBlank()) {
            adminViewModel.clearVotingAccessState()
            return@LaunchedEffect
        }

        adminViewModel.refreshVotingAccess(
            electionId = election.id,
            voterId = voterWalletAddress
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            adminViewModel.clearVotingAccessState()
        }
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.24f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.14f)
        )
    )

    if (showVoteSuccessDialog) {
        VoteSuccessDialog(
            transactionHash = successReceiptTransactionHash,
            onViewReceipt = {
                showVoteSuccessDialog = false

                if (successReceiptTransactionHash.isNotBlank()) {
                    navController.navigate(
                        AppRoutes.receiptRoute(successReceiptTransactionHash)
                    )
                } else {
                    navController.navigate(AppRoutes.RECEIPT)
                }
            },
            onStay = {
                showVoteSuccessDialog = false
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            VotingHeaderCard()

            WalletSummaryCard(
                voterWalletAddress = voterWalletAddress,
                hasVoterAccess = authUiState.canAccessVoter()
            )

            ElectionSelectionCard(
                elections = elections,
                selectedElectionId = selectedElectionId,
                onElectionSelected = { electionId ->
                    if (!isBusy) {
                        selectedElectionId = electionId
                        selectedCandidate = ""
                    }
                },
                isBusy = isBusy
            )

            selectedElection?.let { election ->
                ElectionVotingCard(
                    electionTitle = election.title,
                    electionId = election.id,
                    electionStatus = getElectionStatusText(election),
                    votingAccessText = votingAccessText,
                    votingAccessSuccess = votingAccessSuccess,
                    candidates = election.candidates,
                    selectedCandidate = selectedCandidate,
                    onCandidateSelected = { candidate ->
                        if (!isBusy) {
                            selectedCandidate = candidate
                        }
                    },
                    isBusy = isBusy,
                    isSubmittingVote = isSubmittingVote,
                    canSubmitVote = votingAccessSuccess &&
                            selectedCandidate.isNotBlank() &&
                            !isSubmittingVote,
                    onSubmitVote = {
                        if (!authUiState.canAccessVoter() || voterWalletAddress.isBlank()) {
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar(
                                    "No active voter session found. Return to Voter Access first."
                                )
                            }
                            return@ElectionVotingCard
                        }

                        if (!votingAccessSuccess) {
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar(votingAccessText)
                            }
                            return@ElectionVotingCard
                        }

                        if (selectedCandidate.isBlank()) {
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar("Select a candidate first.")
                            }
                            return@ElectionVotingCard
                        }

                        isSubmittingVote = true

                        coroutineScope.launch {
                            val result = runCatching {
                                withContext(Dispatchers.IO) {
                                    adminViewModel.submitVote(
                                        electionId = election.id,
                                        voterId = voterWalletAddress,
                                        candidateName = selectedCandidate
                                    )
                                }
                            }

                            isSubmittingVote = false

                            val finalResult = result.getOrElse { exception ->
                                VoteValidationResult(
                                    success = false,
                                    message = exception.message ?: "Blockchain vote submission failed."
                                )
                            }

                            if (finalResult.success) {
                                successReceiptTransactionHash =
                                    finalResult.receipt?.transactionHash.orEmpty()

                                showVoteSuccessDialog = true

                                adminViewModel.refreshVotingAccess(
                                    electionId = election.id,
                                    voterId = voterWalletAddress
                                )
                            } else {
                                snackBarHostState.showSnackbar(finalResult.message)
                            }
                        }
                    }
                )
            }

            TextButton(
                onClick = { navController.popBackStack() },
                enabled = !isBusy,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Back",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun VoteSuccessDialog(
    transactionHash: String,
    onViewReceipt: () -> Unit,
    onStay: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            // Keep confirmation visible until the voter chooses an action.
        },
        title = {
            Text(
                text = "Vote recorded",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.70f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Your vote was submitted successfully.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Text(
                            text = "A blockchain receipt is now available for verification.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f)
                        )
                    }
                }

                if (transactionHash.isNotBlank()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.80f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Transaction hash",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = shortenLongValue(transactionHash),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Text(
                    text = "Open the receipt screen to verify and save the transaction hash.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onViewReceipt,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = "View receipt")
            }
        },
        dismissButton = {
            TextButton(onClick = onStay) {
                Text(text = "Stay here")
            }
        }
    )
}

@Composable
private fun VotingHeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "SV",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Voter ballot",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                HeaderChip(text = "VOTER")
            }

            Text(
                text = "Choose an election, confirm voting access, and submit your ballot to the blockchain.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HeaderChip(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun WalletSummaryCard(
    voterWalletAddress: String,
    hasVoterAccess: Boolean
) {
    val hasActiveWallet = hasVoterAccess && voterWalletAddress.isNotBlank()

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
            SectionTitle(
                title = "Wallet identity",
                subtitle = if (hasActiveWallet) {
                    "Active voter session: ${shortenLongValue(voterWalletAddress)}"
                } else {
                    "No voter wallet is currently active."
                }
            )

            StatusPanel(
                text = if (hasActiveWallet) {
                    shortenLongValue(voterWalletAddress)
                } else {
                    "Return to Voter Access to select a voter wallet."
                },
                positive = hasActiveWallet
            )
        }
    }
}

@Composable
private fun ElectionSelectionCard(
    elections: List<Election>,
    selectedElectionId: String,
    onElectionSelected: (String) -> Unit,
    isBusy: Boolean
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
            SectionTitle(
                title = "Select election",
                subtitle = "Choose the election you are eligible to vote in."
            )

            if (elections.isEmpty()) {
                StatusPanel(
                    text = "No elections are available yet.",
                    positive = false
                )
            } else {
                elections.forEach { election ->
                    SelectableRow(
                        title = election.title,
                        subtitle = "Election ID: ${election.id}",
                        selected = selectedElectionId == election.id,
                        onSelected = { onElectionSelected(election.id) },
                        enabled = !isBusy
                    )
                }
            }
        }
    }
}

@Composable
private fun ElectionVotingCard(
    electionTitle: String,
    electionId: String,
    electionStatus: String,
    votingAccessText: String,
    votingAccessSuccess: Boolean,
    candidates: List<String>,
    selectedCandidate: String,
    onCandidateSelected: (String) -> Unit,
    isBusy: Boolean,
    isSubmittingVote: Boolean,
    canSubmitVote: Boolean,
    onSubmitVote: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionTitle(
                title = electionTitle,
                subtitle = "Review the ballot before submitting your vote."
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SmallInfoPill(
                    modifier = Modifier.weight(1f),
                    label = "Status",
                    value = electionStatus
                )

                SmallInfoPill(
                    modifier = Modifier.weight(1f),
                    label = "Election",
                    value = electionId
                )
            }

            StatusPanel(
                text = votingAccessText,
                positive = votingAccessSuccess
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)
            )

            Text(
                text = "Candidates",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (candidates.isEmpty()) {
                StatusPanel(
                    text = "No candidates have been added to this election.",
                    positive = false
                )
            } else {
                candidates.forEachIndexed { index, candidate ->
                    SelectableRow(
                        title = candidate,
                        subtitle = "Tap to select this candidate.",
                        selected = selectedCandidate == candidate,
                        onSelected = { onCandidateSelected(candidate) },
                        enabled = !isBusy,
                        leadingText = candidateSymbolFor(candidate, index)
                    )
                }
            }

            Button(
                onClick = onSubmitVote,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = canSubmitVote,
                shape = RoundedCornerShape(18.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
            ) {
                Text(
                    text = if (isSubmittingVote) {
                        "Submitting vote..."
                    } else {
                        "Submit vote"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "After submission, this wallet cannot vote again in the same election.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
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
            fontWeight = FontWeight.SemiBold,
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
private fun SelectableRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onSelected: () -> Unit,
    enabled: Boolean,
    leadingText: String? = null
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.60f)
    }

    val border = if (selected) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.42f))
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onSelected() },
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = border
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            leadingText?.let { text ->
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

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
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (selected) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = "Selected",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusPanel(
    text: String,
    positive: Boolean
) {
    val containerColor = if (positive) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.62f)
    } else {
        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.66f)
    }

    val contentColor = if (positive) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onTertiaryContainer
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = containerColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SmallInfoPill(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.58f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.78f),
                textAlign = TextAlign.Center
            )

            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun getElectionStatusText(election: Election): String {
    return when {
        election.isClosed() -> "Closed"
        election.isActive() -> "Active"
        else -> "Not started"
    }
}

private fun candidateSymbolFor(candidateName: String, index: Int): String {
    val cleanedName = candidateName.trim()

    if (cleanedName.isNotBlank()) {
        val firstLetter = cleanedName.first().uppercaseChar()
        if (firstLetter.isLetterOrDigit()) {
            return firstLetter.toString()
        }
    }

    return (index + 1).toString()
}

private fun shortenLongValue(value: String): String {
    if (value.length <= 18) return value
    return "${value.take(10)}...${value.takeLast(8)}"
}