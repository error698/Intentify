package com.distractblock.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.distractblock.R
import com.distractblock.data.BlockedApp
import com.distractblock.data.UsageStat
import com.distractblock.util.hasSystemAlertWindowPermission
import com.distractblock.util.hasUsageStatsPermission
import com.distractblock.util.isAccessibilityServiceEnabled
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val vm: MainViewModel by viewModels()
    private lateinit var adapter: BlockedAppAdapter

    companion object {
        private const val PREFS_NAME = "distractblock_prefs"
        private const val KEY_DARK_MODE = "dark_mode"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply saved theme before setContentView
        applySavedTheme()

        setContentView(R.layout.activity_main)

        // Greeting
        findViewById<TextView>(R.id.tv_greeting).text = greeting()



        // Settings button
        findViewById<android.widget.ImageButton>(R.id.btn_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // RecyclerView
        adapter = BlockedAppAdapter(
            packageManager = packageManager,
            onRemove = { vm.removeApp(it) },
            coroutineScope = lifecycleScope,
            onLoadStats = { pkg ->
                vm.syncUsageStats(pkg)
                // small delay to let sync finish, then fetch
                kotlinx.coroutines.delay(500)
                vm.getLast7DaysStats(pkg)
            }
        )
        findViewById<RecyclerView>(R.id.rv_apps).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
            setHasFixedSize(false)
        }

        // Observe blocked apps list
        lifecycleScope.launch {
            vm.blockedApps.collect { apps ->
                adapter.submitList(apps)
                findViewById<TextView>(R.id.tv_empty).visibility =
                    if (apps.isEmpty()) View.VISIBLE else View.GONE
            }
        }

         // FAB
        findViewById<FloatingActionButton>(R.id.fab_add).setOnClickListener {
            showAddAppDialog()
        }
    }

    private fun applySavedTheme() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val isDark = prefs.getBoolean(KEY_DARK_MODE, isSystemDarkMode())
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )
    }

    private fun isSystemDarkMode(): Boolean {
        val uiMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return uiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    override fun onResume() {
        super.onResume()
        showPermissionDialogs()
    }

    private fun showPermissionDialogs() {
        val needAccessibility = !isAccessibilityServiceEnabled()
        val needUsageStats = !hasUsageStatsPermission()
        val needBattery = !isBatteryOptimizationIgnored()
        val needOverlay = !hasSystemAlertWindowPermission()

        if (needAccessibility) {
            com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Enable Accessibility Service")
                .setMessage("Intentify needs the Accessibility Service to detect when you open blocked apps and show the pause screen. The app cannot function without this.")
                .setPositiveButton("Enable") { _, _ ->
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
                .setCancelable(false)
                .show()
        } else if (needUsageStats) {
            com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Grant Usage Access")
                .setMessage("Intentify needs Usage Access to track screen time and show your app usage statistics. The app cannot function without this.")
                .setPositiveButton("Grant") { _, _ ->
                    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }
                .setCancelable(false)
                .show()
        } else if (needOverlay) {
            com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Allow Display Over Other Apps")
                .setMessage("Intentify needs permission to display the block screen over other apps. This ensures the block screen appears reliably.")
                .setPositiveButton("Grant") { _, _ ->
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                        data = android.net.Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                }
                .setCancelable(false)
                .show()
        } else if (needBattery) {
            com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Disable Battery Optimisation")
                .setMessage("Your phone's battery saver can silently stop Intentify's blocking service in the background. To keep it always active, please disable battery optimisation for this app.")
                .setPositiveButton("Disable") { _, _ ->
                    val intent = Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = android.net.Uri.parse("package:$packageName")
                    }
                    try { startActivity(intent) } catch (e: Exception) {
                        // Fallback if device doesn't support direct intent
                        startActivity(Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                    }
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun isBatteryOptimizationIgnored(): Boolean {
        val pm = getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
        return pm.isIgnoringBatteryOptimizations(packageName)
    }

    private fun showAddAppDialog() {
        lifecycleScope.launch {
            val excluded = vm.blockedApps.value.map { it.packageName }.toSet()
            val available = vm.getInstallableApps(excluded)

            if (available.isEmpty()) {
                Toast.makeText(this@MainActivity, "All apps are already blocked!", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val dialogView = LayoutInflater.from(this@MainActivity)
                .inflate(R.layout.dialog_app_picker, null)

            val pickerAdapter = AppPickerAdapter(available)

            val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(this@MainActivity)
                .setView(dialogView)
                .setPositiveButton("Block") { _, _ ->
                    pickerAdapter.getSelected().forEach { pkg -> vm.addApp(pkg) }
                }
                .setNegativeButton("Cancel", null)
                .create()

            val rv = dialogView.findViewById<RecyclerView>(R.id.rv_app_picker)
            rv.layoutManager = LinearLayoutManager(this@MainActivity)
            rv.adapter = pickerAdapter

            dialog.show()
        }
    }

    // ── App picker adapter ───────────────────────────────────────────────────
    private inner class AppPickerAdapter(
        private val apps: List<Pair<String, String>>
    ) : RecyclerView.Adapter<AppPickerAdapter.PVH>() {

        private val selected = mutableSetOf<String>()

        fun getSelected(): Set<String> = selected

        override fun getItemCount() = apps.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PVH(
            LayoutInflater.from(parent.context).inflate(R.layout.item_app_picker, parent, false)
        )

        override fun onBindViewHolder(holder: PVH, position: Int) {
            val (pkg, name) = apps[position]
            holder.tvName.text = name
            holder.cbSelect.isChecked = pkg in selected
            try {
                val info = packageManager.getApplicationInfo(pkg, 0)
                holder.ivIcon.setImageDrawable(packageManager.getApplicationIcon(info))
            } catch (_: Exception) {
                holder.ivIcon.setImageResource(android.R.drawable.sym_def_app_icon)
            }
            holder.itemView.setOnClickListener {
                if (pkg in selected) selected.remove(pkg) else selected.add(pkg)
                holder.cbSelect.isChecked = pkg in selected
            }
        }

        inner class PVH(v: View) : RecyclerView.ViewHolder(v) {
            val ivIcon: ImageView = v.findViewById(R.id.iv_app_icon)
            val tvName: TextView = v.findViewById(R.id.tv_app_name)
            val cbSelect: com.google.android.material.checkbox.MaterialCheckBox = v.findViewById(R.id.cb_select)
        }
    }

    private fun greeting() = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11  -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else      -> "Good Evening"
    }
}

// ── Adapter ──────────────────────────────────────────────────────────────────

class BlockedAppAdapter(
    private val packageManager: android.content.pm.PackageManager,
    private val onRemove: (String) -> Unit,
    private val coroutineScope: CoroutineScope,
    private val onLoadStats: suspend (String) -> List<UsageStat>
) : androidx.recyclerview.widget.ListAdapter<BlockedApp, BlockedAppAdapter.VH>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : androidx.recyclerview.widget.DiffUtil.ItemCallback<BlockedApp>() {
            override fun areItemsTheSame(old: BlockedApp, new: BlockedApp) =
                old.packageName == new.packageName

            override fun areContentsTheSame(old: BlockedApp, new: BlockedApp) = old == new
        }

        private val DAY_FORMAT = SimpleDateFormat("EEE", Locale.getDefault())
    }

    private var expandedPkg: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_blocked_app, parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        private val ivIcon: ImageView = v.findViewById(R.id.iv_icon)
        private val tvName: TextView  = v.findViewById(R.id.tv_name)
        private val tvCount: TextView = v.findViewById(R.id.tv_open_count)
        private val btnRemove: ImageButton = v.findViewById(R.id.btn_remove)
        private val statsContainer: View = v.findViewById(R.id.usage_stats_container)
        private val rowHeader: View = v.findViewById(R.id.row_header)

        private val barViews = listOf(
            v.findViewById<View>(R.id.bar_day1),
            v.findViewById<View>(R.id.bar_day2),
            v.findViewById<View>(R.id.bar_day3),
            v.findViewById<View>(R.id.bar_day4),
            v.findViewById<View>(R.id.bar_day5),
            v.findViewById<View>(R.id.bar_day6),
            v.findViewById<View>(R.id.bar_day7)
        )

        fun bind(app: BlockedApp) {
            tvName.text = app.appName
            tvCount.text = "opened ${app.dailyOpenCount}× today"
            try {
                val info = packageManager.getApplicationInfo(app.packageName, 0)
                ivIcon.setImageDrawable(packageManager.getApplicationIcon(info))
            } catch (e: Exception) {
                ivIcon.setImageResource(android.R.drawable.sym_def_app_icon)
            }
            btnRemove.setOnClickListener { onRemove(app.packageName) }

            val isExpanded = app.packageName == expandedPkg
            statsContainer.visibility = if (isExpanded) View.VISIBLE else View.GONE

            rowHeader.setOnClickListener {
                val wasPkg = expandedPkg
                if (wasPkg == app.packageName) {
                    // Collapse
                    expandedPkg = null
                    notifyItemChanged(bindingAdapterPosition)
                } else {
                    // Collapse previous
                    val prevPos = currentList.indexOfFirst { it.packageName == wasPkg }
                    expandedPkg = app.packageName
                    if (prevPos >= 0) notifyItemChanged(prevPos)
                    notifyItemChanged(bindingAdapterPosition)

                    // Load stats
                    coroutineScope.launch {
                        val stats = onLoadStats(app.packageName)
                        renderBars(stats)
                    }
                }
            }

            // If already expanded (from rebind), load stats
            if (isExpanded) {
                coroutineScope.launch {
                    val stats = onLoadStats(app.packageName)
                    renderBars(stats)
                }
            }
        }

        private fun renderBars(stats: List<UsageStat>) {
            val cal = Calendar.getInstance()
            val maxMinutes = 1440 // 24 hours — full bar = entire day

            for (i in 0 until 7) {
                val barRow = barViews[i]
                val tvDay = barRow.findViewById<TextView>(R.id.tv_day_label)
                val barFill = barRow.findViewById<View>(R.id.bar_fill)
                val tvDuration = barRow.findViewById<TextView>(R.id.tv_duration)

                cal.time = java.util.Date()
                cal.add(Calendar.DAY_OF_YEAR, -i)
                val dayLabel = if (i == 0) "Today" else DAY_FORMAT.format(cal.time)
                tvDay.text = dayLabel

                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                val stat = stats.firstOrNull { it.date == dateStr }
                val minutes = stat?.minutesUsed ?: 0

                // Format duration
                tvDuration.text = when {
                    minutes >= 60 -> "${minutes / 60}h ${minutes % 60}m"
                    minutes > 0   -> "${minutes}m"
                    else          -> "0m"
                }

                // Set bar width proportionally
                barFill.post {
                    val parent = barFill.parent as View
                    val ratio = if (maxMinutes > 0) minutes.toFloat() / maxMinutes else 0f
                    val lp = barFill.layoutParams
                    lp.width = (parent.width * ratio).toInt().coerceAtLeast(if (minutes > 0) 4 else 0)
                    barFill.layoutParams = lp
                }
            }
        }
    }
}

