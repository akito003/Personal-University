package com.personaluniversity.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.personaluniversity.app.ui.nav.AppNavHost
import com.personaluniversity.app.ui.theme.Ink
import com.personaluniversity.app.ui.theme.PersonalUniversityTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PersonalUniversityTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Ink) {
                    AppNavHost()
                }
            }
        }
    }
}
