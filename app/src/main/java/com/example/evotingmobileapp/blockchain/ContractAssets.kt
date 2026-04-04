package com.example.evotingmobileapp.blockchain

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class ContractConfig(
    val contractAddress: String,
    val network: String,
    val rpcUrl: String
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

    fun loadAbi(context: Context): JSONArray {
        val jsonText = context.assets
            .open("evoting-abi.json")
            .bufferedReader()
            .use { it.readText() }

        return JSONArray(jsonText)
    }
}