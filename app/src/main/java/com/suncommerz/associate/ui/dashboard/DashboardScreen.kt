package com.suncommerz.associate.ui.dashboard

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OrderList(uiState)
}

@Composable
fun OrderList(uiState: DashboardUiState) {

    when(uiState) {
        DashboardUiState.Loading -> {

        }

        is DashboardUiState.Error -> {

        }

        is DashboardUiState.Loaded -> {

        }
    }
}