package com.example.evotingmobileapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.evotingmobileapp.admin.AdminViewModel
import com.example.evotingmobileapp.admin.CreateElectionScreen
import com.example.evotingmobileapp.auth.AuthSessionViewModel
import com.example.evotingmobileapp.blockchain.BlockchainRecordsScreen
import com.example.evotingmobileapp.qr.QRCheckInScreen
import com.example.evotingmobileapp.receipt.ReceiptScreen
import com.example.evotingmobileapp.screens.AdminLoginScreen
import com.example.evotingmobileapp.screens.DashboardScreen
import com.example.evotingmobileapp.screens.LoginScreen
import com.example.evotingmobileapp.screens.ResultsScreen
import com.example.evotingmobileapp.screens.VoterAccessScreen
import com.example.evotingmobileapp.screens.VotingScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    adminViewModel: AdminViewModel,
    authSessionViewModel: AuthSessionViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.LOGIN
    ) {
        composable(AppRoutes.LOGIN) {
            LoginScreen(
                navController = navController,
                authSessionViewModel = authSessionViewModel
            )
        }

        composable(AppRoutes.ADMIN_LOGIN) {
            AdminLoginScreen(
                navController = navController,
                authSessionViewModel = authSessionViewModel
            )
        }

        composable(AppRoutes.VOTER_ACCESS) {
            VoterAccessScreen(
                navController = navController,
                authSessionViewModel = authSessionViewModel
            )
        }

        composable(AppRoutes.DASHBOARD) {
            RequireAdminAccess(
                navController = navController,
                authSessionViewModel = authSessionViewModel
            ) {
                DashboardScreen(
                    navController = navController,
                    adminViewModel = adminViewModel,
                    authSessionViewModel = authSessionViewModel
                )
            }
        }

        composable(AppRoutes.ADMIN_DASHBOARD) {
            RequireAdminAccess(
                navController = navController,
                authSessionViewModel = authSessionViewModel
            ) {
                DashboardScreen(
                    navController = navController,
                    adminViewModel = adminViewModel,
                    authSessionViewModel = authSessionViewModel
                )
            }
        }

        composable(AppRoutes.VOTER_DASHBOARD) {
            RequireVoterAccess(
                navController = navController,
                authSessionViewModel = authSessionViewModel
            ) {
                DashboardScreen(
                    navController = navController,
                    adminViewModel = adminViewModel,
                    authSessionViewModel = authSessionViewModel
                )
            }
        }

        composable(AppRoutes.CREATE_ELECTION) {
            RequireAdminAccess(
                navController = navController,
                authSessionViewModel = authSessionViewModel
            ) {
                CreateElectionScreen(
                    navController = navController,
                    adminViewModel = adminViewModel
                )
            }
        }

        composable(AppRoutes.QR_CHECK_IN) {
            RequireAdminAccess(
                navController = navController,
                authSessionViewModel = authSessionViewModel
            ) {
                QRCheckInScreen(
                    navController = navController,
                    adminViewModel = adminViewModel
                )
            }
        }

        composable(AppRoutes.VOTING) {
            RequireVoterAccess(
                navController = navController,
                authSessionViewModel = authSessionViewModel
            ) {
                VotingScreen(
                    navController = navController,
                    adminViewModel = adminViewModel
                )
            }
        }

        composable(AppRoutes.RECEIPT) {
            RequireVoterAccess(
                navController = navController,
                authSessionViewModel = authSessionViewModel
            ) {
                ReceiptScreen(
                    navController = navController,
                    adminViewModel = adminViewModel
                )
            }
        }

        composable(
            route = AppRoutes.RECEIPT_WITH_TX_HASH,
            arguments = listOf(
                navArgument(AppRoutes.RECEIPT_TX_HASH_ARG) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            RequireVoterAccess(
                navController = navController,
                authSessionViewModel = authSessionViewModel
            ) {
                ReceiptScreen(
                    navController = navController,
                    adminViewModel = adminViewModel,
                    initialTransactionHash = backStackEntry.arguments
                        ?.getString(AppRoutes.RECEIPT_TX_HASH_ARG)
                )
            }
        }

        composable(AppRoutes.RESULTS) {
            RequireAdminAccess(
                navController = navController,
                authSessionViewModel = authSessionViewModel
            ) {
                ResultsScreen(
                    navController = navController,
                    adminViewModel = adminViewModel
                )
            }
        }

        composable(AppRoutes.BLOCKCHAIN_RECORDS) {
            RequireAdminAccess(
                navController = navController,
                authSessionViewModel = authSessionViewModel
            ) {
                BlockchainRecordsScreen(
                    adminViewModel = adminViewModel
                )
            }
        }
    }
}

@Composable
private fun RequireAdminAccess(
    navController: NavHostController,
    authSessionViewModel: AuthSessionViewModel,
    content: @Composable () -> Unit
) {
    val authUiState by authSessionViewModel.uiState.collectAsState()

    LaunchedEffect(authUiState) {
        if (!authUiState.canAccessAdmin()) {
            navController.navigate(AppRoutes.LOGIN) {
                popUpTo(AppRoutes.LOGIN) { inclusive = true }
            }
        }
    }

    if (authUiState.canAccessAdmin()) {
        content()
    }
}

@Composable
private fun RequireVoterAccess(
    navController: NavHostController,
    authSessionViewModel: AuthSessionViewModel,
    content: @Composable () -> Unit
) {
    val authUiState by authSessionViewModel.uiState.collectAsState()

    LaunchedEffect(authUiState) {
        if (!authUiState.canAccessVoter()) {
            navController.navigate(AppRoutes.LOGIN) {
                popUpTo(AppRoutes.LOGIN) { inclusive = true }
            }
        }
    }

    if (authUiState.canAccessVoter()) {
        content()
    }
}