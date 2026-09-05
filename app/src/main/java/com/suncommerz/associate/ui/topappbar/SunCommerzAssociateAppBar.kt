package com.suncommerz.associate.ui.topappbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.suncommerz.associate.R
import com.suncommerz.associate.ui.navigation.SunCommerzAssociateScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SunCommerzAssociateAppBar(
    currentScreen: SunCommerzAssociateScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit
) {
    TopAppBar(
        modifier = Modifier,
        title = { Text(text = stringResource(currentScreen.title))},
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(
                    onClick = navigateUp
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}

@Preview
@Composable
fun SunCommerzAssociateAppBarPreview() {
    SunCommerzAssociateAppBar(
        currentScreen = SunCommerzAssociateScreen.Order,
        canNavigateBack = true,
        {}
    )
}