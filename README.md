# Intentify — Mindful App Blocker for Android

[![Download APK](https://img.shields.io/badge/Download-APK%20(v1.0.0)-34D399?style=for-the-badge&logo=android&logoColor=white)](https://github.com/error698/Intentify/releases/latest/download/Intentify-v1.0.apk)
[![Latest Release](https://img.shields.io/github/v/release/error698/Intentify?style=for-the-badge&color=38BDF8)](https://github.com/error698/Intentify/releases/latest)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-lightgrey?style=for-the-badge&logo=android)](https://github.com/error698/Intentify)

**Intentify** introduces mindful friction before you open distracting apps. Instead of mindless scrolling, take a 5-second pause to reflect on your intent before proceeding.

---

## 📥 Download & Installation

### Option 1: Direct APK Download
1. Tap the button below to download the latest APK:
   
   👉 **[⬇️ Download Intentify-v1.0.apk](https://github.com/error698/Intentify/releases/latest/download/Intentify-v1.0.apk)**  
   *(Or browse all versions on the [Releases Page](https://github.com/error698/Intentify/releases))*

2. Open the downloaded `.apk` on your Android phone and install it (allow *"Install from unknown sources"* if prompted).
3. On first launch, grant the required permissions:
   - **Accessibility Service**: Enables detection when a blocked app is opened.
   - **Display Over Other Apps**: Allows the mindful pause splash screen to appear over blocked apps.
   - **Usage Access**: Enables 7-day screen time and daily open count analytics.

> [!NOTE]
> **Android 13, 14 & 15 Notice:**
> If the Accessibility toggle shows *"Restricted setting"*:  
> Go to **Phone Settings > Apps > Intentify > tap the 3 dots in the top-right corner > Allow restricted settings**. Then toggle Accessibility **ON**.

---

## Architecture

```
┌─────────────────────────────────────────────────────┐
│  UI Layer                                           │
│  MainActivity (ViewModel + StateFlow)               │
│  SplashBlockActivity (CountDownTimer + Coroutines)  │
├─────────────────────────────────────────────────────┤
│  Service Layer                                      │
│  BlockerAccessibilityService                        │
│  • Debounced event handling                         │
│  • Reactive Room Flow cache sync (O(1) lookup)      │
│  • Coroutine scope for non-blocking DB checks       │
├─────────────────────────────────────────────────────┤
│  Data Layer                                         │
│  AppRepository (Singleton, single source of truth)  │
│  AppDatabase (Room SQLite)                          │
│  ├── blocked_apps  (packageName, name, open counts) │
│  └── usage_stats   (per-day minutes + open counts)  │
└─────────────────────────────────────────────────────┘
```

## What's optimized vs v1

| Area | Before | After |
|---|---|---|
| Storage | SharedPreferences (slow, string parsing) | Room DB (SQLite, type-safe, reactive) |
| UI updates | Manual refresh | StateFlow + collect (auto-reacts to DB) |
| Service blocking check | DB hit on every event | Reactive in-memory Set cache, auto-synced with Room |
| Event debounce | None (could fire 10x/sec) | 1.5s debounce per package |
| Usage data | Hardcoded mock | Real UsageStatsManager API |
| Open counting | Not persisted | Persisted in DB, day-rollover aware |
| Threading | Main thread | Coroutines on Dispatchers.IO |
| ViewModel | None | AndroidViewModel with viewModelScope |

## Building from Source

1. Clone this repository:
   ```bash
   git clone https://github.com/error698/Intentify.git
   cd Intentify
   ```
2. Build the debug APK using the Gradle wrapper:
   ```powershell
   .\gradlew.bat assembleDebug
   ```
3. The compiled APK will be output to:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```
4. Install directly to a connected phone:
   ```powershell
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

## Key files

| File | Purpose |
|---|---|
| `BlockerAccessibilityService.kt` | Intercepts app launches |
| `AppRepository.kt` | All data logic, UsageStatsManager sync |
| `AppDao.kt` | Room queries |
| `MainViewModel.kt` | UI state management |
| `SplashBlockActivity.kt` | The 5-second pause screen |
