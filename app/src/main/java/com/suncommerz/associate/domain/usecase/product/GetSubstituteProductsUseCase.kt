package com.suncommerz.associate.domain.usecase.product

import com.suncommerz.associate.domain.model.Product
import com.suncommerz.associate.domain.repository.ProductRepository
import javax.inject.Inject

class GetSubstituteProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(
        productId: String
    ): List<Product> {
        return productRepository.getSubstituteProducts(
            productId = productId
        )
    }
}