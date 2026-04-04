package com.example.evotingmobileapp.blockchain

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.BigInteger

class BlockchainViewModel : ViewModel() {

    private val repository = BlockchainRepository()

    private val _latestBlock = MutableStateFlow("Latest Block Number")
    val latestBlock: StateFlow<String> = _latestBlock

    private val _contractSummary = MutableStateFlow("Contract not loaded")
    val contractSummary: StateFlow<String> = _contractSummary

    fun loadBlockchainStatus(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _latestBlock.value = "Loading blockchain status..."
            _contractSummary.value = "Loading contract config..."

            val configResult = repository.getContractConfig(context)

            configResult.onSuccess { config ->
                val shortAddress = if (config.contractAddress.length > 12) {
                    "${config.contractAddress.take(10)}...${config.contractAddress.takeLast(4)}"
                } else {
                    config.contractAddress
                }

                _contractSummary.value =
                    "Contract: $shortAddress (${config.network})"
            }

            configResult.onFailure { exception ->
                _contractSummary.value =
                    "Contract config error: ${exception.message ?: "Unknown error"}"
                _latestBlock.value = "Blockchain status unavailable"
                return@launch
            }

            val blockResult = repository.getLatestBlockNumber(context)

            blockResult.onSuccess { blockNumber: BigInteger ->
                _latestBlock.value = "Latest Block: $blockNumber"
            }

            blockResult.onFailure { exception ->
                _latestBlock.value = "Error: ${exception.message ?: "Unknown error"}"
            }
        }
    }
}