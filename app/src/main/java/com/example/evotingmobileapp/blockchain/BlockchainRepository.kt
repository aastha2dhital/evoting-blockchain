package com.example.evotingmobileapp.blockchain

import okhttp3.OkHttpClient
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import java.math.BigInteger
import java.util.concurrent.TimeUnit

class BlockchainRepository {

    private val rpcUrl = "http://10.0.2.2:8545"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val web3j: Web3j by lazy {
        Web3j.build(HttpService(rpcUrl, okHttpClient, false))
    }

    suspend fun getLatestBlockNumber(): Result<BigInteger> {
        return try {
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