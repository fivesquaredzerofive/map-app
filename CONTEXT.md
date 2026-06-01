# Interactive Maps App - Context Transfer

## Project Overview
This is an Android application built with Kotlin, Jetpack Compose, and MapLibre. It features a full-screen interactive map with local marker persistence and a Samsung One UI-inspired design aesthetic.

## Tech Stack
*   **UI Framework:** Jetpack Compose (Material3)
*   **Map SDK:** MapLibre Compose (Free OpenStreetMap vector tiles)
*   **Local Storage:** Room Database (SQLite) for markers, DataStore for settings
*   **Location Services:** Google Play Services Location (for GPS centering)
*   **Architecture:** MVVM with `ViewModel` and `StateFlow`
*   **Min SDK:** 26 (Android 8.0)
*   **Target SDK:** 35

## Key Features
1.  **Free Mapping:** Uses `demotiles.maplibre.org` meaning no API keys or billing setup is required.
2.  **GPS Centering:** Prompts for `ACCESS_FINE_LOCATION` on startup and animates the camera to the user's physical location.
3.  **Tap-to-Place Markers:** Tapping the map drops a marker which is instantly saved to the Room database.
4.  **One UI Design System:** Soft pastel colors, large rounded corners (28dp), large headers, and a clean minimalist aesthetic.
5.  **Navigation Drawer:** A side menu listing all saved markers (with their coordinates) and allowing deletion.
6.  **Settings & Custom Color Picker:** A custom "Roulette-style" color picker allowing users to select the default color for newly placed markers.

## Project Structure
*   `MainActivity.kt` - Main entry point and Navigation Host.
*   `MapApplication.kt` - Application class for initializing the Room Database and DataStore singletons.
*   `data/`
    *   `AppDatabase.kt` / `MarkerDao.kt` / `MarkerEntity.kt` - Room DB implementation.
    *   `SettingsDataStore.kt` - Preferences DataStore for the marker color setting.
*   `ui/`
    *   `screens/` - `MapScreen` (MapLibre rendering) and `SettingsScreen` (Color customization).
    *   `components/` - Reusable UI elements (`DrawerContent`, `MarkerListItem`, `RouletteColorPicker`).
    *   `viewmodel/` - `MapViewModel` and `SettingsViewModel`.
    *   `theme/` - Compose Material3 theme configured for Samsung One UI aesthetics.

## Current State & Next Steps
*   The project was scaffolded manually and all source files have been generated.
*   **To Build/Run:** Open the root folder in Android Studio. Gradle will automatically sync and download the MapLibre and Compose dependencies. No further manual configuration is needed.
