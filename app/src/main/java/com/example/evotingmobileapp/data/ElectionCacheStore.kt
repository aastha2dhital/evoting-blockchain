package com.example.evotingmobileapp.data

import android.content.Context
import com.example.evotingmobileapp.model.Election
import com.example.evotingmobileapp.model.VoteReceipt
import org.json.JSONArray
import org.json.JSONObject

class ElectionCacheStore(context: Context) {

    private val sharedPreferences =
        context.getSharedPreferences("evoting_cache_store", Context.MODE_PRIVATE)

    private companion object {
        const val KEY_ELECTIONS = "cached_elections"
        const val KEY_VOTE_RECEIPTS = "cached_vote_receipts"
    }

    fun loadElections(): List<Election> {
        return runCatching {
            val rawJson = sharedPreferences.getString(KEY_ELECTIONS, "[]").orEmpty()
            val jsonArray = JSONArray(rawJson)

            buildList {
                for (index in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(index)
                    add(item.toElection())
                }
            }
        }.getOrDefault(emptyList())
    }

    fun saveElections(elections: List<Election>) {
        val jsonArray = JSONArray()

        elections.forEach { election ->
            jsonArray.put(election.toJson())
        }

        sharedPreferences.edit()
            .putString(KEY_ELECTIONS, jsonArray.toString())
            .apply()
    }

    fun loadVoteReceipts(): List<VoteReceipt> {
        return runCatching {
            val rawJson = sharedPreferences.getString(KEY_VOTE_RECEIPTS, "[]").orEmpty()
            val jsonArray = JSONArray(rawJson)

            buildList {
                for (index in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(index)
                    add(item.toVoteReceipt())
                }
            }
        }.getOrDefault(emptyList())
    }

    fun saveVoteReceipts(receipts: List<VoteReceipt>) {
        val jsonArray = JSONArray()

        receipts.forEach { receipt ->
            jsonArray.put(receipt.toJson())
        }

        sharedPreferences.edit()
            .putString(KEY_VOTE_RECEIPTS, jsonArray.toString())
            .apply()
    }

    private fun Election.toJson(): JSONObject {
        val voteCountsJson = JSONObject()
        voteCounts.forEach { (candidate, count) ->
            voteCountsJson.put(candidate, count)
        }

        return JSONObject().apply {
            put("id", id)
            put("title", title)
            put("candidates", JSONArray(candidates))
            put("startTimeMillis", startTimeMillis)
            put("endTimeMillis", endTimeMillis)
            put("isManuallyClosed", isManuallyClosed)
            put("voteCounts", voteCountsJson)
            put("votedVoterIds", JSONArray(votedVoterIds.toList()))
            put("eligibleVoterIds", JSONArray(eligibleVoterIds.toList()))
            put("checkedInVoterIds", JSONArray(checkedInVoterIds.toList()))
        }
    }

    private fun JSONObject.toElection(): Election {
        val candidates = getStringList("candidates")
        val votedVoterIds = getStringSet("votedVoterIds")
        val eligibleVoterIds = getStringSet("eligibleVoterIds")
        val checkedInVoterIds = getStringSet("checkedInVoterIds")

        val voteCountsJson = optJSONObject("voteCounts") ?: JSONObject()
        val voteCounts = linkedMapOf<String, Int>()

        candidates.forEach { candidate ->
            voteCounts[candidate] = voteCountsJson.optInt(candidate, 0)
        }

        return Election(
            id = optString("id"),
            title = optString("title"),
            candidates = candidates,
            startTimeMillis = optLong("startTimeMillis"),
            endTimeMillis = optLong("endTimeMillis"),
            isManuallyClosed = optBoolean("isManuallyClosed", false),
            voteCounts = voteCounts,
            votedVoterIds = votedVoterIds,
            eligibleVoterIds = eligibleVoterIds,
            checkedInVoterIds = checkedInVoterIds
        )
    }

    private fun VoteReceipt.toJson(): JSONObject {
        return JSONObject().apply {
            put("electionId", electionId)
            put("electionTitle", electionTitle)
            put("candidateName", candidateName)
            put("voterId", voterId)
            put("transactionHash", transactionHash)
            put("timestamp", timestamp)
        }
    }

    private fun JSONObject.toVoteReceipt(): VoteReceipt {
        return VoteReceipt(
            electionId = optString("electionId"),
            electionTitle = optString("electionTitle"),
            candidateName = optString("candidateName"),
            voterId = optString("voterId"),
            transactionHash = optString("transactionHash"),
            timestamp = optLong("timestamp")
        )
    }

    private fun JSONObject.getStringList(key: String): List<String> {
        val jsonArray = optJSONArray(key) ?: JSONArray()

        return buildList {
            for (index in 0 until jsonArray.length()) {
                add(jsonArray.optString(index))
            }
        }
    }

    private fun JSONObject.getStringSet(key: String): Set<String> {
        return getStringList(key)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toSet()
    }
}