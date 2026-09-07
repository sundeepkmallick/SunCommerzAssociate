package com.suncommerz.associate.domain.repository

import com.suncommerz.associate.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeProducts(): Flow<List<Product>>
    fun getProduct(productId: String): Flow<Product?>
}