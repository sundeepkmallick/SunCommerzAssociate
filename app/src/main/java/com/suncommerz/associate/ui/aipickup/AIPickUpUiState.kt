package com.suncommerz.associate.ui.aipickup

sealed class AIPickUpUiState {
    data object Loading : AIPickUpUiState()
    data class Error(val message: String, val orderId: String) : AIPickUpUiState()
    data class Processing(
        val currentStatus: String,
        val processedItems: List<AIPickUpItemProposal> = emptyList()
    ) : AIPickUpUiState()
    data class ProposalReady(
        val proposals: List<AIPickUpItemProposal>,
        val isUpdating: Boolean = false
    ) : AIPickUpUiState()
}

data class AIPickUpItemProposal(
    val orderItemId: String,
    val productName: String,
    val proposal: ProposalType,
    val detail: String // e.g. storeId or substituteProductId
)

enum class ProposalType {
    PICKED,
    PICK_FROM_OTHER_STORE,
    SUBSTITUTE,
    UNAVAILABLE
}
