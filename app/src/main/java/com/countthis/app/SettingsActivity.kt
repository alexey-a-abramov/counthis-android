package com.countthis.app

import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.countthis.app.databinding.ActivitySettingsBinding
import com.countthis.app.managers.ThemeManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        themeManager = ThemeManager(this)

        // Apply theme to UI
        applyTheme()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }
    }

    /**
     * Apply theme colors to UI elements.
     */
    private fun applyTheme() {
        // Set status bar color
        window.statusBarColor = ContextCompat.getColor(this, themeManager.getStatusBarColor())

        // Apply background color
        binding.root.setBackgroundColor(
            ContextCompat.getColor(this, themeManager.getSettingsBackgroundColor())
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        private val preferenceChangeListener =
            SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
                when (key) {
                    "color_theme" -> {
                        // Recreate the activity to apply the new theme
                        activity?.recreate()
                    }
                    "answer_range_percent" -> {
                        updateAnswerRangeSummary()
                    }
                }
            }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
            bindAboutVersion()
            updateAnswerRangeSummary()
            setupThemePreview()
        }

        override fun onResume() {
            super.onResume()
            // Register preference change listener
            preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(
                preferenceChangeListener
            )
        }

        override fun onPause() {
            super.onPause()
            // Unregister preference change listener
            preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(
                preferenceChangeListener
            )
        }

        private fun bindAboutVersion() {
            val aboutPreference = findPreference<Preference>("about_app") ?: return
            val packageInfo = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requireContext().packageManager.getPackageInfo(
                        requireContext().packageName,
                        PackageManager.PackageInfoFlags.of(0)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
                }
            } catch (_: Exception) {
                null
            }

            val versionName = packageInfo?.versionName ?: "21"
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo?.longVersionCode?.toInt() ?: 21
            } else {
                @Suppress("DEPRECATION")
                packageInfo?.versionCode ?: 21
            }

            aboutPreference.summary = getString(R.string.version_format, versionName, versionCode)
        }

        private fun updateAnswerRangeSummary() {
            val rangePreference = findPreference<androidx.preference.SeekBarPreference>("answer_range_percent")
            rangePreference?.let { pref ->
                val currentValue = pref.value
                pref.summary = getString(R.string.answer_range_percent_summary, currentValue)
            }
        }

        private fun setupThemePreview() {
            val themePreference = findPreference<androidx.preference.ListPreference>("color_theme")
            themePreference?.setOnPreferenceChangeListener { _, newValue ->
                val themeName = when (newValue.toString()) {
                    "DEFAULT" -> "Default (Blue & Purple)"
                    "DARK" -> "Dark (Charcoal & Teal)"
                    "PASTEL" -> "Pastel (Soft Colors)"
                    "HIGH_CONTRAST" -> "High Contrast (Black & Yellow)"
                    else -> newValue.toString()
                }
                themePreference.summary = themeName
                true
            }

            // Set initial summary
            val currentTheme = themePreference?.value ?: "DEFAULT"
            val themeName = when (currentTheme) {
                "DEFAULT" -> "Default (Blue & Purple)"
                "DARK" -> "Dark (Charcoal & Teal)"
                "PASTEL" -> "Pastel (Soft Colors)"
                "HIGH_CONTRAST" -> "High Contrast (Black & Yellow)"
                else -> currentTheme
            }
            themePreference?.summary = themeName
        }
    }
}
