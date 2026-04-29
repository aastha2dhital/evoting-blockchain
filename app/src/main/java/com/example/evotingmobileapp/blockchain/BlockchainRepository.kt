package com.example.evotingmobileapp.blockchain

import android.content.Context
import com.example.evotingmobileapp.model.Election
import java.math.BigInteger
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Bool
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.crypto.Hash
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthGetTransactionCount
import org.web3j.protocol.core.methods.response.EthSendTransaction
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.http.HttpService
import org.web3j.tx.response.PollingTransactionReceiptProcessor
import org.web3j.utils.Numeric

data class CreateElectionOnChainResult(
    val electionId: BigInteger,
    val createElectionTxHash: String,
    val candidateTxHashes: List<String>,
    val whitelistTxHash: String?
)

data class OnChainTransactionVerification(
    val transactionHash: String,
    val blockNumber: BigInteger,
    val status: String,
    val fromAddress: String,
    val toAddress: String?,
    val gasUsed: BigInteger
)

private data class ReadOnlyChainContext(
    val web3j: Web3j,
    val fromAddress: String,
    val contractAddress: String
)

private data class SigningWalletContext(
    val credentials: Credentials,
    val privateKey: String
)

class BlockchainRepository {

    private companion object {
        const val HARDHAT_CHAIN_ID = 31337L
        val GAS_LIMIT: BigInteger = BigInteger("6000000")
        val MIN_GAS_PRICE_WEI: BigInteger = BigInteger("2000000000")
        const val RECEIPT_POLLING_INTERVAL_MS = 1000L
        const val RECEIPT_POLLING_ATTEMPTS = 40
        val VOTE_CAST_EVENT_TOPIC: String = Hash.sha3String("VoteCast(uint256,uint256,address)")
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

    fun getAllElectionsOnChain(context: Context): Result<List<Election>> {
        return try {
            val electionCount = getElectionCountOnChain(context).getOrThrow()
            val elections = buildList {
                for (electionId in 1..electionCount.toInt()) {
                    add(getElectionOnChain(context, BigInteger.valueOf(electionId.toLong())).getOrThrow())
                }
            }

            Result.success(elections)
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

            val normalizedAddress = normalizeAndValidateWalletAddress(voterWalletAddress)

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

    fun closeElectionEarlyOnChain(
        context: Context,
        electionId: BigInteger
    ): Result<String> {
        return try {
            require(electionId >= BigInteger.ZERO) {
                "Election ID cannot be negative."
            }

            val function = Function(
                "closeElectionEarly",
                listOf(Uint256(electionId)),
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

    fun getTurnoutCountOnChain(
        context: Context,
        electionId: BigInteger
    ): Result<Int> {
        return try {
            require(electionId >= BigInteger.ZERO) {
                "Election ID cannot be negative."
            }

            val function = Function(
                "getTurnoutCount",
                listOf(Uint256(electionId)),
                listOf(object : TypeReference<Uint256>() {})
            )

            val decodedValues = executeReadonlyFunction(
                context = context,
                function = function
            ).getOrThrow()

            if (decodedValues.isEmpty()) {
                Result.failure(Exception("Failed to read turnout count from blockchain."))
            } else {
                Result.success((decodedValues[0].value as BigInteger).toInt())
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    fun isEligibleVoterOnChain(
        context: Context,
        electionId: BigInteger,
        voterWalletAddress: String
    ): Result<Boolean> {
        return readVoterBooleanFlagOnChain(
            context = context,
            electionId = electionId,
            voterWalletAddress = voterWalletAddress,
            functionName = "isEligibleVoter",
            failureMessage = "Failed to read voter eligibility from blockchain."
        )
    }

    fun isCheckedInOnChain(
        context: Context,
        electionId: BigInteger,
        voterWalletAddress: String
    ): Result<Boolean> {
        return readVoterBooleanFlagOnChain(
            context = context,
            electionId = electionId,
            voterWalletAddress = voterWalletAddress,
            functionName = "isCheckedIn",
            failureMessage = "Failed to read voter check-in status from blockchain."
        )
    }

    fun hasVotedOnChain(
        context: Context,
        electionId: BigInteger,
        voterWalletAddress: String
    ): Result<Boolean> {
        return readVoterBooleanFlagOnChain(
            context = context,
            electionId = electionId,
            voterWalletAddress = voterWalletAddress,
            functionName = "hasVoted",
            failureMessage = "Failed to read voter voting status from blockchain."
        )
    }

    private fun readVoterBooleanFlagOnChain(
        context: Context,
        electionId: BigInteger,
        voterWalletAddress: String,
        functionName: String,
        failureMessage: String
    ): Result<Boolean> {
        return try {
            require(electionId >= BigInteger.ZERO) {
                "Election ID cannot be negative."
            }

            val normalizedAddress = normalizeAndValidateWalletAddress(voterWalletAddress)

            val function = Function(
                functionName,
                listOf(
                    Uint256(electionId),
                    Address(normalizedAddress)
                ),
                listOf(object : TypeReference<Bool>() {})
            )

            val decodedValues = executeReadonlyFunction(
                context = context,
                function = function
            ).getOrThrow()

            if (decodedValues.isEmpty()) {
                Result.failure(Exception(failureMessage))
            } else {
                Result.success(decodedValues[0].value as Boolean)
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    fun verifyTransactionReceiptOnChain(
        context: Context,
        transactionHash: String
    ): Result<OnChainTransactionVerification> {
        return try {
            val normalizedHash = transactionHash.trim()

            require(
                normalizedHash.matches(Regex("^0x[a-fA-F0-9]{64}$"))
            ) {
                "Enter a valid transaction hash."
            }

            val contractConfig = ContractAssets.loadContractConfig(context)
            val web3j = buildWeb3j(contractConfig.rpcUrl)

            val response = web3j.ethGetTransactionReceipt(normalizedHash).send()

            if (response.hasError()) {
                return Result.failure(
                    Exception("Blockchain verification failed: ${response.error.message}")
                )
            }

            val optionalReceipt = response.transactionReceipt
            if (optionalReceipt.isEmpty) {
                return Result.failure(
                    Exception("No confirmed transaction receipt was found on-chain for this hash.")
                )
            }

            val receipt = optionalReceipt.get()
            val status = receipt.status ?: "unknown"

            if (status != "0x1") {
                return Result.failure(
                    Exception("Transaction was found on-chain but it failed or reverted.")
                )
            }

            val receiptToAddress = receipt.to.orEmpty()
            if (!receiptToAddress.equals(contractConfig.contractAddress, ignoreCase = true)) {
                return Result.failure(
                    Exception("Transaction was found, but it was not sent to the configured voting contract.")
                )
            }

            val hasVoteCastEvent = receipt.logs.any { log ->
                log.topics.any { topic ->
                    topic.equals(VOTE_CAST_EVENT_TOPIC, ignoreCase = true)
                }
            }

            if (!hasVoteCastEvent) {
                return Result.failure(
                    Exception("Transaction was found on the voting contract, but it is not a vote receipt transaction.")
                )
            }

            Result.success(
                OnChainTransactionVerification(
                    transactionHash = receipt.transactionHash,
                    blockNumber = receipt.blockNumber,
                    status = status,
                    fromAddress = receipt.from,
                    toAddress = receipt.to,
                    gasUsed = receipt.gasUsed
                )
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

            val normalizedAddress = normalizeAndValidateWalletAddress(voterWalletAddress)

            val voterSigningWallet = loadValidatedVoterSigningWallet(
                context = context,
                expectedWalletAddress = normalizedAddress
            ).getOrThrow()

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
                privateKey = voterSigningWallet.privateKey
            )
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    private fun getElectionOnChain(
        context: Context,
        electionId: BigInteger
    ): Result<Election> {
        return try {
            val function = Function(
                "getElection",
                listOf(Uint256(electionId)),
                listOf(
                    object : TypeReference<Uint256>() {},
                    object : TypeReference<Utf8String>() {},
                    object : TypeReference<Uint256>() {},
                    object : TypeReference<Uint256>() {},
                    object : TypeReference<Uint256>() {},
                    object : TypeReference<Uint256>() {},
                    object : TypeReference<Bool>() {}
                )
            )

            val decodedValues = executeReadonlyFunction(
                context = context,
                function = function
            ).getOrThrow()

            require(decodedValues.size >= 7) {
                "Failed to decode election $electionId from blockchain."
            }

            val title = decodedValues[1].value as String
            val startTimeSeconds = decodedValues[2].value as BigInteger
            val endTimeSeconds = decodedValues[3].value as BigInteger
            val candidateCount = decodedValues[4].value as BigInteger
            val isClosed = decodedValues[6].value as Boolean

            val candidateNames = mutableListOf<String>()
            val voteCounts = linkedMapOf<String, Int>()

            for (candidateId in 1..candidateCount.toInt()) {
                val candidate = getCandidateOnChain(
                    context = context,
                    electionId = electionId,
                    candidateId = BigInteger.valueOf(candidateId.toLong())
                ).getOrThrow()

                candidateNames += candidate.first
                voteCounts[candidate.first] = candidate.second
            }

            Result.success(
                Election(
                    id = electionId.toString(),
                    title = title,
                    candidates = candidateNames,
                    startTimeMillis = startTimeSeconds.toLong() * 1000L,
                    endTimeMillis = endTimeSeconds.toLong() * 1000L,
                    isManuallyClosed = isClosed,
                    voteCounts = voteCounts,
                    votedVoterIds = emptySet(),
                    eligibleVoterIds = emptySet(),
                    checkedInVoterIds = emptySet()
                )
            )
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    private fun getCandidateOnChain(
        context: Context,
        electionId: BigInteger,
        candidateId: BigInteger
    ): Result<Pair<String, Int>> {
        return try {
            val function = Function(
                "getCandidate",
                listOf(
                    Uint256(electionId),
                    Uint256(candidateId)
                ),
                listOf(
                    object : TypeReference<Uint256>() {},
                    object : TypeReference<Utf8String>() {},
                    object : TypeReference<Uint256>() {}
                )
            )

            val decodedValues = executeReadonlyFunction(
                context = context,
                function = function
            ).getOrThrow()

            require(decodedValues.size >= 3) {
                "Failed to decode candidate $candidateId for election $electionId."
            }

            val candidateName = decodedValues[1].value as String
            val voteCount = (decodedValues[2].value as BigInteger).toInt()

            Result.success(candidateName to voteCount)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    private fun buildReadonlyChainContext(context: Context): ReadOnlyChainContext {
        val contractConfig = ContractAssets.loadContractConfig(context)
        val adminWalletConfig = ContractAssets.loadAdminWalletConfig(context)
        val credentials = Credentials.create(adminWalletConfig.adminPrivateKey)

        return ReadOnlyChainContext(
            web3j = buildWeb3j(contractConfig.rpcUrl),
            fromAddress = credentials.address,
            contractAddress = contractConfig.contractAddress
        )
    }

    private fun executeReadonlyFunction(
        context: Context,
        function: Function
    ): Result<List<Type<*>>> {
        return try {
            val readOnlyContext = buildReadonlyChainContext(context)
            val encodedFunction = FunctionEncoder.encode(function)

            val response = readOnlyContext.web3j.ethCall(
                Transaction.createEthCallTransaction(
                    readOnlyContext.fromAddress,
                    readOnlyContext.contractAddress,
                    encodedFunction
                ),
                DefaultBlockParameterName.LATEST
            ).send()

            if (response.hasError()) {
                return Result.failure(
                    Exception("Blockchain read failed: ${response.error.message}")
                )
            }

            val decodedValues = FunctionReturnDecoder.decode(
                response.value,
                function.outputParameters
            )

            Result.success(decodedValues)
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
            val function = Function(
                "electionCount",
                emptyList(),
                listOf(object : TypeReference<Uint256>() {})
            )

            val decodedValues = executeReadonlyFunction(
                context = context,
                function = function
            ).getOrThrow()

            if (decodedValues.isEmpty()) {
                Result.failure(Exception("Failed to read electionCount from blockchain."))
            } else {
                Result.success(decodedValues[0].value as BigInteger)
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    private fun normalizeAndValidateWalletAddress(walletAddress: String): String {
        val normalizedAddress = walletAddress.trim()

        require(normalizedAddress.isNotBlank()) {
            "Voter wallet address cannot be blank."
        }

        require(WalletUtils.isValidAddress(normalizedAddress)) {
            "Invalid voter wallet address."
        }

        return normalizedAddress
    }

    private fun loadValidatedVoterSigningWallet(
        context: Context,
        expectedWalletAddress: String
    ): Result<SigningWalletContext> {
        return try {
            val normalizedExpectedAddress = expectedWalletAddress.trim()
            val configuredWallets = ContractAssets.loadVoterWalletConfigs(context)

            val matchingWallet = configuredWallets.firstNotNullOfOrNull { walletConfig ->
                val voterCredentials = Credentials.create(walletConfig.voterPrivateKey)

                if (voterCredentials.address.equals(normalizedExpectedAddress, ignoreCase = true)) {
                    SigningWalletContext(
                        credentials = voterCredentials,
                        privateKey = walletConfig.voterPrivateKey
                    )
                } else {
                    null
                }
            }

            require(matchingWallet != null) {
                "No demo signing wallet is configured for this voter address. Select one of the registered demo voters or add its private key to voter-wallets.json."
            }

            Result.success(matchingWallet)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}