package com.example.evotingmobileapp.admin

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.evotingmobileapp.data.BlockchainElectionRepository
import com.example.evotingmobileapp.data.ElectionRepository
import com.example.evotingmobileapp.data.VoteValidationResult
import com.example.evotingmobileapp.model.Election
import com.example.evotingmobileapp.model.VoteReceipt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel(
    private val repository: ElectionRepository
) : ViewModel() {

    companion object {
        const val DEMO_VOTER_WALLET_ADDRESS = "0x70997970C51812dc3A010C7d01b50e0d17dc79C8"
    }

    val elections: StateFlow<List<Election>> = repository.elections
    val voteReceipts: StateFlow<List<VoteReceipt>> = repository.voteReceipts

    private val _latestReceipt = MutableStateFlow<VoteReceipt?>(null)
    val latestReceipt: StateFlow<VoteReceipt?> = _latestReceipt.asStateFlow()

    private val _connectedWalletAddress = MutableStateFlow("")
    val connectedWalletAddress: StateFlow<String> = _connectedWalletAddress.asStateFlow()

    private val _walletConnected = MutableStateFlow(false)
    val walletConnected: StateFlow<Boolean> = _walletConnected.asStateFlow()

    init {
        refreshBlockchainData()
    }

    fun refreshBlockchainData() {
        val blockchainRepository = repository as? BlockchainElectionRepository ?: return

        viewModelScope.launch(Dispatchers.IO) {
            blockchainRepository.refreshFromBlockchain()
        }
    }

    fun connectDemoWallet() {
        setConnectedWalletAddress(DEMO_VOTER_WALLET_ADDRESS)
    }

    fun setConnectedWalletAddress(walletAddress: String) {
        val cleanedWalletAddress = walletAddress.trim()
        _connectedWalletAddress.value = cleanedWalletAddress
        _walletConnected.value = cleanedWalletAddress.isNotBlank()
    }

    fun clearConnectedWallet() {
        _connectedWalletAddress.value = ""
        _walletConnected.value = false
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

    private fun parseEligibleWalletAddresses(input: String): List<String> {
        return input
            .split(",", "\n", ";")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }
}