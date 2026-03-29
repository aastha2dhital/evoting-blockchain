package com.example.evotingmobileapp.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.evotingmobileapp.admin.AdminViewModel
import com.example.evotingmobileapp.admin.CreateElectionScreen
import com.example.evotingmobileapp.qr.QRCheckInScreen
import com.example.evotingmobileapp.receipt.ReceiptScreen
import com.example.evotingmobileapp.screens.BlockchainRecordsScreen
import com.example.evotingmobileapp.screens.DashboardScreen
import com.example.evotingmobileapp.screens.LoginScreen
import com.example.evotingmobileapp.screens.ResultsScreen
import com.example.evotingmobileapp.screens.VotingScreen

object AppRoutes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val CREATE_ELECTION = "create_election"
    const val QR_CHECK_IN = "qr_check_in"
    const val VOTING = "voting"
    const val RESULTS = "results"
    const val BLOCKCHAIN_RECORDS = "blockchain_records"
    const val RECEIPT = "receipt"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    adminViewModel: AdminViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.LOGIN
    ) {
        composable(AppRoutes.LOGIN) {
            LoginScreen(
                navController = navController
            )
        }

        composable(AppRoutes.DASHBOARD) {
            DashboardScreen(
                navController = navController
            )
        }

        composable(AppRoutes.CREATE_ELECTION) {
            CreateElectionScreen(
                navController = navController,
                adminViewModel = adminViewModel
            )
        }

        composable(AppRoutes.QR_CHECK_IN) {
            QRCheckInScreen(
                navController = navController,
                adminViewModel = adminViewModel
            )
        }

        composable(AppRoutes.VOTING) {
            VotingScreen(
                navController = navController,
                adminViewModel = adminViewModel
            )
        }

        composable(AppRoutes.RESULTS) {
            ResultsScreen(
                navController = navController,
                adminViewModel = adminViewModel
            )
        }

        composable(AppRoutes.BLOCKCHAIN_RECORDS) {
            BlockchainRecordsScreen(
                navController = navController
            )
        }

        composable(AppRoutes.RECEIPT) {
            ReceiptScreen(
                navController = navController,
                adminViewModel = adminViewModel
            )
        }
    }
}