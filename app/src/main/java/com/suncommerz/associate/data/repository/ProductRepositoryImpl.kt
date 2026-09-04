package com.suncommerz.associate.data.repository

import com.suncommerz.associate.data.dto.ProductDto
import com.suncommerz.associate.data.local.FakeBackendApiResponse
import com.suncommerz.associate.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(private val api: FakeBackendApiResponse): ProductRepository {
    override fun observeProducts(): Flow<List<ProductDto>> {
        return api.products
    }

    override suspend fun getProduct(productId: String): Flow<ProductDto?> {
        return api.products.map { products ->
            products.find { product ->
                product.id == productId
            }
        }
    }

}