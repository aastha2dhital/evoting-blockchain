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

    fun checkInVoterOnChain(
        context: Context,
        electionId: BigInteger,
        voterWalletAddress: String
    ): Result<String> {
        return try {
            if (electionId < BigInteger.ZERO) {
                return Result.failure(
                    IllegalArgumentException("Election ID cannot be negative.")
                )
            }

            if (voterWalletAddress.isBlank()) {
                return Result.failure(
                    IllegalArgumentException("Voter wallet address cannot be blank.")
                )
            }

            val contractConfig = ContractAssets.loadContractConfig(context)
            val latestBlockResult = getLatestBlockNumber(context)

            if (latestBlockResult.isFailure) {
                Result.failure(
                    latestBlockResult.exceptionOrNull()
                        ?: Exception("Unable to reach blockchain before check-in transaction.")
                )
            } else {
                Result.failure(
                    UnsupportedOperationException(
                        "Blockchain check-in transaction sending is not implemented yet. " +
                                "A real signer/wallet path is still required for contract ${contractConfig.contractAddress}."
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}