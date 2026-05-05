package com.example.evotingmobileapp.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
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

private val VoteNavy = Color(0xFF07133A)
private val VoteDeepBlue = Color(0xFF102A70)
private val VoteIndigo = Color(0xFF4F32F6)
private val VotePurple = Color(0xFF7C3AED)
private val VoteBlue = Color(0xFF1479FF)
private val VoteTeal = Color(0xFF00B8A9)
private val VoteCoral = Color(0xFFFF5C7A)
private val VoteAmber = Color(0xFFFFB020)
private val VoteGreen = Color(0xFF17C964)
private val VoteCard = Color(0xFFF6F8FF)
private val VotePanel = Color(0xFFEAF1FF)
private val VoteInk = Color(0xFF081229)
private val VoteMuted = Color(0xFF59627E)
private val VoteLine = Color(0xFFD6DDF4)

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
            "Ready to vote. Eligibility and QR check-in are confirmed."

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
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            VoteNavy,
                            VoteDeepBlue,
                            Color(0xFF3155D8),
                            Color(0xFFD8ECFF)
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            VotingDecorativeBackground()

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
                VotingHeaderCard(
                    totalElections = elections.size,
                    selectedElectionTitle = selectedElection?.title
                )

                VotingProgressCard(
                    hasWallet = authUiState.canAccessVoter() && voterWalletAddress.isNotBlank(),
                    hasElection = selectedElection != null,
                    hasAccess = votingAccessSuccess,
                    hasCandidate = selectedCandidate.isNotBlank()
                )

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
                        text = "Back to dashboard",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun VotingDecorativeBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .size(230.dp)
                .offset(x = (-90).dp, y = 40.dp),
            shape = CircleShape,
            color = VoteTeal.copy(alpha = 0.16f)
        ) {}

        Surface(
            modifier = Modifier
                .size(190.dp)
                .align(Alignment.TopEnd)
                .offset(x = 72.dp, y = 88.dp),
            shape = CircleShape,
            color = VoteCoral.copy(alpha = 0.16f)
        ) {}

        Surface(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 98.dp, y = 36.dp),
            shape = CircleShape,
            color = VoteAmber.copy(alpha = 0.14f)
        ) {}

        Surface(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-130).dp, y = 110.dp),
            shape = CircleShape,
            color = VotePurple.copy(alpha = 0.15f)
        ) {}
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
        containerColor = VoteCard,
        shape = RoundedCornerShape(30.dp),
        title = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier.size(58.dp),
                    shape = CircleShape,
                    color = VoteGreen.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, VoteGreen.copy(alpha = 0.36f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "✓",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = VoteGreen
                        )
                    }
                }

                Text(
                    text = "Vote recorded",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = VoteInk,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = VoteGreen.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, VoteGreen.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Your ballot was submitted successfully.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = VoteInk
                        )

                        Text(
                            text = "A blockchain receipt is now available for audit verification.",
                            style = MaterialTheme.typography.bodySmall,
                            color = VoteMuted
                        )
                    }
                }

                if (transactionHash.isNotBlank()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.78f),
                        border = BorderStroke(1.dp, VoteLine)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Transaction hash",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = VoteInk
                            )

                            Text(
                                text = shortenLongValue(transactionHash),
                                style = MaterialTheme.typography.bodySmall,
                                color = VoteMuted
                            )
                        }
                    }
                }

                Text(
                    text = "Open the receipt screen to verify and save the transaction hash.",
                    style = MaterialTheme.typography.bodySmall,
                    color = VoteMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onViewReceipt,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VoteIndigo)
            ) {
                Text(
                    text = "View receipt",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onStay) {
                Text(
                    text = "Stay here",
                    color = VoteIndigo,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
private fun VotingHeaderCard(
    totalElections: Int,
    selectedElectionTitle: String?
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
                    .size(96.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 26.dp, y = (-30).dp),
                shape = CircleShape,
                color = VoteTeal.copy(alpha = 0.12f)
            ) {}

            Surface(
                modifier = Modifier
                    .size(74.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 30.dp, y = 22.dp),
                shape = CircleShape,
                color = VoteCoral.copy(alpha = 0.10f)
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
                        color = VoteIndigo,
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
                            fontWeight = FontWeight.Bold,
                            color = VoteIndigo
                        )

                        Text(
                            text = "Secure ballot",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = VoteInk
                        )
                    }

                    HeaderChip(text = "VOTER")
                }

                Text(
                    text = "Choose an election, confirm QR check-in access, select one candidate, and submit your ballot to the blockchain.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VoteMuted
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HeaderMetric(
                        modifier = Modifier.weight(1f),
                        label = "Elections",
                        value = totalElections.toString(),
                        accent = VoteBlue
                    )

                    HeaderMetric(
                        modifier = Modifier.weight(1f),
                        label = "Selected",
                        value = selectedElectionTitle ?: "None",
                        accent = VoteTeal
                    )
                }
            }
        }
    }
}

@Composable
private fun VotingProgressCard(
    hasWallet: Boolean,
    hasElection: Boolean,
    hasAccess: Boolean,
    hasCandidate: Boolean
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Voting checklist",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProgressStep(
                    modifier = Modifier.weight(1f),
                    number = "1",
                    label = "Wallet",
                    done = hasWallet
                )

                ProgressStep(
                    modifier = Modifier.weight(1f),
                    number = "2",
                    label = "Election",
                    done = hasElection
                )

                ProgressStep(
                    modifier = Modifier.weight(1f),
                    number = "3",
                    label = "Access",
                    done = hasAccess
                )

                ProgressStep(
                    modifier = Modifier.weight(1f),
                    number = "4",
                    label = "Choice",
                    done = hasCandidate
                )
            }
        }
    }
}

@Composable
private fun ProgressStep(
    modifier: Modifier = Modifier,
    number: String,
    label: String,
    done: Boolean
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = if (done) VoteGreen.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.12f),
        border = BorderStroke(
            width = 1.dp,
            color = if (done) VoteGreen.copy(alpha = 0.50f) else Color.White.copy(alpha = 0.18f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                color = if (done) VoteGreen else Color.White.copy(alpha = 0.16f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (done) "✓" else number,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.92f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HeaderMetric(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = accent.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = VoteMuted
            )

            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = VoteInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HeaderChip(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = VoteCoral.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, VoteCoral.copy(alpha = 0.30f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = VoteCoral
        )
    }
}

@Composable
private fun WalletSummaryCard(
    voterWalletAddress: String,
    hasVoterAccess: Boolean
) {
    val hasActiveWallet = hasVoterAccess && voterWalletAddress.isNotBlank()

    ModernVotingCard {
        SectionTitle(
            eyebrow = "IDENTITY",
            title = "Wallet identity",
            subtitle = if (hasActiveWallet) {
                "Active voter session: ${shortenLongValue(voterWalletAddress)}"
            } else {
                "No voter wallet is currently active."
            }
        )

        StatusPanel(
            text = if (hasActiveWallet) {
                "Wallet verified for this session: ${shortenLongValue(voterWalletAddress)}"
            } else {
                "Return to Voter Access to select a voter wallet before voting."
            },
            positive = hasActiveWallet
        )
    }
}

@Composable
private fun ElectionSelectionCard(
    elections: List<Election>,
    selectedElectionId: String,
    onElectionSelected: (String) -> Unit,
    isBusy: Boolean
) {
    ModernVotingCard {
        SectionTitle(
            eyebrow = "ELECTION",
            title = "Select election",
            subtitle = "Choose the election you are eligible to vote in."
        )

        if (elections.isEmpty()) {
            StatusPanel(
                text = "No elections are available yet. Ask the admin to create one first.",
                positive = false
            )
        } else {
            elections.forEach { election ->
                SelectableRow(
                    title = election.title,
                    subtitle = "Election ID: ${election.id}",
                    selected = selectedElectionId == election.id,
                    onSelected = { onElectionSelected(election.id) },
                    enabled = !isBusy,
                    leadingText = "E"
                )
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
    ModernVotingCard {
        SectionTitle(
            eyebrow = "BALLOT",
            title = electionTitle,
            subtitle = "Review the ballot carefully before submitting your vote."
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SmallInfoPill(
                modifier = Modifier.weight(1f),
                label = "Status",
                value = electionStatus,
                accent = if (electionStatus == "Active") VoteGreen else VoteAmber
            )

            SmallInfoPill(
                modifier = Modifier.weight(1f),
                label = "Election ID",
                value = electionId,
                accent = VoteBlue
            )
        }

        StatusPanel(
            text = votingAccessText,
            positive = votingAccessSuccess
        )

        HorizontalDivider(color = VoteLine.copy(alpha = 0.80f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Candidates",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = VoteInk
                )

                Text(
                    text = "Select only one candidate.",
                    style = MaterialTheme.typography.bodySmall,
                    color = VoteMuted
                )
            }

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = VoteIndigo.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, VoteIndigo.copy(alpha = 0.18f))
            ) {
                Text(
                    text = "${candidates.size} options",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = VoteIndigo
                )
            }
        }

        if (candidates.isEmpty()) {
            StatusPanel(
                text = "No candidates have been added to this election.",
                positive = false
            )
        } else {
            candidates.forEachIndexed { index, candidate ->
                SelectableRow(
                    title = candidate,
                    subtitle = if (selectedCandidate == candidate) {
                        "This candidate is selected for your blockchain ballot."
                    } else {
                        "Tap to select this candidate."
                    },
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
                .height(58.dp),
            enabled = canSubmitVote,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VoteCoral,
                disabledContainerColor = VoteMuted.copy(alpha = 0.28f),
                disabledContentColor = Color.White.copy(alpha = 0.72f)
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Text(
                text = if (isSubmittingVote) {
                    "Submitting vote..."
                } else {
                    "Submit secure vote"
                },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = VoteAmber.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, VoteAmber.copy(alpha = 0.22f))
        ) {
            Text(
                text = "Important: after submission, this wallet cannot vote again in the same election.",
                style = MaterialTheme.typography.bodySmall,
                color = VoteInk,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ModernVotingCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = VoteCard.copy(alpha = 0.96f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
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
            color = VoteTeal
        )

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = VoteInk
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = VoteMuted
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
        VoteIndigo.copy(alpha = 0.12f)
    } else {
        VotePanel.copy(alpha = 0.86f)
    }

    val border = if (selected) {
        BorderStroke(1.4.dp, VoteIndigo.copy(alpha = 0.50f))
    } else {
        BorderStroke(1.dp, VoteLine.copy(alpha = 0.72f))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onSelected() },
        shape = RoundedCornerShape(22.dp),
        color = containerColor,
        border = border
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            leadingText?.let { text ->
                Surface(
                    modifier = Modifier.size(50.dp),
                    shape = RoundedCornerShape(17.dp),
                    color = if (selected) VoteIndigo else Color.White.copy(alpha = 0.88f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (selected) VoteIndigo else VoteLine
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = if (selected) Color.White else VoteIndigo,
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
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = VoteInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = VoteMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (selected) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = VoteGreen.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, VoteGreen.copy(alpha = 0.28f))
                ) {
                    Text(
                        text = "Selected",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = VoteGreen,
                        fontWeight = FontWeight.Black
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
        VoteGreen.copy(alpha = 0.13f)
    } else {
        VoteAmber.copy(alpha = 0.14f)
    }

    val accentColor = if (positive) VoteGreen else VoteAmber

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.28f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.size(30.dp),
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.16f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (positive) "✓" else "!",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = accentColor
                    )
                }
            }

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = VoteInk,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SmallInfoPill(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = accent.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.20f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = VoteMuted,
                textAlign = TextAlign.Center
            )

            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = VoteInk,
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