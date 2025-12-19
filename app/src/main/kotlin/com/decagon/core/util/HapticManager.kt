package com.decagon.core.util

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

object HapticManager {
    
    fun performLightClick(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun performSuccess(context: Context) {
        getVibrator(context)?.vibrate(
            VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
        )
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun performError(context: Context) {
        getVibrator(context)?.vibrate(
            VibrationEffect.createWaveform(
                longArrayOf(0, 50, 50, 50),
                intArrayOf(0, 255, 0, 255),
                -1
            )
        )
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun performHeavyClick(context: Context) {
        getVibrator(context)?.vibrate(
            VibrationEffect.createOneShot(75, 200)
        )
    }
    
    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
}

// Compose extension
@Composable
fun Modifier.hapticClick(
    onClick: () -> Unit
): Modifier {
    val context = androidx.compose.ui.platform.LocalContext.current
    val view = androidx.compose.ui.platform.LocalView.current
    
    return this.clickable {
        HapticManager.performLightClick(view)
        onClick()
    }
}