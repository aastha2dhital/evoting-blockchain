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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdminViewModel(
    private val repository: ElectionRepository
) : ViewModel() {

    val elections: StateFlow<List<Election>> = repository.elections
    val voteReceipts: StateFlow<List<VoteReceipt>> = repository.voteReceipts

    private val _latestReceipt = MutableStateFlow<VoteReceipt?>(null)
    val latestReceipt: StateFlow<VoteReceipt?> = _latestReceipt.asStateFlow()

    private val _turnoutCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val turnoutCounts: StateFlow<Map<String, Int>> = _turnoutCounts.asStateFlow()

    private val _verifiedOnChainTransaction = MutableStateFlow<OnChainTransactionVerification?>(null)
    val verifiedOnChainTransaction: StateFlow<OnChainTransactionVerification?> =
        _verifiedOnChainTransaction.asStateFlow()

    private val _verificationInProgress = MutableStateFlow(false)
    val verificationInProgress: StateFlow<Boolean> = _verificationInProgress.asStateFlow()

    private val _verificationError = MutableStateFlow<String?>(null)
    val verificationError: StateFlow<String?> = _verificationError.asStateFlow()

    fun refreshBlockchainData() {
        val blockchainRepository = repository as? BlockchainElectionRepository ?: return

        viewModelScope.launch(Dispatchers.IO) {
            blockchainRepository.refreshFromBlockchain()
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

        val cleanedEligibleWalletAddresses = parseEligibleWalletAddresses(eligibleVoterIdsInput)

        repository.createElection(
            title = cleanedTitle,
            candidates = cleanedCandidates,
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis,
            eligibleVoterIds = cleanedEligibleWalletAddresses
        )
    }

    fun checkInVoter(electionId: String, voterId: String): String {
        val cleanedWalletAddress = voterId.trim()

        return repository.checkInVoter(
            electionId = electionId.trim(),
            voterId = cleanedWalletAddress
        )
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

        return blockchainRepository.checkInVoterOnChain(
            context = context,
            electionId = electionId.trim(),
            voterWalletAddress = voterWalletAddress.trim()
        )
    }

    fun validateVoting(electionId: String, voterId: String): VoteValidationResult {
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
        val cleanedWalletAddress = voterId.trim()

        val result = repository.vote(
            electionId = electionId.trim(),
            voterId = cleanedWalletAddress,
            candidateName = candidateName.trim()
        )

        if (result.success) {
            _latestReceipt.value = result.receipt
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
        return repository.closeElectionEarly(electionId.trim())
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
            _verificationError.value = "Blockchain election repository is not currently active."
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