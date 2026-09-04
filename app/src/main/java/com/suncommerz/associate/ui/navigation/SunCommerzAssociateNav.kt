package com.suncommerz.associate.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.suncommerz.associate.ui.dashboard.DashboardScreen

@Composable
fun SunCommerzAssociateNav(navController: NavHostController, modifier: Modifier) {

        NavHost(
            navController = navController,
            startDestination = SunCommerzAssociateScreen.Start.name,
            modifier = modifier
        ) {
            composable(SunCommerzAssociateScreen.Start.name) {
                DashboardScreen()
            }
        }




}

