package com.example.evotingmobileapp.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Voting : Screen("voting")
    object Results : Screen("results")
    object Blockchain : Screen("blockchain")
}