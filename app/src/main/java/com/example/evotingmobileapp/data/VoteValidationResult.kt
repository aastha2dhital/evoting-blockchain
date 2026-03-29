package com.example.evotingmobileapp.data

import com.example.evotingmobileapp.model.VoteReceipt

data class VoteValidationResult(
    val success: Boolean,
    val message: String,
    val receipt: VoteReceipt? = null
)