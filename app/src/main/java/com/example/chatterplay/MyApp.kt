package com.example.chatterplay

import android.app.Application
import com.example.chatterplay.analytics.AnalyticsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Set up global crash handler
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            handleUncaughtException(thread, throwable)
        }
    }

    private fun handleUncaughtException(thread: Thread, throwable: Throwable) {
        val context = this
        val analyticsManager = AnalyticsManager.getInstance(context)

        CoroutineScope(Dispatchers.IO).launch {
            // Log crash event in Firebase Analytics
            val params = android.os.Bundle().apply {
                putString("user_id", getCurrentUserId()) // Replace with method to fetch current user ID
                putString("error_message", throwable.message)
                putString("screen_name", (context as? MainActivity)?.getCurrentScreen() ?: "unknown_screen") // Replace with your screen-tracking logic
            }
            analyticsManager.logEvent("app_crash", params)
        }

        // Pass the exception to the default handler after logging
        Thread.getDefaultUncaughtExceptionHandler()?.uncaughtException(thread, throwable)
    }

    private fun getCurrentUserId(): String {
        // Return the current user ID or a default value if not logged in
        return "anonymous_user"
    }

    private fun getCurrentScreenName(): String {
        // Implement logic to fetch the current screen name, if available
        return "unknown_screen"
    }
}
