package com.suncommerz.associate.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.suncommerz.associate.ui.aipickup.AIPickUpScreen
import com.suncommerz.associate.ui.common.UiContentType
import com.suncommerz.associate.ui.dashboard.DashboardScreen
import com.suncommerz.associate.ui.order.OrderDetailsScreen
import com.suncommerz.associate.ui.orderitem.OrderItemDetailsScreen

@Composable
fun SunCommerzAssociateNav(
    navController: NavHostController,
    modifier: Modifier,
    contentType: UiContentType
) {

        NavHost(
            navController = navController,
            startDestination = SunCommerzAssociateScreen.Start.name,
            modifier = modifier
        ) {
            composable(SunCommerzAssociateScreen.Start.name) {
                DashboardScreen(
                    onOrderListItemSelected = {
                        navController.navigate("${SunCommerzAssociateScreen.Order.name}/${it}")
                    }
                )
            }

            composable("${SunCommerzAssociateScreen.Order.name}/{orderId}") { entry ->
                OrderDetailsScreen(
                    hiltViewModel(),
                    contentType,
                    onBackPressed = { navController.popBackStack() },
                    onOrderItemClick = { selectedOrderId, orderItemId ->
                        navController.navigate(
                            "${SunCommerzAssociateScreen.OrderItem.name}/${selectedOrderId}/${orderItemId}"
                        )
                    },
                    onAiPickupClick = { orderId ->
                        navController.navigate(
                            "${SunCommerzAssociateScreen.AIPickUp.name}/${orderId}"
                        )
                    }
                )
            }

            composable("${SunCommerzAssociateScreen.OrderItem.name}/{orderId}/{orderItemId}") {
                OrderItemDetailsScreen(
                    hiltViewModel(),
                    onBackPressed = { navController.popBackStack() }
                )
            }

            composable("${SunCommerzAssociateScreen.AIPickUp.name}/{orderId}") {
                AIPickUpScreen(
                    hiltViewModel(),
                    onBackPressed = { navController.popBackStack() }
                )
            }
        }
}

