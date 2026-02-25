package com.countthis.app.managers

import android.content.Context
import com.countthis.app.R
import com.countthis.app.enums.ColorTheme

/**
 * Manages theme-specific color resources for the app.
 * Maps ColorTheme enum values to their corresponding color resource IDs.
 */
class ThemeManager(private val context: Context) {
    private val prefsHelper = PreferencesHelper(context)

    /**
     * Get the currently selected theme from preferences.
     */
    fun getCurrentTheme(): ColorTheme {
        return prefsHelper.getColorTheme()
    }

    /**
     * Get background color resource ID for current theme.
     */
    fun getBackgroundColor(): Int {
        return when (getCurrentTheme()) {
            ColorTheme.DARK -> R.color.dark_background
            ColorTheme.DEFAULT -> R.color.default_background
            else -> R.color.default_background // Fallback for PASTEL and HIGH_CONTRAST
        }
    }

    /**
     * Get game container background color resource ID for current theme.
     */
    fun getGameContainerBgColor(): Int {
        return when (getCurrentTheme()) {
            ColorTheme.DARK -> R.color.dark_game_container_bg
            ColorTheme.DEFAULT -> R.color.default_game_container_bg
            else -> R.color.default_game_container_bg
        }
    }

    /**
     * Get settings background color resource ID for current theme.
     */
    fun getSettingsBackgroundColor(): Int {
        return when (getCurrentTheme()) {
            ColorTheme.DARK -> R.color.dark_settings_background
            ColorTheme.DEFAULT -> R.color.default_settings_background
            else -> R.color.default_settings_background
        }
    }

    /**
     * Get primary text color resource ID for current theme.
     */
    fun getTextPrimaryColor(): Int {
        return when (getCurrentTheme()) {
            ColorTheme.DARK -> R.color.dark_text_primary
            ColorTheme.DEFAULT -> R.color.default_text_primary
            else -> R.color.default_text_primary
        }
    }

    /**
     * Get secondary text color resource ID for current theme.
     */
    fun getTextSecondaryColor(): Int {
        return when (getCurrentTheme()) {
            ColorTheme.DARK -> R.color.dark_text_secondary
            ColorTheme.DEFAULT -> R.color.default_text_secondary
            else -> R.color.default_text_secondary
        }
    }

    /**
     * Get header background color resource ID for current theme.
     */
    fun getHeaderBgColor(): Int {
        return when (getCurrentTheme()) {
            ColorTheme.DARK -> R.color.dark_header_bg
            ColorTheme.DEFAULT -> R.color.default_header_bg
            else -> R.color.default_header_bg
        }
    }

    /**
     * Get header text color resource ID for current theme.
     */
    fun getHeaderTextColor(): Int {
        return when (getCurrentTheme()) {
            ColorTheme.DARK -> R.color.dark_header_text
            ColorTheme.DEFAULT -> R.color.default_header_text
            else -> R.color.default_header_text
        }
    }

    /**
     * Get ready button color resource ID for current theme.
     */
    fun getReadyButtonColor(): Int {
        return when (getCurrentTheme()) {
            ColorTheme.DARK -> R.color.dark_ready_button
            ColorTheme.DEFAULT -> R.color.default_ready_button
            else -> R.color.default_ready_button
        }
    }

    /**
     * Get ready button pressed color resource ID for current theme.
     */
    fun getReadyButtonPressedColor(): Int {
        return when (getCurrentTheme()) {
            ColorTheme.DARK -> R.color.dark_ready_button_pressed
            ColorTheme.DEFAULT -> R.color.default_ready_button_pressed
            else -> R.color.default_ready_button_pressed
        }
    }

    /**
     * Get status bar color resource ID for current theme.
     * Uses a slightly darker variant of the header background.
     */
    fun getStatusBarColor(): Int {
        return when (getCurrentTheme()) {
            ColorTheme.DARK -> R.color.purple_700 // Dark indigo for dark theme
            ColorTheme.DEFAULT -> R.color.purple_700 // Dark indigo for default theme
            else -> R.color.purple_700
        }
    }

    /**
     * Get answer button colors as an array of resource IDs for current theme.
     * Returns [answer_a, answer_b, answer_c, answer_d] in order.
     */
    fun getAnswerButtonColors(): IntArray {
        return when (getCurrentTheme()) {
            ColorTheme.DARK -> intArrayOf(
                R.color.dark_answer_a,
                R.color.dark_answer_b,
                R.color.dark_answer_c,
                R.color.dark_answer_d
            )
            ColorTheme.DEFAULT -> intArrayOf(
                R.color.default_answer_a,
                R.color.default_answer_b,
                R.color.default_answer_c,
                R.color.default_answer_d
            )
            else -> intArrayOf(
                R.color.default_answer_a,
                R.color.default_answer_b,
                R.color.default_answer_c,
                R.color.default_answer_d
            )
        }
    }

    /**
     * Get correct answer feedback color.
     * This color works on both light and dark backgrounds.
     */
    fun getCorrectColor(): Int {
        return R.color.correct_green
    }

    /**
     * Get wrong answer feedback color.
     * This color works on both light and dark backgrounds.
     */
    fun getWrongColor(): Int {
        return R.color.wrong_red
    }
}
