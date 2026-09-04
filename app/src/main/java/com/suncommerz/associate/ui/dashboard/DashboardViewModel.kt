package com.suncommerz.associate.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suncommerz.associate.domain.model.StoreAssociate
import com.suncommerz.associate.domain.usecase.associate.ObserveUserUseCase
import com.suncommerz.associate.domain.usecase.order.ObserveOrdersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    observeOrdersUseCase: ObserveOrdersUseCase,
    private val observeLoggedInUserUseCase: ObserveUserUseCase
): ViewModel() {
    val orderList = observeOrdersUseCase.invoke().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    private val _loggedInUser = MutableStateFlow<StoreAssociate?>(null)
    val loggedInUser = _loggedInUser.asStateFlow()

    init {
        getLoggedInUser()
    }

    private fun getLoggedInUser() {
        viewModelScope.launch {
            observeLoggedInUserUseCase.invoke().collectLatest { associate ->
                _loggedInUser.value = associate
            }
        }
    }
}