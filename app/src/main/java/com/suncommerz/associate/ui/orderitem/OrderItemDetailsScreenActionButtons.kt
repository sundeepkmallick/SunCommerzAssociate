package com.suncommerz.associate.ui.orderitem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayoutScope
import com.suncommerz.associate.R
import com.suncommerz.associate.domain.model.OrderItemAvailabilityStatus

@Composable
fun ConstraintLayoutScope.OrderItemDetailsScreenActionButtons(
    layoutActionButtons: ConstrainedLayoutReference,
    uiState: OrderItemDetailsUiState.Loaded,
    notifyOutOfStock: (String, String, String) -> Unit,
    notifyStockLow: (String, String, String) -> Unit,
    pickUpItem: (String, String, Int) -> Unit
) {
    Column(
        modifier = Modifier.constrainAs(layoutActionButtons) {
            bottom.linkTo(parent.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        },
        verticalArrangement = Arrangement.Bottom
    ) {

        when (uiState.orderItemAvailabilityInStoreUiModel.orderItemAvailabilityStatus.name) {
            OrderItemAvailabilityStatus.AVAILABLE.name -> {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(dimensionResource(R.dimen.padding_small)),
                    onClick = {
                        pickUpItem(
                            uiState.orderItem.orderId,
                            uiState.orderItem.id,
                            uiState.orderItem.requestedQuantity
                        )
                    },
                ) {
                    Text(text = stringResource(R.string.button_pick_manual))
                }
            }

            OrderItemAvailabilityStatus.OUT_OF_STOCK.name -> {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(dimensionResource(R.dimen.padding_small)),
                    onClick = {
                        notifyOutOfStock(
                            uiState.orderItem.orderId,
                            uiState.orderItem.id,
                            uiState.orderItem.product.id
                        )
                    },
                ) {
                    Text(text = stringResource(R.string.button_notify_out_of_stock))
                }
            }
        }

        if (uiState.orderItemAvailabilityInStoreUiModel.isStockLow) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(dimensionResource(R.dimen.padding_small)),
                onClick = {
                    notifyStockLow(
                        uiState.orderItem.orderId,
                        uiState.orderItem.id,
                        uiState.orderItem.product.id
                    )
                },
            ) {
                Text(text = stringResource(R.string.button_notify_stock_low))
            }
        }


    }
}