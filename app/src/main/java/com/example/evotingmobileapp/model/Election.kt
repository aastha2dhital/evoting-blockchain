package com.example.evotingmobileapp.model

data class Election(
    val id: String,
    val title: String,
    val candidates: List<String>,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val isManuallyClosed: Boolean = false,

    val voteCounts: Map<String, Int> = emptyMap(),
    val votedVoterIds: Set<String> = emptySet(),

    val eligibleVoterIds: Set<String> = emptySet(),
    val checkedInVoterIds: Set<String> = emptySet()
) {

    fun isActive(currentTimeMillis: Long = System.currentTimeMillis()): Boolean {
        return !isManuallyClosed &&
                currentTimeMillis >= startTimeMillis &&
                currentTimeMillis <= endTimeMillis
    }

    fun isClosed(currentTimeMillis: Long = System.currentTimeMillis()): Boolean {
        return isManuallyClosed || currentTimeMillis > endTimeMillis
    }

    fun hasStarted(currentTimeMillis: Long = System.currentTimeMillis()): Boolean {
        return currentTimeMillis >= startTimeMillis
    }

    fun isEligible(voterId: String): Boolean {
        return voterId in eligibleVoterIds
    }

    fun isCheckedIn(voterId: String): Boolean {
        return voterId in checkedInVoterIds
    }
}