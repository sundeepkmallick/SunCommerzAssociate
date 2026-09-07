package com.suncommerz.associate.ui.order

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.suncommerz.associate.R
import com.suncommerz.associate.domain.model.Coordinate
import com.suncommerz.associate.domain.model.ItemPickupStatus
import com.suncommerz.associate.domain.model.Order
import com.suncommerz.associate.domain.model.OrderItem
import com.suncommerz.associate.domain.model.OrderStatus
import com.suncommerz.associate.domain.model.OrderUiModel
import com.suncommerz.associate.domain.model.Product
import com.suncommerz.associate.domain.model.ProductCategory
import com.suncommerz.associate.domain.model.Store
import com.suncommerz.associate.domain.model.StoreAssociate
import kotlin.time.Instant

@Composable
fun OrderDetailsCompact(
    uiState: OrderDetailsUiState.Loaded,
    onBackPressed: () -> Unit,
    onOrderItemSelected: (String, String) -> Unit
) {
    val orderUiModel = uiState.orderUiModel

    OrderDetailsCompactContent(orderUiModel, onOrderItemSelected)


}

@Composable
fun OrderDetailsCompactContent(orderUiModel: OrderUiModel, onOrderItemSelected: (String, String) -> Unit) {
    Surface {
        ConstraintLayout(
            modifier = Modifier.fillMaxSize().padding(dimensionResource(R.dimen.padding))
        ) {
            val (orderHeader, orderItemList, buttonPickUsingAiLayout) = createRefs()

            Column(
                modifier = Modifier.constrainAs(orderHeader){
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }.wrapContentHeight().fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = orderUiModel.order.id,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = orderUiModel.order.status.name,
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = orderUiModel.orderDateTimeFormatted,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            LazyColumn(
                modifier = Modifier.constrainAs(orderItemList){
                    top.linkTo(orderHeader.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }.wrapContentHeight().fillMaxWidth()
                    .padding(top = dimensionResource(R.dimen.padding)),
            ) {
                items(orderUiModel.order.items) { orderItem ->
                    ShowListItemOrderItem(orderItem) {
                        onOrderItemSelected(orderItem.orderId, orderItem.id)
                    }
                }
            }

            Column(
                modifier = Modifier.constrainAs(buttonPickUsingAiLayout){
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }.fillMaxWidth().wrapContentSize()
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth().wrapContentSize()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.secondary,
                            shape = RoundedCornerShape(dimensionResource(R.dimen.padding_small))
                        )
                        .padding(dimensionResource(R.dimen.padding)),
                ) {
                    Text(
                        text = "Click on each item in above list to manually pick or take help of AI to pick all items at once using below button."
                    )
                }

                Button(
                    modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.padding)),
                    onClick = {
                        //TODO
                    }
                ) {
                    Text(text = stringResource(R.string.button_pick_using_ai))
                }
            }


        }
    }

}

@Composable
fun ShowListItemOrderItem(orderItem: OrderItem, onOrderItemSelected: () -> Unit) {
    Card(
        modifier = Modifier.padding(dimensionResource(R.dimen.padding_extra_small)),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.tertiaryContainer),
        onClick = onOrderItemSelected,
        shape = RoundedCornerShape(dimensionResource(R.dimen.padding))
    ) {

        ConstraintLayout(
            modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.padding)),
        ) {
            val (productItemStatus, productItemName, tapIndicationArrow) = createRefs()

            Text(
                modifier = Modifier.constrainAs(productItemStatus){
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    bottom.linkTo(parent.bottom)
                }.wrapContentSize().padding(dimensionResource(R.dimen.padding_small)),
                text = orderItem.pickupStatus.name,
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                modifier = Modifier.constrainAs(productItemName){
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(productItemStatus.end)
                    end.linkTo(tapIndicationArrow.start)
                },
                text = orderItem.product.name,
                style = MaterialTheme.typography.titleMedium
            )


            IconButton(
                modifier = Modifier.constrainAs(tapIndicationArrow){
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
                onClick = {}
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowRight,
                    contentDescription = stringResource(R.string.content_description_icon_right_arrow)
                )
            }
        }
    }
}


@Preview
@Composable
fun OrderDetailsCompactContentPreview() {
    OrderDetailsCompactContent(
        orderUiModel = OrderUiModel(
            order = Order(
                id = "ORD-1001",
                status = OrderStatus.READY,
                orderDateTime = Instant.parse("2026-09-04T07:45:00Z"),
                store = Store("93", "Store #93", Coordinate(52.5208, 13.4095)),
                items = listOf(
                    OrderItem(
                        id = "OI-1001",
                        orderId = "ORD-1001",
                        product = Product(
                            id = "1",
                            name = "Whole Milk",
                            price = 1.19,
                            currency = "EUR",
                            description = "Fresh whole milk, 1 liter",
                            category = ProductCategory.DAIRY,
                            ingredients = listOf("Milk"),
                            attributes = mapOf(
                                "volume" to "1L",
                                "fat" to "3.5%",
                                "type" to "whole-milk"
                            ),
                        ),
                        requestedQuantity = 2,
                        pickupStatus = ItemPickupStatus.SUBSTITUTE,
                        pickedQuantity = 2,
                        selectedSubstituteId = "3"
                    )
                ),
                assignedAssociate = StoreAssociate("7", "emp007")
            ),
            orderDateTimeFormatted = "09 September, 2026 9:00"
        ),
        onOrderItemSelected = { _, _ -> }
    )
}


