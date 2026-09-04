package com.suncommerz.associate.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suncommerz.associate.domain.model.StoreAssociate
import com.suncommerz.associate.domain.usecase.associate.ObserveUserUseCase
import com.suncommerz.associate.domain.usecase.order.ObserveOrdersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val observeOrdersUseCase: ObserveOrdersUseCase,
    private val observeLoggedInUserUseCase: ObserveUserUseCase
): ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        getLoggedInUserAndThenOrderList()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getLoggedInUserAndThenOrderList() {
        viewModelScope.launch {
            observeLoggedInUserUseCase.invoke().flatMapLatest { user ->
                if(user == null) {
                    flowOf(
                        DashboardUiState.Error("Store associate is not loggedin!")
                    )
                } else {
                    observeOrdersUseCase.invoke().map { orders ->
                        DashboardUiState.Loaded(
                            loggedInUser = user,
                            orderList = orders
                        )
                    }
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}