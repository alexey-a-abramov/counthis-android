package com.countthis.app.managers

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.countthis.app.enums.DifficultyPreset
import com.countthis.app.enums.PatternMode
import com.countthis.app.enums.ItemTheme
import com.countthis.app.enums.ColorTheme

class PreferencesHelper(context: Context) {
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun getDefaultPatternMode(): PatternMode {
        val value = prefs.getString("default_pattern_mode", "SCATTERED") ?: "SCATTERED"
        return try {
            PatternMode.valueOf(value)
        } catch (e: IllegalArgumentException) {
            PatternMode.SCATTERED
        }
    }

    fun getItemTheme(): ItemTheme {
        val value = prefs.getString("item_theme", "ANIMALS") ?: "ANIMALS"
        return try {
            ItemTheme.valueOf(value)
        } catch (e: IllegalArgumentException) {
            ItemTheme.ANIMALS
        }
    }

    fun getColorTheme(): ColorTheme {
        val value = prefs.getString("color_theme", "DEFAULT") ?: "DEFAULT"
        return try {
            ColorTheme.valueOf(value)
        } catch (e: IllegalArgumentException) {
            ColorTheme.DEFAULT
        }
    }

    fun getBackgroundStyle(): String {
        return prefs.getString("background_style", "plain") ?: "plain"
    }

    fun getAnswerRangePercent(): Int {
        return prefs.getInt("answer_range_percent", 25).coerceIn(10, 60)
    }

    fun isMeditativeModeEnabled(): Boolean {
        return prefs.getBoolean("meditative_mode", false)
    }

    fun saveSelectedDifficultyPreset(preset: DifficultyPreset) {
        prefs.edit().putString("selected_difficulty_preset", preset.name).apply()
    }

    fun getSelectedDifficultyPreset(): DifficultyPreset {
        val value = prefs.getString("selected_difficulty_preset", DifficultyPreset.BEGINNER.name)
            ?: DifficultyPreset.BEGINNER.name
        return try {
            DifficultyPreset.valueOf(value)
        } catch (e: IllegalArgumentException) {
            DifficultyPreset.BEGINNER
        }
    }
}
