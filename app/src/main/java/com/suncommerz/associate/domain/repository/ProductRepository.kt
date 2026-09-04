package com.suncommerz.associate.domain.repository

import com.suncommerz.associate.data.dto.ProductDto
import com.suncommerz.associate.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeProducts(): Flow<List<ProductDto>>
    suspend fun getProduct(productId: String): Flow<ProductDto?>
}