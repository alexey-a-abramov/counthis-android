package com.countthis.app.managers

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.countthis.app.enums.GameMode

class RecentGameModeHandler(context: Context) {
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun save(mode: GameMode) {
        prefs.edit().putString(KEY_RECENT_GAME_MODE, mode.name).apply()
    }

    fun getRecentMode(): GameMode {
        val value = prefs.getString(KEY_RECENT_GAME_MODE, GameMode.TRAINING.name)
            ?: GameMode.TRAINING.name
        return try {
            GameMode.valueOf(value)
        } catch (e: IllegalArgumentException) {
            GameMode.TRAINING
        }
    }

    companion object {
        private const val KEY_RECENT_GAME_MODE = "recent_game_mode"
    }
}
