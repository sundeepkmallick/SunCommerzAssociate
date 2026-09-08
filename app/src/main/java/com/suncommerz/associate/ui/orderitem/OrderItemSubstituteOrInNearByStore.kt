package com.suncommerz.associate.ui.orderitem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayoutScope
import androidx.constraintlayout.compose.Dimension
import com.suncommerz.associate.domain.model.OrderItemAvailabilityStatus

@Composable
fun ConstraintLayoutScope.OrderItemProductSubstituteOrInNearByStore(
    layoutProductAlternative: ConstrainedLayoutReference,
    layoutProductItemInfo: ConstrainedLayoutReference,
    uiState: OrderItemDetailsUiState.Loaded,
    onSubstituteProductItemSelected: (String, Int) -> Unit,
    orderItemInNearByStoreReserved: (String, Int) -> Unit
) {

    Column(
        modifier = Modifier
            .constrainAs(layoutProductAlternative) {
                top.linkTo(layoutProductItemInfo.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
                height = Dimension.wrapContent
            }
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        if (uiState.orderItemAvailabilityInStoreUiModel.orderItemAvailabilityStatus
            == OrderItemAvailabilityStatus.SUBSTITUTE_AVAILABLE
        ) {
            val substituteProducts = uiState.fulfillmentOptions?.substitutesInCurrentStore
            if (substituteProducts?.isNotEmpty() == true) {
                LazyColumn(modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()) {
                    items(substituteProducts) { substituteProduct ->
                        ListItemSubstituteItem(
                            substituteProduct = substituteProduct,
                            selectedSubstituteId = uiState.orderItem.selectedSubstituteId,
                            onSubstituteProductItemSelected = { onSubstituteProductItemSelected(substituteProduct.id, uiState.orderItem.requestedQuantity) })
                    }
                }
            }
        } else if (uiState.orderItemAvailabilityInStoreUiModel.orderItemAvailabilityStatus
            == OrderItemAvailabilityStatus.AVAILABLE_IN_NEARBY_STORE
        ) {
            val nearByStores = uiState.fulfillmentOptions?.originalProductNearbyStores
            if (nearByStores?.isNotEmpty() == true) {
                LazyColumn(modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()) {
                    items(nearByStores) { nearByStore ->
                        ListItemNearByStoresWhereOrderItemAvailable(
                            nearByStore = nearByStore,
                            reservedStoreId = uiState.orderItem.reservedStoreId,
                            orderItemInNearByStoreReserved = { orderItemInNearByStoreReserved(nearByStore.id, uiState.orderItem.requestedQuantity) })
                    }
                }
            }
        }
    }
}