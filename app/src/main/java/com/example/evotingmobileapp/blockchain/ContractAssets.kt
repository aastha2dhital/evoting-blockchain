package com.example.evotingmobileapp.blockchain

import android.content.Context
import org.json.JSONArray
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
    val voterPrivateKey: String
)

object ContractAssets {

    fun loadContractConfig(context: Context): ContractConfig {
        val jsonText = context.assets
            .open("contract-info.json")
            .bufferedReader()
            .use { it.readText() }

        val jsonObject = JSONObject(jsonText)

        return ContractConfig(
            contractAddress = jsonObject.getString("contractAddress"),
            network = jsonObject.getString("network"),
            rpcUrl = jsonObject.getString("rpcUrl")
        )
    }

    fun loadAdminWalletConfig(context: Context): AdminWalletConfig {
        val jsonText = context.assets
            .open("admin-wallet.json")
            .bufferedReader()
            .use { it.readText() }

        val jsonObject = JSONObject(jsonText)
        val rawKey = jsonObject.getString("adminPrivateKey").trim()

        return AdminWalletConfig(
            adminPrivateKey = normalizePrivateKey(
                rawKey = rawKey,
                fieldName = "adminPrivateKey"
            )
        )
    }

    fun loadVoterWalletConfig(context: Context): VoterWalletConfig {
        val jsonText = context.assets
            .open("voter-wallet.json")
            .bufferedReader()
            .use { it.readText() }

        val jsonObject = JSONObject(jsonText)
        val rawKey = jsonObject.getString("voterPrivateKey").trim()

        return VoterWalletConfig(
            voterPrivateKey = normalizePrivateKey(
                rawKey = rawKey,
                fieldName = "voterPrivateKey"
            )
        )
    }

    fun loadAbi(context: Context): JSONArray {
        val jsonText = context.assets
            .open("evoting-abi.json")
            .bufferedReader()
            .use { it.readText() }

        return JSONArray(jsonText)
    }

    private fun normalizePrivateKey(
        rawKey: String,
        fieldName: String
    ): String {
        require(rawKey.isNotBlank()) {
            "$fieldName is missing."
        }

        return if (rawKey.startsWith("0x")) rawKey else "0x$rawKey"
    }
}