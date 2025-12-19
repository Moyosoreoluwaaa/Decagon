package com.koin.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.decagon.R
import com.koin.ui.session.SessionViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

/**
 * Shows a splash while we decide where to go (auth vs main).
 * Uses the system splash on Android 12+, so keep this quick (<800ms ideally).
 */
@Composable
fun SplashScreen(navController: NavController) {
    val sessionViewModel: SessionViewModel = koinViewModel()
    val isLoggedIn = sessionViewModel.isLoggedIn.collectAsState().value

    // Navigate after small delay to allow first collect.
    LaunchedEffect(isLoggedIn) {
        // Give the ViewModel a frame to emit.
        delay(150)
        val target = if (isLoggedIn) "portfolio" else "auth"
        navController.navigate(target) {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier
            .size(200.dp)
            .clip(RoundedCornerShape(20))) {

            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                contentScale = ContentScale.Crop

                )
        }
    }
}
