package com.suncommerz.associate.domain.usecase.fullfilment

import com.suncommerz.associate.domain.model.Product
import com.suncommerz.associate.domain.model.Store

/**
 * Contains every possible fulfillment path
 * for an unavailable order item.
 *
 * Priority:
 *
 * 1. Substitute available in current store
 * 2. Original product available in nearby store
 * 3. Substitute available in nearby store
 */
data class FulfillmentOptions(
    val substitutesInCurrentStore: List<Product>,
    val originalProductNearbyStores: List<Store>,
    val substitutesInNearbyStores: Map<Product, List<Store>>
)