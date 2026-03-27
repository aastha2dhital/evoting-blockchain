package com.example.evotingmobileapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.evotingmobileapp.navigation.Screen

@Composable
fun DashboardScreen(navController: NavController) {
    val screenBackground = Color(0xFFF3F2F7)
    val cardBackground = Color(0xFFE8E6ED)
    val primaryColor = Color(0xFF4C63A6)
    val secondaryColor = Color(0xFF6A8F63)
    val darkText = Color(0xFF1F1F28)
    val subtitleText = Color(0xFF5A5A66)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBackground)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = darkText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Manage elections, vote securely, and verify blockchain activity.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = subtitleText
                )

                Spacer(modifier = Modifier.height(28.dp))

                DashboardButton(
                    text = "Create Election",
                    containerColor = primaryColor,
                    onClick = {
                        navController.navigate(Screen.CreateElection.route)
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                DashboardButton(
                    text = "Go to Voting",
                    containerColor = secondaryColor,
                    onClick = {
                        navController.navigate(Screen.Voting.route)
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                DashboardButton(
                    text = "View Results",
                    containerColor = primaryColor,
                    onClick = {
                        navController.navigate(Screen.Results.route)
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                DashboardButton(
                    text = "Blockchain Records",
                    containerColor = secondaryColor,
                    onClick = {
                        navController.navigate(Screen.Blockchain.route)
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Login.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB05A5A),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Logout",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardButton(
    text: String,
    containerColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}