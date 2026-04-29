package com.example.evotingmobileapp.blockchain

import android.content.Context
import java.io.FileNotFoundException
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

data class ContractConfig(
    val contractAddress: String,
    val network: String,
    val rpcUrl: String
)

data class AdminWalletConfig(
    val adminPrivateKey: String
)

data class VoterWalletConfig(
    val voterPrivateKey: String,
    val label: String = "Demo voter"
)

object ContractAssets {

    private val privateKeyRegex = Regex("^0x[0-9a-fA-F]{64}$")

    fun loadContractConfig(context: Context): ContractConfig {
        val jsonObject = readJsonObjectAsset(
            context = context,
            fileName = "contract-info.json"
        )

        val contractAddress = jsonObject.optString("contractAddress").trim()
        val network = jsonObject.optString("network").trim()
        val rpcUrl = jsonObject.optString("rpcUrl").trim()

        require(contractAddress.isNotBlank()) {
            "contractAddress is missing in contract-info.json."
        }

        require(network.isNotBlank()) {
            "network is missing in contract-info.json."
        }

        require(rpcUrl.isNotBlank()) {
            "rpcUrl is missing in contract-info.json."
        }

        return ContractConfig(
            contractAddress = contractAddress,
            network = network,
            rpcUrl = rpcUrl
        )
    }

    fun loadAdminWalletConfig(context: Context): AdminWalletConfig {
        val jsonObject = readJsonObjectAsset(
            context = context,
            fileName = "admin-wallet.json"
        )

        val rawKey = jsonObject.optString("adminPrivateKey").trim()

        return AdminWalletConfig(
            adminPrivateKey = normalizePrivateKey(
                rawKey = rawKey,
                fieldName = "adminPrivateKey",
                fileName = "admin-wallet.json"
            )
        )
    }

    fun loadVoterWalletConfig(context: Context): VoterWalletConfig {
        return loadVoterWalletConfigs(context).first()
    }

    fun loadVoterWalletConfigs(context: Context): List<VoterWalletConfig> {
        val multiWalletJsonText = readOptionalAssetText(
            context = context,
            fileName = "voter-wallets.json"
        )

        if (multiWalletJsonText != null) {
            val jsonObject = try {
                JSONObject(multiWalletJsonText)
            } catch (exception: JSONException) {
                throw IllegalStateException(
                    "voter-wallets.json is not a valid JSON object.",
                    exception
                )
            }

            val walletsArray = jsonObject.optJSONArray("wallets")
                ?: throw IllegalStateException("wallets array is missing in voter-wallets.json.")

            require(walletsArray.length() > 0) {
                "At least one voter wallet must be defined in voter-wallets.json."
            }

            return buildList {
                for (index in 0 until walletsArray.length()) {
                    val walletObject = walletsArray.optJSONObject(index)
                        ?: throw IllegalStateException(
                            "Each wallet in voter-wallets.json must be a JSON object."
                        )

                    val label = walletObject.optString("label").ifBlank {
                        "Demo voter ${index + 1}"
                    }

                    val rawKey = walletObject.optString("privateKey").trim()

                    add(
                        VoterWalletConfig(
                            voterPrivateKey = normalizePrivateKey(
                                rawKey = rawKey,
                                fieldName = "privateKey",
                                fileName = "voter-wallets.json"
                            ),
                            label = label
                        )
                    )
                }
            }
        }

        val jsonObject = readJsonObjectAsset(
            context = context,
            fileName = "voter-wallet.json"
        )

        val rawKey = jsonObject.optString("voterPrivateKey").trim()

        return listOf(
            VoterWalletConfig(
                voterPrivateKey = normalizePrivateKey(
                    rawKey = rawKey,
                    fieldName = "voterPrivateKey",
                    fileName = "voter-wallet.json"
                )
            )
        )
    }

    fun loadAbi(context: Context): JSONArray {
        val jsonText = readAssetText(
            context = context,
            fileName = "evoting-abi.json"
        )

        return try {
            JSONArray(jsonText)
        } catch (exception: JSONException) {
            throw IllegalStateException(
                "evoting-abi.json is not a valid JSON array.",
                exception
            )
        }
    }

    private fun readOptionalAssetText(
        context: Context,
        fileName: String
    ): String? {
        return try {
            context.assets
                .open(fileName)
                .bufferedReader()
                .use { it.readText() }
        } catch (exception: FileNotFoundException) {
            null
        } catch (exception: Exception) {
            throw IllegalStateException(
                "Failed to read $fileName.",
                exception
            )
        }
    }

    private fun readJsonObjectAsset(
        context: Context,
        fileName: String
    ): JSONObject {
        val jsonText = readAssetText(
            context = context,
            fileName = fileName
        )

        return try {
            JSONObject(jsonText)
        } catch (exception: JSONException) {
            throw IllegalStateException(
                "$fileName is not a valid JSON object.",
                exception
            )
        }
    }

    private fun readAssetText(
        context: Context,
        fileName: String
    ): String {
        return try {
            context.assets
                .open(fileName)
                .bufferedReader()
                .use { it.readText() }
        } catch (exception: FileNotFoundException) {
            throw IllegalStateException(
                "$fileName is missing from app/src/main/assets.",
                exception
            )
        } catch (exception: Exception) {
            throw IllegalStateException(
                "Failed to read $fileName.",
                exception
            )
        }
    }

    private fun normalizePrivateKey(
        rawKey: String,
        fieldName: String,
        fileName: String
    ): String {
        require(rawKey.isNotBlank()) {
            "$fieldName is missing in $fileName."
        }

        val normalizedKey = if (rawKey.startsWith("0x")) rawKey else "0x$rawKey"

        require(privateKeyRegex.matches(normalizedKey)) {
            "$fieldName in $fileName must be a valid 64-character hex private key."
        }

        return normalizedKey
    }
}