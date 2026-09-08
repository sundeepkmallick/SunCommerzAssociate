package com.suncommerz.associate.ui.orderitem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayoutScope
import com.suncommerz.associate.R
import com.suncommerz.associate.domain.model.OrderItemAvailabilityStatus

@Composable
fun ConstraintLayoutScope.OrderItemProductInfo(
    layoutProductItemInfo: ConstrainedLayoutReference,
    uiState: OrderItemDetailsUiState.Loaded
) {
    val orderItem = uiState.orderItem
    val orderItemAvailabilityInStoreUiModel = uiState.orderItemAvailabilityInStoreUiModel
    val fulfillmentOptions = uiState.fulfillmentOptions
    Column(
        modifier = Modifier
            .constrainAs(layoutProductItemInfo) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
            .wrapContentHeight()
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer
            )
            .padding(dimensionResource(R.dimen.padding)),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(R.string.label_item_id, orderItem.id),
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = stringResource(R.string.label_item_order_id, orderItem.orderId),
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = stringResource(R.string.label_item_name, orderItem.product.name),
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = stringResource(
                R.string.label_item_price,
                orderItem.product.price,
                orderItem.product.currency
            ),
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = stringResource(R.string.label_item_category, orderItem.product.category),
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = stringResource(
                R.string.label_item_category_description,
                orderItem.product.description
            ),
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = stringResource(
                R.string.label_item_quantity_ordered,
                orderItem.requestedQuantity
            ),
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = stringResource(R.string.label_item_available_quantity, orderItemAvailabilityInStoreUiModel.availableQuantity),
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = stringResource(
                R.string.label_item_availability,
                getAvailabilityStatus(orderItemAvailabilityInStoreUiModel.orderItemAvailabilityStatus)
            ),
            style = MaterialTheme.typography.bodyLarge
        )

    }
}

@Composable
fun getAvailabilityStatus(orderItemAvailabilityStatus: OrderItemAvailabilityStatus): String {
    return when (orderItemAvailabilityStatus) {
        OrderItemAvailabilityStatus.AVAILABLE -> stringResource(R.string.label_item_available)
        OrderItemAvailabilityStatus.SUBSTITUTE_AVAILABLE -> stringResource(R.string.label_item_substitution_available)
        OrderItemAvailabilityStatus.AVAILABLE_IN_NEARBY_STORE -> stringResource(R.string.label_item_available_nearby_store)
        OrderItemAvailabilityStatus.OUT_OF_STOCK -> stringResource(R.string.label_item_out_of_stock)
    }
}