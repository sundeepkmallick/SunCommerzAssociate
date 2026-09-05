package com.suncommerz.associate.ui.dashboard

import com.suncommerz.associate.domain.model.OrderUiModel
import com.suncommerz.associate.domain.model.StoreAssociate

sealed class DashboardUiState{
    data object Loading: DashboardUiState()
    data class Error(val message: String): DashboardUiState()
    data class Loaded(val loggedInUser: StoreAssociate, val orderList: List<OrderUiModel>): DashboardUiState()
}