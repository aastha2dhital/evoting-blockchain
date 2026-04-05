package com.example.evotingmobileapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
            LoginScreen(navController = navController)
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