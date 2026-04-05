package com.example.evotingmobileapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.evotingmobileapp.admin.AdminViewModel
import com.example.evotingmobileapp.admin.AdminViewModelFactory
import com.example.evotingmobileapp.data.InMemoryElectionRepository
import com.example.evotingmobileapp.navigation.AppNavGraph
import com.example.evotingmobileapp.ui.theme.EVotingMobileAppTheme
import org.conscrypt.Conscrypt
import java.security.Security

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Security.insertProviderAt(Conscrypt.newProvider(), 1)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EVotingMobileAppTheme {
                val navController = rememberNavController()

                val repository = remember { InMemoryElectionRepository() }
                val adminViewModelFactory = remember { AdminViewModelFactory(repository) }
                val adminViewModel: AdminViewModel = viewModel(factory = adminViewModelFactory)

                AppNavGraph(
                    navController = navController,
                    adminViewModel = adminViewModel
                )
            }
        }
    }
}