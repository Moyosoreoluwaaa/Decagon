package com.decagon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.fragment.app.FragmentActivity
import com.decagon.ui.navigation.DecagonNavGraph
import com.decagon.ui.theme.DecagonTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DecagonTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    DecagonNavGraph()
                }
            }
        }
    }
}