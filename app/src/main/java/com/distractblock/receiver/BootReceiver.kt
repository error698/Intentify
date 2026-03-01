package com.distractblock.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        // Accessibility service restarts automatically if it was enabled.
        // We can add: reset daily counts, schedule notifications, etc.
        Log.d("DistractBlock", "Boot complete — service will auto-restart if enabled")
    }
}
