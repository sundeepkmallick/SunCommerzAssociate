package com.suncommerz.associate.data.repository

import com.suncommerz.associate.data.local.FakeBackendApiResponse
import com.suncommerz.associate.data.mapper.toDomain
import com.suncommerz.associate.domain.model.Product
import com.suncommerz.associate.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(private val api: FakeBackendApiResponse): ProductRepository {
    override fun observeProducts(): Flow<List<Product>> {
        return api.products.map { productDtos ->
            productDtos.map { productDto ->
                productDto.toDomain()
            }
        }
    }

    override suspend fun getProduct(productId: String): Flow<Product?> {
        return api.products.map { products ->
            products.find { product ->
                product.id == productId
            }?.toDomain()
        }
    }

}