package ee.ut.cs.iotbazaar.theme

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

/**
 * Helper object for managing application theme preferences (Dark/Light mode).
 * Persists the user's choice in SharedPreferences.
 */
object ThemePreferences {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_NIGHT_MODE = "night_mode"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Checks if a theme preference has been saved.
     */
    fun hasSavedMode(context: Context): Boolean =
        prefs(context).contains(KEY_NIGHT_MODE)

    /**
     * Retrieves the saved theme mode.
     * @return The saved mode (e.g., AppCompatDelegate.MODE_NIGHT_YES) or null if not set.
     */
    fun getSavedMode(context: Context): Int? =
        if (hasSavedMode(context)) prefs(context).getInt(KEY_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) else null

    /**
     * Applies the saved theme mode to the application.
     * Should be called in Activity.onCreate() before setContentView().
     */
    fun applySavedMode(context: Context) {
        val mode = getSavedMode(context)
        if (mode != null) AppCompatDelegate.setDefaultNightMode(mode)
    }

    /**
     * Sets and saves the dark mode preference.
     * @param enabled True to enable dark mode, false for light mode.
     */
    fun setDarkEnabled(context: Context, enabled: Boolean) {
        val mode = if (enabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        prefs(context).edit().putInt(KEY_NIGHT_MODE, mode).apply()
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    /**
     * Checks if dark mode is currently enabled.
     * Considers both the saved preference and the system configuration if no preference is saved.
     */
    fun isDarkEnabled(context: Context): Boolean {
        return when (getSavedMode(context)) {
            AppCompatDelegate.MODE_NIGHT_YES -> true
            AppCompatDelegate.MODE_NIGHT_NO -> false
            else -> {
                val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                uiMode == Configuration.UI_MODE_NIGHT_YES
            }
        }
    }
}
