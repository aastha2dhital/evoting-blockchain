package com.example.evotingmobileapp.blockchain

import android.content.Context
import okhttp3.OkHttpClient
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import java.math.BigInteger
import java.util.concurrent.TimeUnit

class BlockchainRepository {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun buildWeb3j(rpcUrl: String): Web3j {
        return Web3j.build(HttpService(rpcUrl, okHttpClient, false))
    }

    fun getContractConfig(context: Context): Result<ContractConfig> {
        return try {
            Result.success(ContractAssets.loadContractConfig(context))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getLatestBlockNumber(context: Context? = null): Result<BigInteger> {
        return try {
            val rpcUrl = if (context != null) {
                ContractAssets.loadContractConfig(context).rpcUrl
            } else {
                "http://10.0.2.2:8545"
            }

            val web3j = buildWeb3j(rpcUrl)
            val response = web3j.ethBlockNumber().send()

            if (response.hasError()) {
                Result.failure(
                    Exception("RPC error ${response.error.code}: ${response.error.message}")
                )
            } else {
                Result.success(response.blockNumber)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}