package com.suncommerz.associate.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onOrderListItemSelected: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedOrderStatus by viewModel.selectedOrderStatus.collectAsStateWithLifecycle()

    Column {
        OrderStatusFilterBar(
            selectedOrderStatus = selectedOrderStatus,
            onOrderStatusSelected = {status ->
                viewModel.onStatusFilterChanged(status)
            }
        )
        OrderList(uiState, onOrderListItemSelected)
    }

}

@Composable
fun OrderStatusFilterBar(
    selectedOrderStatus: OrderStatus?,
    onOrderStatusSelected: (OrderStatus?) -> Unit
) {
    val filters = listOf(null) + OrderStatus.entries

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.padding)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding))
    ) {
        items(filters) { status ->
            FilterChip(
                selected = (status == selectedOrderStatus),
                onClick = {onOrderStatusSelected(status)},
                label = {
                    Text(text = status?.name ?: stringResource(R.string.filter_none))
                }
            )
        }
    }
}

@Composable
fun OrderList(
    uiState: DashboardUiState,
    onOrderListItemSelected: (String) -> Unit
) {

    Surface(modifier = Modifier.fillMaxSize()) {
        when(uiState) {
            DashboardUiState.Loading -> {
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

            is DashboardUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(dimensionResource(R.dimen.padding_extra_small)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Snackbar(
                        action = {
                            TextButton(
                                onClick = {  }
                            ) { }
                        }
                    ) {
                        Text(text = uiState.message)
                    }
                }
            }

            is DashboardUiState.Loaded -> {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(dimensionResource(R.dimen.padding)),
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding))
                    ) {
                        items( uiState.orderList) { order ->
                            OrderListItem(
                                orderUiModel = order,
                                onCardClick = {
                                    onOrderListItemSelected(order.order.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderListItem(orderUiModel: OrderUiModel, onCardClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxSize().padding(dimensionResource(R.dimen.padding_extra_small)),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer),
        onClick = onCardClick,
        elevation = CardDefaults.cardElevation(dimensionResource(R.dimen.list_item_card_elevation))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(R.dimen.order_list_item_inner_padding)),
            horizontalArrangement = Arrangement.Start, //Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.fillMaxSize().background(
                    getOrderStatusColor(orderUiModel.order.status)
                ).weight(0.2f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    modifier = Modifier,
                    text = orderUiModel.order.status.name,
                    fontSize = 10.sp,
                    color = Color.White
                )
            }

            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(dimensionResource(R.dimen.padding))
                    .weight(0.5f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = orderUiModel.order.id,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.order_items_label, orderUiModel.order.items.size.toString())
                )
            }

            Text(
                modifier = Modifier.weight(0.3f),
                text = orderUiModel.orderDateTimeFormatted
            )

            IconButton(
                modifier = Modifier.weight(0.1f),
                onClick = {}
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowRight,
                    contentDescription = stringResource(R.string.right_arrow)
                )
            }


        }
    }
}

@Composable
private fun getOrderStatusColor(orderStatus: OrderStatus): Color {
    return when(orderStatus) {
        OrderStatus.PENDING -> Color.Blue
        OrderStatus.INCOMPLETE -> Color.Red
        OrderStatus.READY -> Color.Green
        else -> Color.Gray
    }
}

@Preview
@Composable
fun DashboardScreenLoading() {
    OrderList(uiState = DashboardUiState.Loading, {})
}

@Preview
@Composable
fun DashboardScreenError() {
    OrderList(uiState = DashboardUiState.Error(message = "Some error occurred!"), {})
}

@Preview
@Composable
fun OrderListPreviewLoaded() {
    OrderList(
        uiState = DashboardUiState.Loaded(
            loggedInUser = StoreAssociate("1", "testuser001"),
            orderList = listOf(
                OrderUiModel(
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
                                    substituteProductIds = listOf("2", "3", "4")
                                ),
                                requestedQuantity = 2,
                                pickupStatus = ItemPickupStatus.SUBSTITUTE,
                                pickedQuantity = 2,
                                selectedSubstituteId = "3"
                            )
                        ),
                        assignedAssociate = StoreAssociate("7", "emp007")
                    ),
                    orderDateTimeFormatted = "04 Sep 2026, 14:56"
                )
            )
        ),
        onOrderListItemSelected = {}
    )
}
