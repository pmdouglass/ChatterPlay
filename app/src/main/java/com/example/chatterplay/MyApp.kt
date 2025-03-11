package com.example.chatterplay

import android.app.Application
import android.content.Intent
import com.example.chatterplay.analytics.AnalyticsManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

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
                putString("screen_name", getCurrentScreenName()) // Replace with your screen-tracking logic
            }
            analyticsManager.logEvent("app_crash", params)
        }

        // Restart app instead of crashing
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)

        // Kill process to prevent infinite loop crashes
        exitProcess(1)
    }

    private fun getCurrentUserId(): String {
        // Return the current user ID or a default value if not logged in
        return FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous_user"
    }

    private fun getCurrentScreenName(): String {
        // Implement logic to fetch the current screen name, if available
        return (applicationContext as? MainActivity)?.getCurrentScreen() ?: "unknown_screen"
    }
}
