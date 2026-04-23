package com.example.evotingmobileapp.screens

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.evotingmobileapp.admin.AdminViewModel
import com.example.evotingmobileapp.auth.AuthSessionViewModel
import com.example.evotingmobileapp.data.VoteValidationResult
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
    val authUiState by authSessionViewModel.uiState.collectAsState()

    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var selectedElectionId by rememberSaveable { mutableStateOf("") }
    var selectedCandidate by rememberSaveable { mutableStateOf("") }
    var isSubmittingVote by rememberSaveable { mutableStateOf(false) }

    val isBusy = isSubmittingVote
    val selectedElection = elections.find { it.id == selectedElectionId }
    val voterWalletAddress = authUiState.walletAddress.trim()

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

    fun getElectionStatusText(): String {
        val election = selectedElection ?: return "No election selected"
        return when {
            election.isClosed() -> "Closed"
            election.isActive() -> "Active"
            else -> "Not Started"
        }
    }

    fun getVotingAccessText(): String {
        val election = selectedElection ?: return "Please select an election first."

        if (!authUiState.canAccessVoter() || voterWalletAddress.isBlank()) {
            return "No active voter session was found. Return to the voter access portal first."
        }

        val result = adminViewModel.validateVoting(
            electionId = election.id,
            voterId = voterWalletAddress
        )

        return if (result.success) {
            "Eligible and checked-in. Ready to vote."
        } else {
            result.message
        }
    }

    fun getVotingAccessSuccess(): Boolean {
        val election = selectedElection ?: return false
        if (!authUiState.canAccessVoter() || voterWalletAddress.isBlank()) return false
        return adminViewModel.validateVoting(
            electionId = election.id,
            voterId = voterWalletAddress
        ).success
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            VotingHeroCard()

            WalletIdentityCard(
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
                    electionStatus = getElectionStatusText(),
                    votingAccessText = getVotingAccessText(),
                    votingAccessSuccess = getVotingAccessSuccess(),
                    candidates = election.candidates,
                    selectedCandidate = selectedCandidate,
                    onCandidateSelected = { candidate ->
                        if (!isBusy) {
                            selectedCandidate = candidate
                        }
                    },
                    isBusy = isBusy,
                    isSubmittingVote = isSubmittingVote,
                    onSubmitVote = {
                        if (!authUiState.canAccessVoter() || voterWalletAddress.isBlank()) {
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar(
                                    "No active voter session found. Please return to Voter Access."
                                )
                            }
                            return@ElectionVotingCard
                        }

                        if (selectedCandidate.isBlank()) {
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar("Please select a candidate.")
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
                                    message = exception.message ?: "Blockchain vote failed."
                                )
                            }

                            if (finalResult.success) {
                                snackBarHostState.showSnackbar(
                                    "Congratulations. Your vote was recorded successfully."
                                )

                                val transactionHash = finalResult.receipt?.transactionHash.orEmpty()

                                if (transactionHash.isNotBlank()) {
                                    navController.navigate(
                                        AppRoutes.receiptRoute(transactionHash)
                                    )
                                } else {
                                    navController.navigate(AppRoutes.RECEIPT)
                                }
                            } else {
                                snackBarHostState.showSnackbar(finalResult.message)
                            }
                        }
                    }
                )
            }

            TextButton(
                onClick = { navController.popBackStack() },
                enabled = !isBusy
            ) {
                Text("Back")
            }
        }
    }
}

@Composable
private fun VotingHeroCard() {
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
                text = "Vote Now",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Complete your vote securely after QR check-in. This screen validates your voter session, election access, and final submission.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f)
            )

            PillColumn(
                items = listOf("1. Wallet Verified", "2. Check-In Required", "3. Submit Vote")
            )
        }
    }
}

@Composable
private fun WalletIdentityCard(
    voterWalletAddress: String,
    hasVoterAccess: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionTitle(
                title = "Wallet Identity",
                subtitle = "Your signed-in voter wallet is used for eligibility, check-in matching, and vote submission."
            )

            OutlinedTextField(
                value = voterWalletAddress,
                onValueChange = {},
                label = { Text("Signed-In Voter Wallet") },
                placeholder = { Text("0x...") },
                singleLine = true,
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )

            StatusInfoBadge(
                text = if (hasVoterAccess && voterWalletAddress.isNotBlank()) {
                    "Active voter session: ${shortenWalletAddress(voterWalletAddress)}"
                } else {
                    "No active voter wallet session found."
                },
                positive = hasVoterAccess && voterWalletAddress.isNotBlank()
            )
        }
    }
}

@Composable
private fun ElectionSelectionCard(
    elections: List<com.example.evotingmobileapp.model.Election>,
    selectedElectionId: String,
    onElectionSelected: (String) -> Unit,
    isBusy: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionTitle(
                title = "Select Election",
                subtitle = "Choose the election you want to participate in."
            )

            if (elections.isEmpty()) {
                StatusInfoBadge(
                    text = "No elections have been created yet.",
                    positive = false
                )
            } else {
                elections.forEach { election ->
                    SelectionCard(
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
    onSubmitVote: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionTitle(
                title = electionTitle,
                subtitle = "Election ready for review and candidate selection."
            )

            PillColumn(
                items = listOf(
                    "Status: $electionStatus",
                    "Election ID: $electionId"
                )
            )

            StatusInfoBadge(
                text = votingAccessText,
                positive = votingAccessSuccess
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Text(
                text = "Candidates",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            candidates.forEach { candidate ->
                SelectionCard(
                    title = candidate,
                    subtitle = "Select this candidate for your final vote.",
                    selected = selectedCandidate == candidate,
                    onSelected = { onCandidateSelected(candidate) },
                    enabled = !isBusy
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onSubmitVote,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isBusy,
                shape = RoundedCornerShape(18.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    if (isSubmittingVote) "Submitting Vote..." else "Submit Vote",
                    style = MaterialTheme.typography.labelLarge
                )
            }
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
private fun SelectionCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onSelected: () -> Unit,
    enabled: Boolean
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 4.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onSelected,
                enabled = enabled
            )

            Column(
                modifier = Modifier.padding(start = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.82f)
                )
            }
        }
    }
}

@Composable
private fun StatusInfoBadge(
    text: String,
    positive: Boolean
) {
    val containerColor = if (positive) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.tertiaryContainer
    }

    val contentColor = if (positive) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onTertiaryContainer
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = containerColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor
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

private fun shortenWalletAddress(address: String): String {
    if (address.length <= 16) return address
    return "${address.take(10)}...${address.takeLast(8)}"
}