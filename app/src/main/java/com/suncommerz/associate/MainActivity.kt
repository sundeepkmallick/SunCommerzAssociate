package com.suncommerz.associate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.suncommerz.associate.ui.SunCommerzAssociateScreen
import com.suncommerz.associate.ui.theme.SunCommerzAssociateTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SunCommerzAssociateTheme {
                SunCommerzAssociateScreen()
            }
        }
    }
}