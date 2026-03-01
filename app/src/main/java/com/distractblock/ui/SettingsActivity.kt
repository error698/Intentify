package com.distractblock.ui

import android.app.TimePickerDialog
import android.view.View
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.distractblock.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch

class SettingsActivity : AppCompatActivity() {

    companion object {
        const val PREFS_NAME = "distractblock_prefs"
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_COUNTDOWN_SECS = "countdown_secs"
        const val KEY_ALLOW_OPEN = "allow_open_after_countdown"
        const val KEY_BLOCK_WEEKENDS = "block_weekends"
        const val KEY_SHOW_OPEN_COUNT = "show_open_count"
        const val KEY_WEEKLY_REPORT = "weekly_report_notif"
        const val KEY_DAILY_REMINDER = "daily_reminder"
        const val KEY_WEEKLY_SUMMARY = "weekly_summary"
        const val KEY_NOTIF_HOUR = "notif_hour"
        const val KEY_NOTIF_MINUTE = "notif_minute"
    }

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        setupBackButton()
        setupSectionHeaders()
        setupBlockingBehaviour()
        setupStats()
        setupAppearance()
        setupNotifications()
        setupAppManagement()
        setupAbout()
    }

    private fun setupBackButton() {
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }
    }

    private fun setupSectionHeaders() {
        // When <include> has a root TextView and is given an ID, that ID maps to the TextView directly
        findViewById<TextView>(R.id.header_blocking)?.text = "BLOCKING BEHAVIOUR"
        findViewById<TextView>(R.id.header_stats)?.text = "STATS & AWARENESS"
        findViewById<TextView>(R.id.header_appearance)?.text = "APPEARANCE"
        findViewById<TextView>(R.id.header_notifs)?.text = "NOTIFICATIONS"
        findViewById<TextView>(R.id.header_management)?.text = "APP MANAGEMENT"
        findViewById<TextView>(R.id.header_about)?.text = "ABOUT"
    }

    // ── BLOCKING BEHAVIOUR ──

    private fun setupBlockingBehaviour() {
        // Countdown duration
        val countdownSecs = prefs.getInt(KEY_COUNTDOWN_SECS, 5)
        updateCountdownLabel(countdownSecs)

        findViewById<View>(R.id.row_countdown).setOnClickListener {
            val options = arrayOf("3 seconds", "5 seconds", "10 seconds")
            val values = intArrayOf(3, 5, 10)
            val current = when (prefs.getInt(KEY_COUNTDOWN_SECS, 5)) {
                3 -> 0; 10 -> 2; else -> 1
            }
            MaterialAlertDialogBuilder(this)
                .setTitle("Countdown duration")
                .setSingleChoiceItems(options, current) { dialog, which ->
                    prefs.edit().putInt(KEY_COUNTDOWN_SECS, values[which]).apply()
                    updateCountdownLabel(values[which])
                    dialog.dismiss()
                }
                .show()
        }

        // Allow open after countdown
        val switchAllowOpen = findViewById<MaterialSwitch>(R.id.switch_allow_open)
        switchAllowOpen.isChecked = prefs.getBoolean(KEY_ALLOW_OPEN, true)
        switchAllowOpen.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean(KEY_ALLOW_OPEN, checked).apply()
        }

        // Block on weekends
        val switchWeekends = findViewById<MaterialSwitch>(R.id.switch_block_weekends)
        switchWeekends.isChecked = prefs.getBoolean(KEY_BLOCK_WEEKENDS, true)
        switchWeekends.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean(KEY_BLOCK_WEEKENDS, checked).apply()
        }

        // Scheduled blocking (coming soon)
        findViewById<View>(R.id.row_schedule).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Coming soon")
                .setMessage("Scheduled blocking windows will be available in a future update.")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun updateCountdownLabel(secs: Int) {
        findViewById<TextView>(R.id.tv_countdown_value).text = "$secs seconds"
    }

    // ── STATS & AWARENESS ──

    private fun setupStats() {
        val switchOpenCount = findViewById<MaterialSwitch>(R.id.switch_show_open_count)
        switchOpenCount.isChecked = prefs.getBoolean(KEY_SHOW_OPEN_COUNT, true)
        switchOpenCount.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean(KEY_SHOW_OPEN_COUNT, checked).apply()
        }

        val switchWeeklyReport = findViewById<MaterialSwitch>(R.id.switch_weekly_report)
        switchWeeklyReport.isChecked = prefs.getBoolean(KEY_WEEKLY_REPORT, false)
        switchWeeklyReport.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean(KEY_WEEKLY_REPORT, checked).apply()
        }

        findViewById<View>(R.id.row_reset_stats).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Reset stats")
                .setItems(arrayOf("Reset today's stats", "Reset this week's stats", "Reset all time stats")) { _, which ->
                    val label = when (which) { 0 -> "today's"; 1 -> "this week's"; else -> "all time" }
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Reset $label stats?")
                        .setMessage("This cannot be undone.")
                        .setPositiveButton("Reset") { _, _ ->
                            // TODO: call repository reset methods
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
                .show()
        }
    }

    // ── APPEARANCE ──

    private fun setupAppearance() {
        val switchDark = findViewById<MaterialSwitch>(R.id.switch_dark_mode)
        val isDark = prefs.getBoolean(KEY_DARK_MODE, isSystemDarkMode())
        switchDark.isChecked = isDark
        switchDark.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean(KEY_DARK_MODE, checked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (checked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )
        }

        findViewById<View>(R.id.row_accent_color).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Coming soon")
                .setMessage("Custom accent colors will be available in a future update.")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun isSystemDarkMode(): Boolean {
        val uiMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return uiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    // ── NOTIFICATIONS ──

    private fun setupNotifications() {
        val switchDaily = findViewById<MaterialSwitch>(R.id.switch_daily_reminder)
        switchDaily.isChecked = prefs.getBoolean(KEY_DAILY_REMINDER, false)
        switchDaily.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean(KEY_DAILY_REMINDER, checked).apply()
        }

        val switchWeeklySummary = findViewById<MaterialSwitch>(R.id.switch_weekly_summary)
        switchWeeklySummary.isChecked = prefs.getBoolean(KEY_WEEKLY_SUMMARY, false)
        switchWeeklySummary.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean(KEY_WEEKLY_SUMMARY, checked).apply()
        }

        val tvNotifTime = findViewById<TextView>(R.id.tv_notif_time)
        updateNotifTimeLabel(tvNotifTime)

        findViewById<View>(R.id.row_notif_time).setOnClickListener {
            val hour = prefs.getInt(KEY_NOTIF_HOUR, 8)
            val min = prefs.getInt(KEY_NOTIF_MINUTE, 0)
            TimePickerDialog(this, { _, h, m ->
                prefs.edit().putInt(KEY_NOTIF_HOUR, h).putInt(KEY_NOTIF_MINUTE, m).apply()
                updateNotifTimeLabel(tvNotifTime)
            }, hour, min, false).show()
        }
    }

    private fun updateNotifTimeLabel(tv: TextView) {
        val h = prefs.getInt(KEY_NOTIF_HOUR, 8)
        val m = prefs.getInt(KEY_NOTIF_MINUTE, 0)
        val amPm = if (h < 12) "AM" else "PM"
        val hour12 = if (h == 0) 12 else if (h > 12) h - 12 else h
        tv.text = "%d:%02d %s".format(hour12, m, amPm)
    }

    // ── APP MANAGEMENT ──

    private fun setupAppManagement() {
        findViewById<View>(R.id.row_pause_blocking).setOnClickListener {
            val options = arrayOf("30 minutes", "1 hour", "2 hours")
            val millis = longArrayOf(30 * 60 * 1000L, 60 * 60 * 1000L, 2 * 60 * 60 * 1000L)
            MaterialAlertDialogBuilder(this)
                .setTitle("Pause all blocking")
                .setItems(options) { _, which ->
                    com.distractblock.service.BlockerAccessibilityService.pauseBlocking(millis[which])
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Blocking paused")
                        .setMessage("All blocking paused for ${options[which]}.")
                        .setPositiveButton("OK", null)
                        .show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        findViewById<View>(R.id.row_export).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Coming soon")
                .setMessage("Import/export of blocked apps will be available in a future update.")
                .setPositiveButton("OK", null)
                .show()
        }

        findViewById<View>(R.id.row_biometric).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Coming soon")
                .setMessage("PIN/biometric lock will be available in a future update.")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    // ── ABOUT ──

    private fun setupAbout() {
        val versionName = try { packageManager.getPackageInfo(packageName, 0).versionName } catch (e: Exception) { "" }
        findViewById<TextView>(R.id.tv_version).text = "v$versionName"

        findViewById<View>(R.id.row_rate).setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
            } catch (e: Exception) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
            }
        }

        findViewById<View>(R.id.row_privacy).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Privacy Policy")
                .setMessage("Intentify does not collect, store or transmit any personal data. All data (blocked apps, usage stats) stays on your device only.")
                .setPositiveButton("OK", null)
                .show()
        }

        findViewById<View>(R.id.row_feedback).setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("feedback@intentify.app"))
                putExtra(Intent.EXTRA_SUBJECT, "Intentify App Feedback")
            }
            try { startActivity(intent) } catch (e: Exception) { }
        }
    }
}
