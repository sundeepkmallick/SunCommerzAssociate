package com.suncommerz.associate.domain.usecase.product

import com.suncommerz.associate.domain.model.Product
import com.suncommerz.associate.domain.repository.OrderRepository
import com.suncommerz.associate.domain.repository.ProductRepository
import com.suncommerz.associate.domain.service.ProductSimilarityCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetSubstituteProductsUseCase @Inject constructor(
    private val repository: ProductRepository,
    private val orderRepository: OrderRepository,
    private val similarityCalculator: ProductSimilarityCalculator
) {
    operator fun invoke(productId: String): Flow<List<Product?>?> {
        return combine(
            repository.observeProducts(),
            repository.getProduct(productId),
            orderRepository.pastOrderWithSubstitutionHistory(productId)
        ) { products, orderedProduct, history ->
            if (orderedProduct == null) {
                null
            } else {
                val acceptedIds = history
                    .filter { it.accepted }
                    .map { it.substituteProductId }
                    .toSet()

                similarityCalculator.findSubstitutes(
                    product = orderedProduct,
                    products = products,
                    acceptedSubstituteIds = acceptedIds
                )
            }
        }
    }
}