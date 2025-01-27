package com.example.chatterplay.analytics

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ScreenPresenceLogger(screenName: String, userId: String) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var startTime by remember { mutableStateOf(0L) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    // Screen is visible
                    startTime = System.currentTimeMillis()
                }
                Lifecycle.Event.ON_STOP -> {
                    // Screen is no longer visible
                    val duration = System.currentTimeMillis() - startTime
                    CoroutineScope(Dispatchers.IO).launch {
                        val params = Bundle().apply {
                            putString("screen_name", screenName)
                            putString("userId", userId)
                            putLong("screen_duration", duration)
                        }
                        AnalyticsManager.getInstance(context).logEvent("screen_view", params)
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
