package com.example.evotingmobileapp.blockchain

import android.content.Context
import okhttp3.OkHttpClient
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthCall
import org.web3j.protocol.core.methods.response.EthGetTransactionCount
import org.web3j.protocol.core.methods.response.EthSendTransaction
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.http.HttpService
import org.web3j.tx.response.PollingTransactionReceiptProcessor
import org.web3j.utils.Numeric
import java.math.BigInteger
import java.util.concurrent.TimeUnit

data class CreateElectionOnChainResult(
    val electionId: BigInteger,
    val createElectionTxHash: String,
    val candidateTxHashes: List<String>,
    val whitelistTxHash: String?
)

class BlockchainRepository {

    private companion object {
        const val HARDHAT_CHAIN_ID = 31337L
        val GAS_LIMIT: BigInteger = BigInteger("6000000")
        val MIN_GAS_PRICE_WEI: BigInteger = BigInteger("2000000000")
        const val RECEIPT_POLLING_INTERVAL_MS = 1000L
        const val RECEIPT_POLLING_ATTEMPTS = 40
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun buildWeb3j(rpcUrl: String): Web3j {
        return Web3j.build(HttpService(rpcUrl, okHttpClient, false))
    }

    fun getContractConfig(context: Context): Result<ContractConfig> {
        return try {
            Result.success(ContractAssets.loadContractConfig(context))
        } catch (exception: Exception) {
            Result.failure(exception)
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
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    fun createElectionOnChain(
        context: Context,
        title: String,
        candidateNames: List<String>,
        startTimeSeconds: BigInteger,
        endTimeSeconds: BigInteger,
        eligibleVoterAddresses: List<String>
    ): Result<CreateElectionOnChainResult> {
        return try {
            val normalizedTitle = title.trim()
            require(normalizedTitle.isNotBlank()) {
                "Election title is required."
            }

            require(startTimeSeconds >= BigInteger.ZERO) {
                "Start time must be a valid non-negative value."
            }

            require(endTimeSeconds > startTimeSeconds) {
                "End time must be after start time."
            }

            val normalizedCandidates = candidateNames
                .map { it.trim() }
                .filter { it.isNotBlank() }

            require(normalizedCandidates.isNotEmpty()) {
                "At least one candidate is required."
            }

            val normalizedVoterAddresses = eligibleVoterAddresses
                .map { it.trim() }
                .filter { it.isNotBlank() }

            val invalidAddresses = normalizedVoterAddresses
                .filterNot { WalletUtils.isValidAddress(it) }

            require(invalidAddresses.isEmpty()) {
                "Eligible voters must be valid wallet addresses only. Invalid entries: " +
                        invalidAddresses.joinToString(", ")
            }

            val createElectionFunction = Function(
                "createElection",
                listOf(
                    Utf8String(normalizedTitle),
                    Uint256(startTimeSeconds),
                    Uint256(endTimeSeconds)
                ),
                emptyList()
            )

            val createElectionTxHash = sendOwnerTransaction(
                context = context,
                function = createElectionFunction
            ).getOrThrow()

            val electionId = getElectionCountOnChain(context).getOrThrow()

            val candidateTxHashes = mutableListOf<String>()

            normalizedCandidates.forEach { candidateName ->
                val addCandidateFunction = Function(
                    "addCandidate",
                    listOf(
                        Uint256(electionId),
                        Utf8String(candidateName)
                    ),
                    emptyList()
                )

                val candidateTxHash = sendOwnerTransaction(
                    context = context,
                    function = addCandidateFunction
                ).getOrThrow()

                candidateTxHashes += candidateTxHash
            }

            val whitelistTxHash = if (normalizedVoterAddresses.isNotEmpty()) {
                val addressTypes = normalizedVoterAddresses.map { Address(it) }

                val addEligibleVotersFunction = Function(
                    "addEligibleVoters",
                    listOf(
                        Uint256(electionId),
                        DynamicArray(Address::class.java, addressTypes)
                    ),
                    emptyList()
                )

                sendOwnerTransaction(
                    context = context,
                    function = addEligibleVotersFunction
                ).getOrThrow()
            } else {
                null
            }

            Result.success(
                CreateElectionOnChainResult(
                    electionId = electionId,
                    createElectionTxHash = createElectionTxHash,
                    candidateTxHashes = candidateTxHashes,
                    whitelistTxHash = whitelistTxHash
                )
            )
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    fun checkInVoterOnChain(
        context: Context,
        electionId: BigInteger,
        voterWalletAddress: String
    ): Result<String> {
        return try {
            require(electionId >= BigInteger.ZERO) {
                "Election ID cannot be negative."
            }

            val normalizedAddress = voterWalletAddress.trim()

            require(normalizedAddress.isNotBlank()) {
                "Voter wallet address cannot be blank."
            }

            require(WalletUtils.isValidAddress(normalizedAddress)) {
                "Invalid voter wallet address."
            }

            val function = Function(
                "checkInVoter",
                listOf(
                    Uint256(electionId),
                    Address(normalizedAddress)
                ),
                emptyList()
            )

            sendOwnerTransaction(
                context = context,
                function = function
            )
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    fun voteOnChain(
        context: Context,
        electionId: BigInteger,
        candidateId: BigInteger,
        voterWalletAddress: String
    ): Result<String> {
        return try {
            require(electionId >= BigInteger.ZERO) {
                "Election ID cannot be negative."
            }

            require(candidateId > BigInteger.ZERO) {
                "Candidate ID must be greater than zero."
            }

            val normalizedAddress = voterWalletAddress.trim()

            require(normalizedAddress.isNotBlank()) {
                "Voter wallet address cannot be blank."
            }

            require(WalletUtils.isValidAddress(normalizedAddress)) {
                "Invalid voter wallet address."
            }

            val voterWalletConfig = ContractAssets.loadVoterWalletConfig(context)
            val voterCredentials = Credentials.create(voterWalletConfig.voterPrivateKey)

            require(voterCredentials.address.equals(normalizedAddress, ignoreCase = true)) {
                "The provided wallet address does not match voter-wallet.json."
            }

            val function = Function(
                "vote",
                listOf(
                    Uint256(electionId),
                    Uint256(candidateId)
                ),
                emptyList()
            )

            sendSignedTransaction(
                context = context,
                function = function,
                privateKey = voterWalletConfig.voterPrivateKey
            )
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    private fun sendOwnerTransaction(
        context: Context,
        function: Function
    ): Result<String> {
        return try {
            val adminWalletConfig = ContractAssets.loadAdminWalletConfig(context)

            sendSignedTransaction(
                context = context,
                function = function,
                privateKey = adminWalletConfig.adminPrivateKey
            )
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    private fun sendSignedTransaction(
        context: Context,
        function: Function,
        privateKey: String
    ): Result<String> {
        return try {
            val contractConfig = ContractAssets.loadContractConfig(context)
            val web3j = buildWeb3j(contractConfig.rpcUrl)
            val credentials = Credentials.create(privateKey)

            val gasPrice = resolveGasPrice(web3j).getOrThrow()
            val nonce = getPendingNonce(web3j, credentials.address).getOrThrow()
            val encodedFunction = FunctionEncoder.encode(function)

            val rawTransaction = RawTransaction.createTransaction(
                nonce,
                gasPrice,
                GAS_LIMIT,
                contractConfig.contractAddress,
                BigInteger.ZERO,
                encodedFunction
            )

            val signedMessage = TransactionEncoder.signMessage(
                rawTransaction,
                HARDHAT_CHAIN_ID,
                credentials
            )

            val signedHex = Numeric.toHexString(signedMessage)

            val response: EthSendTransaction = web3j.ethSendRawTransaction(signedHex).send()

            if (response.hasError()) {
                return Result.failure(
                    Exception("Blockchain transaction failed: ${response.error.message}")
                )
            }

            val txHash = response.transactionHash
            if (txHash.isNullOrBlank()) {
                return Result.failure(Exception("Blockchain transaction hash was empty."))
            }

            val receipt = waitForReceipt(web3j, txHash).getOrThrow()
            val status = receipt.status

            if (status != null && status != "0x1") {
                return Result.failure(
                    Exception("Blockchain transaction reverted. Tx hash: $txHash")
                )
            }

            Result.success(txHash)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    private fun resolveGasPrice(web3j: Web3j): Result<BigInteger> {
        return try {
            val response = web3j.ethGasPrice().send()

            if (response.hasError()) {
                Result.success(MIN_GAS_PRICE_WEI)
            } else {
                val networkGasPrice = response.gasPrice
                val resolvedGasPrice =
                    if (networkGasPrice >= MIN_GAS_PRICE_WEI) {
                        networkGasPrice
                    } else {
                        MIN_GAS_PRICE_WEI
                    }

                Result.success(resolvedGasPrice)
            }
        } catch (exception: Exception) {
            Result.success(MIN_GAS_PRICE_WEI)
        }
    }

    private fun getPendingNonce(
        web3j: Web3j,
        walletAddress: String
    ): Result<BigInteger> {
        return try {
            val response: EthGetTransactionCount = web3j.ethGetTransactionCount(
                walletAddress,
                DefaultBlockParameterName.PENDING
            ).send()

            if (response.hasError()) {
                Result.failure(
                    Exception("Failed to fetch nonce: ${response.error.message}")
                )
            } else {
                Result.success(response.transactionCount)
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    private fun waitForReceipt(
        web3j: Web3j,
        txHash: String
    ): Result<TransactionReceipt> {
        return try {
            val processor = PollingTransactionReceiptProcessor(
                web3j,
                RECEIPT_POLLING_INTERVAL_MS,
                RECEIPT_POLLING_ATTEMPTS
            )
            Result.success(processor.waitForTransactionReceipt(txHash))
        } catch (exception: Exception) {
            Result.failure(
                Exception(
                    "Transaction was sent but receipt was not confirmed for tx $txHash",
                    exception
                )
            )
        }
    }

    private fun getElectionCountOnChain(context: Context): Result<BigInteger> {
        return try {
            val contractConfig = ContractAssets.loadContractConfig(context)
            val adminWalletConfig = ContractAssets.loadAdminWalletConfig(context)

            val web3j = buildWeb3j(contractConfig.rpcUrl)
            val credentials = Credentials.create(adminWalletConfig.adminPrivateKey)

            val function = Function(
                "electionCount",
                emptyList(),
                listOf(object : TypeReference<Uint256>() {})
            )

            val encodedFunction = FunctionEncoder.encode(function)

            val response: EthCall = web3j.ethCall(
                Transaction.createEthCallTransaction(
                    credentials.address,
                    contractConfig.contractAddress,
                    encodedFunction
                ),
                DefaultBlockParameterName.LATEST
            ).send()

            if (response.hasError()) {
                return Result.failure(
                    Exception("Blockchain read failed: ${response.error.message}")
                )
            }

            val decoded = FunctionReturnDecoder.decode(
                response.value,
                function.outputParameters
            )

            if (decoded.isEmpty()) {
                Result.failure(Exception("Failed to read electionCount from blockchain."))
            } else {
                Result.success(decoded[0].value as BigInteger)
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}