package com.suncommerz.associate.ui.aipickup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.suncommerz.associate.R

@Composable
fun AIPickUpScreen(
    viewModel: AIPickUpViewModel = hiltViewModel(),
    onBackPressed: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is AIPickUpUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is AIPickUpUiState.Processing -> {
                AIPickUpProcessingContent(state, onBackPressed)
            }
            is AIPickUpUiState.ProposalReady -> {
                AIPickUpProposalContent(state, viewModel, onBackPressed)
            }
            is AIPickUpUiState.Error -> {
                AIPickUpErrorContent(state, { viewModel.startAiPickup(state.orderId) }, onBackPressed)
            }
        }
    }
}

@Composable
fun AIPickUpProcessingContent(state: AIPickUpUiState.Processing, onBackPressed: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = state.currentStatus, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(24.dp))
        TextButton(onClick = onBackPressed) {
            Text(text = "Cancel")
        }
    }
}

@Composable
fun AIPickUpProposalContent(state: AIPickUpUiState.ProposalReady, viewModel: AIPickUpViewModel, onBackPressed: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "AI Pickup Proposal", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(state.proposals) { proposal ->
                ProposalItem(proposal)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        if (state.isUpdating) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else {
            Button(
                onClick = { viewModel.approveProposals() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Approve & Update Order")
            }
        }
        
        TextButton(
            onClick = onBackPressed,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isUpdating
        ) {
            Text(text = "Back")
        }
    }
}

@Composable
fun ProposalItem(proposal: AIPickUpItemProposal) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = proposal.productName, style = MaterialTheme.typography.titleMedium)
                Text(text = "Action: ${proposal.proposal}", style = MaterialTheme.typography.bodySmall)
            }
            if (proposal.proposal == ProposalType.SUBSTITUTE || proposal.proposal == ProposalType.PICK_FROM_OTHER_STORE) {
                Text(text = proposal.detail, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun AIPickUpErrorContent(state: AIPickUpUiState.Error, onRetry: () -> Unit, onBackPressed: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = state.message, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(text = "Retry")
        }
        TextButton(onClick = onBackPressed) {
            Text(text = "Back")
        }
    }
}
