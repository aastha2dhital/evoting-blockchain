package com.example.evotingmobileapp.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.evotingmobileapp.admin.AdminViewModel
import com.example.evotingmobileapp.admin.CreateElectionScreen
import com.example.evotingmobileapp.screens.BlockchainScreen
import com.example.evotingmobileapp.screens.DashboardScreen
import com.example.evotingmobileapp.screens.LoginScreen
import com.example.evotingmobileapp.screens.ResultsScreen
import com.example.evotingmobileapp.screens.VotingScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {

        composable(Screen.Login.route) {
            LoginScreen(
                walletConnected = true,
                walletAddress = "0xA1B2C3D4E5F6",
                onConnectWallet = {},
                onLoginAsVoter = {
                    navController.navigate(Screen.Dashboard.route)
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }

        composable(Screen.Voting.route) {
            VotingScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onSubmitVote = {
                    navController.navigate(Screen.Results.route)
                }
            )
        }

        composable(Screen.Results.route) {
            ResultsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Blockchain.route) {
            BlockchainScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.CreateElection.route) {
            val adminViewModel: AdminViewModel = viewModel()

            CreateElectionScreen(
                adminViewModel = adminViewModel
            )
        }
    }
}