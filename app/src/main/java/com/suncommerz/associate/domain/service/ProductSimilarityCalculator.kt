package com.suncommerz.associate.domain.service

import com.suncommerz.associate.domain.model.Product
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductSimilarityCalculator @Inject constructor() {

    fun findSubstitutes(
        product: Product,
        products: List<Product>,
        acceptedSubstituteIds: Set<String> = emptySet()
    ): List<Product> {
        return products
            .asSequence()
            .filter { it.id != product.id }
            .filter { it.category == product.category }
            .map { candidate ->
                var score = calculateSubstitutionScore(product, candidate)
                if (acceptedSubstituteIds.contains(candidate.id)) {
                    score += 0.5 // Boost score for previously accepted substitutes
                }
                candidate to score
            }
            .filter { (_, score) -> score >= 0.5 }
            .sortedByDescending { (_, score) -> score }
            .map { (candidate, _) -> candidate }
            .toList()
    }

    private fun calculateSubstitutionScore(
        source: Product,
        candidate: Product
    ): Double {
        val nameScore = similarity(source.name, candidate.name)
        val ingredientScore = ingredientSimilarity(
            source.ingredients,
            candidate.ingredients
        )
        val attributeScore = attributeSimilarity(
            source.attributes,
            candidate.attributes
        )
        val typeScore = typeSimilarity(source, candidate)

        return (nameScore * 0.20 +
                ingredientScore * 0.15 +
                attributeScore * 0.25 +
                typeScore * 0.40)
    }

    private fun similarity(
        first: String,
        second: String
    ): Double {
        val firstWords = first
            .lowercase()
            .split(" ")
            .toSet()

        val secondWords = second
            .lowercase()
            .split(" ")
            .toSet()

        if (firstWords.isEmpty() || secondWords.isEmpty()) {
            return 0.0
        }

        val intersection = firstWords.intersect(secondWords).size
        val union = firstWords.union(secondWords).size

        return intersection.toDouble() / union
    }

    private fun ingredientSimilarity(
        first: List<String>,
        second: List<String>
    ): Double {
        val firstIngredients = first
            .map { it.lowercase() }
            .toSet()

        val secondIngredients = second
            .map { it.lowercase() }
            .toSet()

        if (firstIngredients.isEmpty() || secondIngredients.isEmpty()) {
            return 0.0
        }

        val intersection = firstIngredients.intersect(secondIngredients).size

        val union = firstIngredients.union(secondIngredients).size

        return intersection.toDouble() / union
    }

    private fun attributeSimilarity(
        first: Map<String, String>,
        second: Map<String, String>
    ): Double {
        if (first.isEmpty()) {
            return 0.0
        }

        val matchingAttributes = first.count { (key, value) ->
            second[key]?.equals(value, ignoreCase = true) == true
        }

        return matchingAttributes.toDouble() / first.size
    }

    private fun typeSimilarity(
        first: Product,
        second: Product
    ): Double {
        val firstType = first.attributes["type"] ?: return 0.0
        val secondType = second.attributes["type"] ?: return 0.0

        if (firstType == secondType) {
            return 1.0
        }

        val milkTypes = setOf(
            "whole-milk",
            "lactose-free-milk",
            "plant-based-milk"
        )

        val butterTypes = setOf(
            "butter",
            "butter-alternative"
        )

        val coffeeTypes = setOf(
            "ground-coffee"
        )

        val pastaTypes = setOf(
            "regular-pasta",
            "gluten-free-pasta"
        )

        return when (firstType) {
            in milkTypes if secondType in milkTypes -> 0.9
            in butterTypes if secondType in butterTypes -> 0.9
            in coffeeTypes if secondType in coffeeTypes -> 0.9
            in pastaTypes if secondType in pastaTypes -> 0.9
            else -> 0.0
        }
    }
}
