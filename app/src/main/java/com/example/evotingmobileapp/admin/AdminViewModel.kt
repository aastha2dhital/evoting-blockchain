package com.example.evotingmobileapp.admin

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.evotingmobileapp.blockchain.OnChainTransactionVerification
import com.example.evotingmobileapp.data.BlockchainElectionRepository
import com.example.evotingmobileapp.data.ElectionRepository
import com.example.evotingmobileapp.data.VoteValidationResult
import com.example.evotingmobileapp.model.Election
import com.example.evotingmobileapp.model.VoteReceipt
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VotingAccessUiState(
    val electionId: String = "",
    val voterId: String = "",
    val isLoading: Boolean = false,
    val canVote: Boolean = false,
    val message: String = "",
    val result: VoteValidationResult? = null
)

class AdminViewModel(
    private val repository: ElectionRepository
) : ViewModel() {

    val elections: StateFlow<List<Election>> = repository.elections
    val voteReceipts: StateFlow<List<VoteReceipt>> = repository.voteReceipts

    private val _latestReceipt = MutableStateFlow<VoteReceipt?>(null)
    val latestReceipt: StateFlow<VoteReceipt?> = _latestReceipt.asStateFlow()

    private val _turnoutCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val turnoutCounts: StateFlow<Map<String, Int>> = _turnoutCounts.asStateFlow()

    private val _votingAccessUiState = MutableStateFlow(VotingAccessUiState())
    val votingAccessUiState: StateFlow<VotingAccessUiState> =
        _votingAccessUiState.asStateFlow()

    private val latestVotingAccessRequestKey = AtomicReference("")
    private var votingAccessCheckJob: Job? = null

    private val _verifiedOnChainTransaction =
        MutableStateFlow<OnChainTransactionVerification?>(null)
    val verifiedOnChainTransaction: StateFlow<OnChainTransactionVerification?> =
        _verifiedOnChainTransaction.asStateFlow()

    private val _verificationInProgress = MutableStateFlow(false)
    val verificationInProgress: StateFlow<Boolean> =
        _verificationInProgress.asStateFlow()

    private val _verificationError = MutableStateFlow<String?>(null)
    val verificationError: StateFlow<String?> =
        _verificationError.asStateFlow()

    init {
        refreshBlockchainData()
    }

    fun refreshBlockchainData() {
        val blockchainRepository = repository as? BlockchainElectionRepository ?: return

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                blockchainRepository.refreshFromBlockchain()
            }
        }
    }

    fun createElection(
        title: String,
        candidates: List<String>,
        startTimeMillis: Long,
        endTimeMillis: Long,
        eligibleVoterIdsInput: String = ""
    ) {
        val cleanedTitle = title.trim()

        val cleanedCandidates = candidates
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        val cleanedEligibleWalletAddresses =
            parseEligibleWalletAddresses(eligibleVoterIdsInput)

        repository.createElection(
            title = cleanedTitle,
            candidates = cleanedCandidates,
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis,
            eligibleVoterIds = cleanedEligibleWalletAddresses
        )

        refreshBlockchainData()
    }

    fun checkInVoter(electionId: String, voterId: String): String {
        val cleanedWalletAddress = voterId.trim()

        val result = repository.checkInVoter(
            electionId = electionId.trim(),
            voterId = cleanedWalletAddress
        )

        refreshBlockchainData()

        return result
    }

    fun checkInVoterOnChain(
        context: Context,
        electionId: String,
        voterWalletAddress: String
    ): Result<String> {
        val blockchainRepository = repository as? BlockchainElectionRepository
            ?: return Result.failure(
                IllegalStateException("Blockchain election repository is not currently active.")
            )

        val result = blockchainRepository.checkInVoterOnChain(
            context = context,
            electionId = electionId.trim(),
            voterWalletAddress = voterWalletAddress.trim()
        )

        result.onSuccess {
            refreshBlockchainData()
        }

        return result
    }

    fun refreshVotingAccess(
        electionId: String,
        voterId: String
    ) {
        val cleanedElectionId = electionId.trim()
        val cleanedWalletAddress = voterId.trim()

        votingAccessCheckJob?.cancel()

        if (cleanedElectionId.isBlank()) {
            latestVotingAccessRequestKey.set("")
            _votingAccessUiState.value = VotingAccessUiState(
                message = "Select an election first."
            )
            return
        }

        if (cleanedWalletAddress.isBlank()) {
            latestVotingAccessRequestKey.set("")
            _votingAccessUiState.value = VotingAccessUiState(
                electionId = cleanedElectionId,
                message = "No active voter wallet session."
            )
            return
        }

        val requestKey = "$cleanedElectionId|$cleanedWalletAddress"
        latestVotingAccessRequestKey.set(requestKey)

        _votingAccessUiState.value = VotingAccessUiState(
            electionId = cleanedElectionId,
            voterId = cleanedWalletAddress,
            isLoading = true,
            canVote = false,
            message = "Checking voting access..."
        )

        votingAccessCheckJob = viewModelScope.launch(Dispatchers.IO) {
            val validationResult = runCatching {
                repository.validateVoting(
                    electionId = cleanedElectionId,
                    voterId = cleanedWalletAddress
                )
            }.getOrElse { exception ->
                VoteValidationResult(
                    success = false,
                    message = exception.message ?: "Failed to validate voting access."
                )
            }

            if (latestVotingAccessRequestKey.get() != requestKey) {
                return@launch
            }

            _votingAccessUiState.value = VotingAccessUiState(
                electionId = cleanedElectionId,
                voterId = cleanedWalletAddress,
                isLoading = false,
                canVote = validationResult.success,
                message = validationResult.message,
                result = validationResult
            )
        }
    }

    fun clearVotingAccessState() {
        votingAccessCheckJob?.cancel()
        latestVotingAccessRequestKey.set("")
        _votingAccessUiState.value = VotingAccessUiState()
    }

    fun validateVoting(
        electionId: String,
        voterId: String
    ): VoteValidationResult {
        val cleanedWalletAddress = voterId.trim()

        return repository.validateVoting(
            electionId = electionId.trim(),
            voterId = cleanedWalletAddress
        )
    }

    fun vote(
        electionId: String,
        voterId: String,
        candidateName: String
    ): VoteValidationResult {
        val cleanedElectionId = electionId.trim()
        val cleanedWalletAddress = voterId.trim()

        val result = repository.vote(
            electionId = cleanedElectionId,
            voterId = cleanedWalletAddress,
            candidateName = candidateName.trim()
        )

        if (result.success) {
            _latestReceipt.value = result.receipt
            refreshBlockchainData()
            loadTurnoutCount(cleanedElectionId)
        }

        return result
    }

    fun submitVote(
        electionId: String,
        voterId: String,
        candidateName: String
    ): VoteValidationResult {
        return vote(
            electionId = electionId,
            voterId = voterId,
            candidateName = candidateName
        )
    }

    fun closeElectionEarly(electionId: String): Result<String> {
        val cleanedElectionId = electionId.trim()

        val result = repository.closeElectionEarly(cleanedElectionId)

        result.onSuccess {
            refreshBlockchainData()
            loadTurnoutCount(cleanedElectionId)
        }

        return result
    }

    fun loadTurnoutCount(electionId: String) {
        val cleanedElectionId = electionId.trim()
        if (cleanedElectionId.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            repository.getTurnoutCount(cleanedElectionId)
                .onSuccess { turnout ->
                    _turnoutCounts.update { current ->
                        current + (cleanedElectionId to turnout)
                    }
                }
        }
    }

    fun getCachedTurnoutCount(electionId: String): Int? {
        return turnoutCounts.value[electionId.trim()]
    }

    fun verifyTransactionReceiptOnChain(
        context: Context,
        transactionHash: String
    ) {
        val blockchainRepository = repository as? BlockchainElectionRepository
        if (blockchainRepository == null) {
            _verifiedOnChainTransaction.value = null
            _verificationError.value =
                "Blockchain election repository is not currently active."
            _verificationInProgress.value = false
            return
        }

        val cleanedTransactionHash = transactionHash.trim()

        _verificationInProgress.value = true
        _verificationError.value = null
        _verifiedOnChainTransaction.value = null

        viewModelScope.launch(Dispatchers.IO) {
            val result = blockchainRepository.verifyTransactionReceiptOnChain(
                context = context,
                transactionHash = cleanedTransactionHash
            )

            result.fold(
                onSuccess = { verification ->
                    _verifiedOnChainTransaction.value = verification
                    _verificationError.value = null
                },
                onFailure = { exception ->
                    _verifiedOnChainTransaction.value = null
                    _verificationError.value =
                        exception.message ?: "Failed to verify transaction on-chain."
                }
            )

            _verificationInProgress.value = false
        }
    }

    fun clearOnChainVerification() {
        _verifiedOnChainTransaction.value = null
        _verificationError.value = null
        _verificationInProgress.value = false
    }

    fun findReceiptByTransactionHash(transactionHash: String): VoteReceipt? {
        val normalizedTransactionHash = normalizeTransactionHash(transactionHash)
        if (normalizedTransactionHash.isBlank()) return null

        return voteReceipts.value.firstOrNull { receipt ->
            normalizeTransactionHash(receipt.transactionHash) == normalizedTransactionHash
        }
    }

    fun selectReceiptByTransactionHash(transactionHash: String): VoteReceipt? {
        val matchedReceipt = findReceiptByTransactionHash(transactionHash)
        _latestReceipt.value = matchedReceipt
        return matchedReceipt
    }

    fun clearLatestReceipt() {
        _latestReceipt.value = null
    }

    private fun parseEligibleWalletAddresses(input: String): List<String> {
        return input
            .split(",", "\n", ";")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    private fun normalizeTransactionHash(transactionHash: String): String {
        return transactionHash.trim().lowercase()
    }
}