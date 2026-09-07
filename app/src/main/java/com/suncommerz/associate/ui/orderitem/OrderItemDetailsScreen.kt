package com.suncommerz.associate.ui.orderitem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutScope
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.suncommerz.associate.R
import com.suncommerz.associate.domain.model.ItemPickupStatus
import com.suncommerz.associate.domain.model.OrderItem
import com.suncommerz.associate.domain.model.OrderItemAvailabilityInStoreUiModel
import com.suncommerz.associate.domain.model.OrderItemAvailabilityStatus
import com.suncommerz.associate.domain.model.Product
import com.suncommerz.associate.domain.model.ProductCategory
import com.suncommerz.associate.domain.model.Store
import com.suncommerz.associate.domain.usecase.fullfilment.FulfillmentOptions

@Composable
public fun OrderItemDetailsScreen(
    viewModel: OrderItemDetailsViewModel = hiltViewModel(),
    onBackPressed: () -> Boolean
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OrderItemDetailsContent(uiState)
}

@Composable
fun OrderItemDetailsContent(uiState: OrderItemDetailsUiState) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        when (uiState) {
            OrderItemDetailsUiState.Loading -> {
                ShowLoading()
            }

            is OrderItemDetailsUiState.Error -> {
                ShowErrorMessage(uiState.message)
            }

            is OrderItemDetailsUiState.Loaded -> {

                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(R.dimen.padding))
                ) {

                    val (layoutProductItemInfo, layoutProductAlternative, layoutActionButtons) = createRefs()

                    ProductItemInfo(layoutProductItemInfo, uiState)

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
                        if(uiState.orderItemAvailabilityInStoreUiModel.orderItemAvailabilityStatus
                            == OrderItemAvailabilityStatus.SUBSTITUTE_AVAILABLE
                        ) {
                            val substituteProducts = uiState.fulfillmentOptions?.substitutesInCurrentStore
                            if(substituteProducts?.isNotEmpty() == true){
                                LazyColumn(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                                    items(substituteProducts){ substituteProduct ->
                                        ListItemSubstituteItem(substituteProduct, {})
                                    }
                                }
                            }
                        } else if(uiState.orderItemAvailabilityInStoreUiModel.orderItemAvailabilityStatus
                            == OrderItemAvailabilityStatus.AVAILABLE_IN_NEARBY_STORE) {
                            val nearByStores =  uiState.fulfillmentOptions?.originalProductNearbyStores
                            if(nearByStores?.isNotEmpty() == true) {
                                LazyColumn(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                                    items(nearByStores){ nearByStore ->
                                        ListItemNearByStoresWhereOrderItemAvailable(nearByStore, {})
                                    }
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.constrainAs(layoutActionButtons) {
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        },
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(dimensionResource(R.dimen.padding_small)),
                            onClick = {},
                        ) {
                            Text(text = stringResource(R.string.button_pick_manual))
                        }
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(dimensionResource(R.dimen.padding_small)),
                            onClick = {},
                        ) {
                            Text(text = stringResource(R.string.button_notify_manager))
                        }
                    }


                }
            }

        }
    }
}

@Composable
fun ListItemSubstituteItem(
    substituteProduct: Product,
    onSubstituteItemSelected: () -> Unit
) {
    Card(
        modifier = Modifier.padding(dimensionResource(R.dimen.padding_extra_small)),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.tertiaryContainer),
        onClick = onSubstituteItemSelected,
        shape = RoundedCornerShape(dimensionResource(R.dimen.padding))
    ) {

        ConstraintLayout(
            modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.padding)),
        ) {
            val (substituteProductDetails, tapIndicationArrow) = createRefs()

            Column(
                modifier = Modifier.constrainAs(substituteProductDetails){
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    bottom.linkTo(parent.bottom)
                }.wrapContentHeight().padding(dimensionResource(R.dimen.padding_small))
            ) {
                Text(
                    modifier = Modifier.wrapContentSize().padding(dimensionResource(R.dimen.padding_extra_small)),
                    text = substituteProduct.name,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    modifier = Modifier.wrapContentSize().padding(dimensionResource(R.dimen.padding_extra_small)),
                    text = "${substituteProduct.price} ${substituteProduct.currency}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    modifier = Modifier.wrapContentSize().padding(dimensionResource(R.dimen.padding_extra_small)),
                    text = substituteProduct.description,
                    style = MaterialTheme.typography.bodySmall
                )
            }


            IconButton(
                modifier = Modifier.constrainAs(tapIndicationArrow){
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
                onClick = {}
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.content_description_icon_pickup_item)
                )
            }
        }
    }
}

@Composable
fun ListItemNearByStoresWhereOrderItemAvailable(nearbyStore: Store, orderItemInNearByStoreReserved: () -> Unit) {
    Card(
        modifier = Modifier.padding(dimensionResource(R.dimen.padding_extra_small)),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.tertiaryContainer),
        onClick = orderItemInNearByStoreReserved,
        shape = RoundedCornerShape(dimensionResource(R.dimen.padding))
    ) {

        ConstraintLayout(
            modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.padding)),
        ) {
            val (nearbyStoreDetails, tapIndicationArrow) = createRefs()

            Column(
                modifier = Modifier.constrainAs(nearbyStoreDetails){
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    bottom.linkTo(parent.bottom)
                }.wrapContentHeight().padding(dimensionResource(R.dimen.padding_small))
            ) {
                Text(
                    modifier = Modifier.wrapContentSize().padding(dimensionResource(R.dimen.padding_extra_small)),
                    text = nearbyStore.name,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    modifier = Modifier.wrapContentSize().padding(dimensionResource(R.dimen.padding_extra_small)),
                    text = nearbyStore.location.toString(),
                    style = MaterialTheme.typography.bodySmall
                )
            }


            IconButton(
                modifier = Modifier.constrainAs(tapIndicationArrow){
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
                onClick = {}
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.content_description_icon_pickup_item)
                )
            }
        }
    }
}

@Composable
private fun ConstraintLayoutScope.ProductItemInfo(
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
        OrderItemAvailabilityStatus.LOW -> stringResource(R.string.label_item_stock_low)
        OrderItemAvailabilityStatus.OUT_OF_STOCK -> stringResource(R.string.label_item_out_of_stock)
    }
}

@Composable
fun ShowLoading() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.width(dimensionResource(R.dimen.app_progressbar)),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun ShowErrorMessage(errorMessage: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.padding_extra_small)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Snackbar(
            action = {
                TextButton(
                    onClick = { }
                ) { }
            }
        ) {
            Text(text = errorMessage)
        }
    }
}

@Preview
@Composable
fun OrderItemDetailsContentPreview() {
    OrderItemDetailsContent(
        OrderItemDetailsUiState.Loaded(
            orderItem = OrderItem(
                id = "OI-1013",
                orderId = "ORD-1007",
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
                requestedQuantity = 3,
                pickupStatus = ItemPickupStatus.SUBSTITUTE,
                pickedQuantity = 3,
                selectedSubstituteId = "3"
            ),
            orderItemAvailabilityInStoreUiModel = OrderItemAvailabilityInStoreUiModel(
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
                availableQuantity = 5,
                orderItemAvailabilityStatus = OrderItemAvailabilityStatus.LOW
            ),
            fulfillmentOptions = FulfillmentOptions(emptyList(), emptyList()),
            actionState = ItemPickupStatus.PENDING,
        )
    )
}