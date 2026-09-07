package com.suncommerz.associate.data.local

import com.suncommerz.associate.data.dto.InventoryRecordDto
import com.suncommerz.associate.data.dto.OrderDto
import com.suncommerz.associate.data.dto.ProductDto
import com.suncommerz.associate.data.dto.StoreAssociateDto
import com.suncommerz.associate.data.dto.StoreDto
import com.suncommerz.associate.data.dto.SubstitutionHistoryDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeBackendApiResponse @Inject constructor() {
    private val _stores = MutableStateFlow(FakeBackendData.stores)
    val stores: StateFlow<List<StoreDto>> = _stores.asStateFlow()

    private val _products = MutableStateFlow(FakeBackendData.products)
    val products: StateFlow<List<ProductDto>> = _products.asStateFlow()

    private val _inventoryRecords = MutableStateFlow(FakeBackendData.inventoryRecords)
    val inventoryRecords: StateFlow<List<InventoryRecordDto>> = _inventoryRecords.asStateFlow()

    private val _orders = MutableStateFlow(FakeBackendData.orders)
    val orders: StateFlow<List<OrderDto>> = _orders.asStateFlow()

    private val _substitutionHistory = MutableStateFlow(FakeBackendData.substitutionHistory)
    val substitutionHistory: StateFlow<List<SubstitutionHistoryDto>> = _substitutionHistory.asStateFlow()

    private val _loggedInAssociates = MutableStateFlow(FakeBackendData.loggedInAssociate)
    val loggedInAssociates: StateFlow<StoreAssociateDto> = _loggedInAssociates.asStateFlow()

    fun updateOrders(
        orders: List<OrderDto>
    ) {
        _orders.value = orders
    }

    fun updateInventory(
        inventory: List<InventoryRecordDto>
    ) {
        _inventoryRecords.value = inventory
    }
}