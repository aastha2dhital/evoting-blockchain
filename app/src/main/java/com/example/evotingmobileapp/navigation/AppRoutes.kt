package com.example.evotingmobileapp.navigation

import android.net.Uri

object AppRoutes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"

    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val VOTER_DASHBOARD = "voter_dashboard"

    const val CREATE_ELECTION = "create_election"
    const val QR_CHECK_IN = "qr_check_in"
    const val VOTING = "voting"

    const val RECEIPT = "receipt"
    const val RECEIPT_TX_HASH_ARG = "transactionHash"
    const val RECEIPT_WITH_TX_HASH = "$RECEIPT/{$RECEIPT_TX_HASH_ARG}"

    const val RESULTS = "results"
    const val BLOCKCHAIN_RECORDS = "blockchain_records"

    fun receiptRoute(transactionHash: String): String {
        return "$RECEIPT/${Uri.encode(transactionHash)}"
    }
}