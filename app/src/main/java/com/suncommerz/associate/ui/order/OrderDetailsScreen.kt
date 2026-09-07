package com.suncommerz.associate.ui.order

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.suncommerz.associate.R
import com.suncommerz.associate.ui.common.UiContentType

@Composable
fun OrderDetailsScreen(
    viewModel: OrderDetailsViewModel = hiltViewModel(),
    contentType: UiContentType,
    onBackPressed: () -> Boolean,
    onOrderItemClick: (String, String) -> Unit
) {

    val uiState by viewModel.uiStat.collectAsStateWithLifecycle()
    OrderDetails(
        uiState,
        contentType,
        onOrderItemSelected = { orderId, orderItemId ->
            onOrderItemClick(orderId, orderItemId)
        },
        {onBackPressed()}
    )
}

@Composable
fun OrderDetails(
    uiState: OrderDetailsUiState,
    contentType: UiContentType,
    onOrderItemSelected: (String, String) -> Unit,
    onBackPressed: () -> Unit
) {
    when (uiState) {
        OrderDetailsUiState.Loading -> {
            ShowLoading()
        }

        is OrderDetailsUiState.Error -> {
            ShowErrorMessage(uiState.message)
        }

        is OrderDetailsUiState.Loaded -> {
            if (contentType == UiContentType.LIST_ONLY) {
                OrderDetailsCompact(
                    uiState = uiState,
                    onBackPressed = onBackPressed,
                    onOrderItemSelected = onOrderItemSelected
                )
            } else if (contentType == UiContentType.LIST_AND_DETAIL) {
                /*OrderDetailsExpanded(
                    uiState = uiState,
                    onBackPressed = onBackPressed,
                    onOrderItemClick = { orderItemId ->
                        onOrderItemClick(
                            uiState.order,
                            orderItemId
                        )
                    }
                )*/
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
