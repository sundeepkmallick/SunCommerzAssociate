package com.suncommerz.associate.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.suncommerz.associate.ui.common.UiContentType
import com.suncommerz.associate.ui.navigation.SunCommerzAssociateNav
import com.suncommerz.associate.ui.navigation.SunCommerzAssociateScreen
import com.suncommerz.associate.ui.topappbar.SunCommerzAssociateAppBar

@Composable
fun SunCommerzAssociateScreen(windowSize: WindowWidthSizeClass) {
    val navController = rememberNavController()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route ?: SunCommerzAssociateScreen.Start.name
    val currentScreen = SunCommerzAssociateScreen.valueOf(route.substringBefore('/'))

    val contentType = when (windowSize) {
        WindowWidthSizeClass.Expanded -> UiContentType.LIST_AND_DETAIL
        else -> UiContentType.LIST_ONLY
    }

    Scaffold(
        topBar = {
            SunCommerzAssociateAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() },
                onSettingsClick = {
                    if (currentScreen != SunCommerzAssociateScreen.AIModelPreferences) {
                        navController.navigate(SunCommerzAssociateScreen.AIModelPreferences.name) {
                            launchSingleTop = true
                        }
                    }
                },
            )
        }
    ) { innerPadding ->
        SunCommerzAssociateNav(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            contentType = contentType
        )
    }
}