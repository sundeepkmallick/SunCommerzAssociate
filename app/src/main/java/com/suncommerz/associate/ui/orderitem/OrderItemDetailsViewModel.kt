package com.suncommerz.associate.ui.orderitem

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OrderItemDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
): ViewModel() {
    private val orderId: String = checkNotNull(savedStateHandle["orderId"])
    private val orderItemId: String = checkNotNull(savedStateHandle["orderItemId"])
}