package com.example.chatterplay.analytics

import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.launch

class UserPresenceObserver(private val context: Context, private val userId: String) : DefaultLifecycleObserver {

    private val analyticsManager = AnalyticsManager.getInstance(context)
    private var startTime: Long = 0
    val coroutineScope = rememberCoroutineScope()

    override fun onStart(owner: LifecycleOwner) {
        // App enters the foreground
        startTime = System.currentTimeMillis()
        logUserPresence("online")
    }

    override fun onStop(owner: LifecycleOwner) {
        // App goes to the background
        val sessionDuration = System.currentTimeMillis() - startTime
        logUserPresence("offline", sessionDuration)
    }

    private fun logUserPresence(status: String, sessionDuration: Long = 0) {
        coroutineScope.launch {
            val params = Bundle().apply {
                putString("status", status)
                putString("userId", userId)
                if (sessionDuration > 0) {
                    putLong("session_duration", sessionDuration)
                }
            }
            analyticsManager.logEvent("user_status_change", params)
        }
    }
}
