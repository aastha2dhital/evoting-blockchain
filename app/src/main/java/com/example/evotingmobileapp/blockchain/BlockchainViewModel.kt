package com.example.evotingmobileapp.blockchain

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

    fun loadLatestBlock() {
        viewModelScope.launch(Dispatchers.IO) {

            _latestBlock.value = "Loading..."

            val result = repository.getLatestBlockNumber()

            result.onSuccess { blockNumber: BigInteger ->
                _latestBlock.value = "Latest Block: $blockNumber"
            }

            result.onFailure { exception ->
                _latestBlock.value = "Error: ${exception.message ?: "Unknown error"}"
            }
        }
    }
}