package com.example.evotingmobileapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.evotingmobileapp.navigation.AppNavGraph
import com.example.evotingmobileapp.ui.theme.EVotingMobileAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EVotingMobileAppTheme {
                val navController = rememberNavController()

                AppNavGraph(
                    navController = navController
                )
            }
        }
    }
}