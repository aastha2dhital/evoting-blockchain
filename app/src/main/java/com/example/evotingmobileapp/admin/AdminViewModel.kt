package com.example.evotingmobileapp.admin

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.evotingmobileapp.data.BlockchainElectionRepository
import com.example.evotingmobileapp.data.ElectionRepository
import com.example.evotingmobileapp.data.VoteValidationResult
import com.example.evotingmobileapp.model.Election
import com.example.evotingmobileapp.model.VoteReceipt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdminViewModel(
    private val repository: ElectionRepository
) : ViewModel() {

    val elections: StateFlow<List<Election>> = repository.elections
    val voteReceipts: StateFlow<List<VoteReceipt>> = repository.voteReceipts

    private val _latestReceipt = MutableStateFlow<VoteReceipt?>(null)
    val latestReceipt: StateFlow<VoteReceipt?> = _latestReceipt.asStateFlow()

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