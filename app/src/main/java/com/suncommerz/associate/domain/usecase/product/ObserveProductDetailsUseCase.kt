package com.suncommerz.associate.domain.usecase.product

import com.suncommerz.associate.domain.model.Product
import com.suncommerz.associate.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveProductDetailsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    operator fun invoke(
        productId: String
    ): Flow<Product?> {
        return productRepository.getProduct(
            productId = productId
        )
    }
}