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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.evotingmobileapp.R
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

    val noElectionSelectedText = stringResource(R.string.voting_status_no_election_selected)
    val closedStatusText = stringResource(R.string.voting_status_closed)
    val activeStatusText = stringResource(R.string.voting_status_active)
    val notStartedStatusText = stringResource(R.string.voting_status_not_started)
    val selectElectionFirstText = stringResource(R.string.voting_access_select_election_first)
    val noActiveSessionText = stringResource(R.string.voting_access_no_active_session)
    val readyToVoteText = stringResource(R.string.voting_access_ready)
    val noActiveSessionSnackbar = stringResource(R.string.voting_error_no_active_session)
    val selectCandidateSnackbar = stringResource(R.string.voting_error_select_candidate)
    val blockchainVoteFailedText = stringResource(R.string.voting_error_blockchain_vote_failed)
    val voteSuccessSnackbar = stringResource(R.string.voting_success_vote_recorded)

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
        val election = selectedElection ?: return noElectionSelectedText
        return when {
            election.isClosed() -> closedStatusText
            election.isActive() -> activeStatusText
            else -> notStartedStatusText
        }
    }

    fun getVotingAccessText(): String {
        val election = selectedElection ?: return selectElectionFirstText

        if (!authUiState.canAccessVoter() || voterWalletAddress.isBlank()) {
            return noActiveSessionText
        }

        val result = adminViewModel.validateVoting(
            electionId = election.id,
            voterId = voterWalletAddress
        )

        return if (result.success) {
            readyToVoteText
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
                                snackBarHostState.showSnackbar(noActiveSessionSnackbar)
                            }
                            return@ElectionVotingCard
                        }

                        if (selectedCandidate.isBlank()) {
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar(selectCandidateSnackbar)
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
                                    message = exception.message ?: blockchainVoteFailedText
                                )
                            }

                            if (finalResult.success) {
                                snackBarHostState.showSnackbar(voteSuccessSnackbar)

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
                Text(text = stringResource(R.string.voting_back_button))
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
                text = stringResource(R.string.voting_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = stringResource(R.string.voting_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f)
            )

            PillColumn(
                items = listOf(
                    stringResource(R.string.voting_step_wallet_verified),
                    stringResource(R.string.voting_step_check_in_required),
                    stringResource(R.string.voting_step_submit_vote)
                )
            )
        }
    }
}

@Composable
private fun WalletIdentityCard(
    voterWalletAddress: String,
    hasVoterAccess: Boolean
) {
    val hasActiveWallet = hasVoterAccess && voterWalletAddress.isNotBlank()

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
                title = stringResource(R.string.voting_wallet_identity_title),
                subtitle = stringResource(R.string.voting_wallet_identity_subtitle)
            )

            OutlinedTextField(
                value = voterWalletAddress,
                onValueChange = {},
                label = { Text(text = stringResource(R.string.voting_signed_in_wallet_label)) },
                placeholder = { Text(text = stringResource(R.string.voting_wallet_placeholder)) },
                singleLine = true,
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )

            StatusInfoBadge(
                text = if (hasActiveWallet) {
                    stringResource(
                        R.string.voting_active_session,
                        shortenWalletAddress(voterWalletAddress)
                    )
                } else {
                    stringResource(R.string.voting_no_active_wallet_session)
                },
                positive = hasActiveWallet
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
                title = stringResource(R.string.voting_select_election_title),
                subtitle = stringResource(R.string.voting_select_election_subtitle)
            )

            if (elections.isEmpty()) {
                StatusInfoBadge(
                    text = stringResource(R.string.voting_no_elections),
                    positive = false
                )
            } else {
                elections.forEach { election ->
                    SelectionCard(
                        title = election.title,
                        subtitle = stringResource(R.string.voting_election_id, election.id),
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
                subtitle = stringResource(R.string.voting_election_review_subtitle)
            )

            PillColumn(
                items = listOf(
                    stringResource(R.string.voting_status_summary, electionStatus),
                    stringResource(R.string.voting_election_id, electionId)
                )
            )

            StatusInfoBadge(
                text = votingAccessText,
                positive = votingAccessSuccess
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Text(
                text = stringResource(R.string.voting_candidates_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            candidates.forEach { candidate ->
                SelectionCard(
                    title = candidate,
                    subtitle = stringResource(R.string.voting_candidate_select_subtitle),
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
                    text = if (isSubmittingVote) {
                        stringResource(R.string.voting_submitting_vote_button)
                    } else {
                        stringResource(R.string.voting_submit_vote_button)
                    },
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
