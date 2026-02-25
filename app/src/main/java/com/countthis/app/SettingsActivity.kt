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
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == "color_theme") {
                    // Recreate the activity to apply the new theme
                    activity?.recreate()
                }
            }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
            bindAboutVersion()
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

            val versionName = packageInfo?.versionName
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo?.longVersionCode?.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo?.versionCode
            }

            aboutPreference.summary = if (!versionName.isNullOrBlank() && versionCode != null) {
                getString(R.string.version_format, versionName, versionCode)
            } else {
                getString(R.string.version_unknown)
            }
        }
    }
}
