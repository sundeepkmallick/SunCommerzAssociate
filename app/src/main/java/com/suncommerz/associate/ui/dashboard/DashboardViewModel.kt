package com.suncommerz.associate.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suncommerz.associate.domain.model.OrderStatus
import com.suncommerz.associate.domain.model.OrderUiModel
import com.suncommerz.associate.domain.usecase.associate.ObserveUserUseCase
import com.suncommerz.associate.domain.usecase.order.ObserveOrdersUseCase
import com.suncommerz.associate.util.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val observeOrdersUseCase: ObserveOrdersUseCase,
    private val observeLoggedInUserUseCase: ObserveUserUseCase
): ViewModel() {

    private val _selectedOrderStatus = MutableStateFlow<OrderStatus?>(null)
    val selectedOrderStatus: StateFlow<OrderStatus?> = _selectedOrderStatus.asStateFlow()

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        getLoggedInUserAndThenOrderList()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getLoggedInUserAndThenOrderList() {
        viewModelScope.launch {
            combine(
                observeLoggedInUserUseCase.invoke(),
                observeOrdersUseCase.invoke(),
                _selectedOrderStatus
            ){  loggedInUser, orderList, selectedOrderStatus ->

                if (loggedInUser == null) {
                    DashboardUiState.Error("Store associate is not loggedin!")
                } else {
                    val filteredOrdersByOrderStatus = orderList.filter {
                        selectedOrderStatus == null || it.status == selectedOrderStatus
                    }

                    val uiOrderList = filteredOrdersByOrderStatus.map { order ->
                        OrderUiModel(
                            order = order,
                            orderDateTimeFormatted = DateTimeUtils.formatInstantToDateTime(order.orderDateTime)
                        )
                    }
                    DashboardUiState.Loaded(
                        loggedInUser = loggedInUser,
                        orderList = uiOrderList
                    )
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onStatusFilterChanged(orderStatus: OrderStatus?) {
        _selectedOrderStatus.value = orderStatus
    }
}