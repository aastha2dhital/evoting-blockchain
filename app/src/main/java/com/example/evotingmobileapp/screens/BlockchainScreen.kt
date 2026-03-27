package com.example.evotingmobileapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evotingmobileapp.blockchain.BlockchainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockchainScreen(
    onBack: () -> Unit
) {
    val viewModel: BlockchainViewModel = viewModel()

    val latestBlockState = viewModel.latestBlock.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Blockchain") }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = latestBlockState.value,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.loadLatestBlock() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Load Blockchain Data")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back")
            }
        }
    }
}