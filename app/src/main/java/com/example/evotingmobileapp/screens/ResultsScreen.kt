package com.example.evotingmobileapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.evotingmobileapp.admin.AdminViewModel
import com.example.evotingmobileapp.model.Election
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ResultsScreen(
    navController: NavHostController? = null,
    adminViewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val elections by adminViewModel.elections.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var closingElectionId by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SnackbarHost(hostState = snackbarHostState)

        Text(
            text = "Results",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Results are shown only after the election closes. Admins can also close an election early from here.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
        )

        if (elections.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "No elections created yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
            return
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(elections) { election ->
                ElectionResultCard(
                    election = election,
                    isClosing = closingElectionId == election.id,
                    onCloseElectionEarly = {
                        if (closingElectionId != null) {
                            return@ElectionResultCard
                        }

                        closingElectionId = election.id

                        coroutineScope.launch {
                            val result = withContext(Dispatchers.IO) {
                                adminViewModel.closeElectionEarly(election.id)
                            }

                            closingElectionId = null

                            val message = result.fold(
                                onSuccess = { successMessage ->
                                    successMessage
                                },
                                onFailure = { exception ->
                                    exception.message ?: "Failed to close election early."
                                }
                            )

                            snackbarHostState.showSnackbar(message)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ElectionResultCard(
    election: Election,
    isClosing: Boolean,
    onCloseElectionEarly: () -> Unit
) {
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val totalVotes = election.voteCounts.values.sum()

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = election.title,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Start: ${formatter.format(Date(election.startTimeMillis))}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "End: ${formatter.format(Date(election.endTimeMillis))}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Status: ${if (election.isClosed()) "Closed" else "Open"}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
            )

            if (!election.isClosed()) {
                Text(
                    text = "Results are locked until this election closes.",
                    style = MaterialTheme.typography.bodyLarge
                )

                OutlinedButton(
                    onClick = onCloseElectionEarly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    enabled = !isClosing
                ) {
                    Text(
                        text = if (isClosing) {
                            "Closing Election..."
                        } else {
                            "Close Election Early"
                        }
                    )
                }

                return@Column
            }

            Text(
                text = "Total Votes: $totalVotes",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            election.candidates.forEachIndexed { index, candidate ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Text(
                        text = candidate,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "${election.voteCounts[candidate] ?: 0} votes",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                if (index != election.candidates.lastIndex) {
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}