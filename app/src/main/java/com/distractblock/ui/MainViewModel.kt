package com.distractblock.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.distractblock.data.AppRepository
import com.distractblock.data.BlockedApp
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {

    val repository = AppRepository(app)

    // Exposed as StateFlow so the UI reacts to DB changes automatically
    val blockedApps: StateFlow<List<BlockedApp>> = repository.observeBlockedApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addApp(packageName: String) = viewModelScope.launch {
        repository.addApp(packageName)
        repository.invalidateCache()
    }

    fun removeApp(packageName: String) = viewModelScope.launch {
        repository.removeApp(packageName)
        repository.invalidateCache()
    }

    suspend fun getInstallableApps(excludePackages: Set<String>) =
        repository.getInstallableApps(excludePackages)

    fun syncUsageStats(packageName: String) = viewModelScope.launch {
        repository.syncUsageStats(packageName)
    }

    suspend fun getLast7DaysStats(packageName: String) =
        repository.getLast7DaysStats(packageName)
}
