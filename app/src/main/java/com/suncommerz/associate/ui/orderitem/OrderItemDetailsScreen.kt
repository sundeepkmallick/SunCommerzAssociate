package com.suncommerz.associate.ui.orderitem

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.suncommerz.associate.R
import com.suncommerz.associate.domain.model.ItemPickupStatus
import com.suncommerz.associate.domain.model.NotificationReasonType
import com.suncommerz.associate.domain.model.OrderItem
import com.suncommerz.associate.domain.model.OrderItemAvailabilityInStoreUiModel
import com.suncommerz.associate.domain.model.OrderItemAvailabilityStatus
import com.suncommerz.associate.domain.model.Product
import com.suncommerz.associate.domain.model.ProductCategory
import com.suncommerz.associate.domain.usecase.fullfilment.FulfillmentOptions

@Composable
fun OrderItemDetailsScreen(
    viewModel: OrderItemDetailsViewModel = hiltViewModel(),
    onBackPressed: () -> Boolean
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OrderItemDetailsContent(
        uiState = uiState,
        onSubstituteProductItemSelected = { substituteProductId, requestedQuantity ->
            viewModel.onSubstituteProductItemSelected(substituteProductId, requestedQuantity)
        },
        orderItemInNearByStoreReserved = { nearByStoreId, requestedQuantity ->
            viewModel.orderItemInNearByStoreReserved(nearByStoreId, requestedQuantity)
        },
        notifyOutOfStock = { orderId, orderItemId, productId ->
            viewModel.notifyOutOfStock(orderId, orderItemId, productId)
            onBackPressed()
        },
        notifyStockLow = { orderId, orderItemId, productId ->
            viewModel.notifyStockLow(orderId, orderItemId, productId)
        },
        pickUpItem = { orderId, orderItemId, requestedQuantity ->
            viewModel.pickUpItem(orderId, orderItemId, requestedQuantity)
            onBackPressed()
        }
    )

    ShowToastMessageNotified(viewModel)
}


@Composable
fun OrderItemDetailsContent(
    uiState: OrderItemDetailsUiState,
    onSubstituteProductItemSelected: (String, Int) -> Unit,
    orderItemInNearByStoreReserved: (String, Int) -> Unit,
    notifyOutOfStock: (String, String, String) -> Unit,
    notifyStockLow: (String, String, String) -> Unit,
    pickUpItem: (String, String, Int) -> Unit
) {
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

                    OrderItemProductInfo(layoutProductItemInfo, uiState)
                    OrderItemProductSubstituteOrInNearByStore(
                        layoutProductAlternative,
                        layoutProductItemInfo,
                        uiState,
                        onSubstituteProductItemSelected,
                        orderItemInNearByStoreReserved
                    )
                    OrderItemDetailsScreenActionButtons(
                        layoutActionButtons = layoutActionButtons,
                        uiState = uiState,
                        notifyOutOfStock = notifyOutOfStock,
                        notifyStockLow = notifyStockLow,
                        pickUpItem = pickUpItem
                    )
                }
            }

        }
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

@Composable
fun ShowToastMessageNotified(viewModel: OrderItemDetailsViewModel) {
    val context = LocalContext.current

    val toastMessageNotifiedStockLow = stringResource(R.string.notified_low_stock)
    val toastMessageNotifiedOutOfStock = stringResource(R.string.notified_out_of_stock)
    val toastMessageNotifiedManager = stringResource(R.string.notified_notified_manager)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is OrderItemDetailsEvent.ShowToastNotified -> {
                    Toast.makeText(
                        context,
                        when (event.notificationReasonType) {
                            NotificationReasonType.INVENTORY_ISSUE_LOW_STOCK -> toastMessageNotifiedStockLow
                            NotificationReasonType.INVENTORY_ISSUE_OUT_OF_STOCK -> toastMessageNotifiedOutOfStock
                            else -> toastMessageNotifiedManager
                        },
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
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
                isStockLow = false,
                orderItemAvailabilityStatus = OrderItemAvailabilityStatus.AVAILABLE,
            ),
            fulfillmentOptions = FulfillmentOptions(emptyList(), emptyList()),
            actionState = ItemPickupStatus.PENDING,
        ),
        onSubstituteProductItemSelected = { _, _ -> },
        orderItemInNearByStoreReserved = { _, _ -> },
        notifyOutOfStock = { _, _, _ -> },
        notifyStockLow = { _, _, _ -> },
        pickUpItem = { _, _, _ -> }
    )
}