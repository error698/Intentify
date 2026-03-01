# DistractBlock — Android (Optimized)

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
│  • In-memory blocked-app cache (O(1) lookup)        │
│  • Coroutine scope for non-blocking DB checks       │
├─────────────────────────────────────────────────────┤
│  Data Layer                                         │
│  AppRepository (single source of truth)             │
│  AppDatabase (Room)                                 │
│  ├── blocked_apps  (packageName, name, open counts) │
│  └── usage_stats   (per-day minutes + open counts)  │
└─────────────────────────────────────────────────────┘
```

## What's optimized vs v1

| Area | Before | After |
|---|---|---|
| Storage | SharedPreferences (slow, string parsing) | Room DB (SQLite, type-safe, reactive) |
| UI updates | Manual refresh | StateFlow + collect (auto-reacts to DB) |
| Service blocking check | DB hit on every event | In-memory Set cache, DB only on cache miss |
| Event debounce | None (could fire 10x/sec) | 2s debounce per package |
| Usage data | Hardcoded mock | Real UsageStatsManager API |
| Open counting | Not persisted | Persisted in DB, day-rollover aware |
| Threading | Main thread | Coroutines on Dispatchers.IO |
| ViewModel | None | AndroidViewModel with viewModelScope |

## Setup

1. Open in Android Studio (Hedgehog or newer)
2. Sync Gradle
3. Run on a real device (API 26+)
4. Grant **two** permissions on first launch:
   - **Accessibility Service**: Settings > Accessibility > DistractBlock
   - **Usage Access**: Settings > Apps > Special app access > Usage access
5. Tap **+** to add apps to block
6. Open a blocked app from your home screen — splash fires immediately

## Key files

| File | Purpose |
|---|---|
| `BlockerAccessibilityService.kt` | Intercepts app launches |
| `AppRepository.kt` | All data logic, UsageStatsManager sync |
| `AppDao.kt` | Room queries |
| `MainViewModel.kt` | UI state management |
| `SplashBlockActivity.kt` | The 5-second pause screen |
