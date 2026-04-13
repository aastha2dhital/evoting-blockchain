package com.example.evotingmobileapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.evotingmobileapp.admin.AdminViewModel
import com.example.evotingmobileapp.admin.CreateElectionScreen
import com.example.evotingmobileapp.blockchain.BlockchainRecordsScreen
import com.example.evotingmobileapp.qr.QRCheckInScreen
import com.example.evotingmobileapp.receipt.ReceiptScreen
import com.example.evotingmobileapp.screens.DashboardScreen
import com.example.evotingmobileapp.screens.LoginScreen
import com.example.evotingmobileapp.screens.ResultsScreen
import com.example.evotingmobileapp.screens.VotingScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    adminViewModel: AdminViewModel
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.LOGIN
    ) {
        composable(AppRoutes.LOGIN) {
            LoginScreen(
                navController = navController,
                adminViewModel = adminViewModel
            )
        }

        composable(AppRoutes.DASHBOARD) {
            DashboardScreen(
                navController = navController,
                adminViewModel = adminViewModel
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

        composable(AppRoutes.RECEIPT) {
            ReceiptScreen(
                navController = navController,
                adminViewModel = adminViewModel
            )
        }

        composable(
            route = AppRoutes.RECEIPT_WITH_TX_HASH,
            arguments = listOf(
                navArgument(AppRoutes.RECEIPT_TX_HASH_ARG) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            ReceiptScreen(
                navController = navController,
                adminViewModel = adminViewModel,
                initialTransactionHash = backStackEntry.arguments
                    ?.getString(AppRoutes.RECEIPT_TX_HASH_ARG)
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
                adminViewModel = adminViewModel
            )
        }
    }
}