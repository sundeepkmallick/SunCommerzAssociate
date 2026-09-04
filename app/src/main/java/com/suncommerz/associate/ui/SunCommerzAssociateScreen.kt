package com.suncommerz.associate.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.suncommerz.associate.ui.navigation.SunCommerzAssociateNav
import com.suncommerz.associate.ui.navigation.SunCommerzAssociateScreen
import com.suncommerz.associate.ui.topappbar.SunCommerzAssociateAppBar

@Composable
fun SunCommerzAssociateScreen() {
    val navController = rememberNavController()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = SunCommerzAssociateScreen.valueOf(
        backStackEntry?.destination?.route ?: SunCommerzAssociateScreen.Start.name
    )

    Scaffold(
        topBar = {
            SunCommerzAssociateAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        SunCommerzAssociateNav(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}