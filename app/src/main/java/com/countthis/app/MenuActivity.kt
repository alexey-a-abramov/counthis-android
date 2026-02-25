package com.countthis.app

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.countthis.app.databinding.ActivityMenuBinding
import com.countthis.app.enums.DifficultyPreset
import com.countthis.app.enums.GameMode
import com.countthis.app.managers.PreferencesHelper
import com.countthis.app.managers.StatisticsManager
import com.countthis.app.managers.ThemeManager

class MenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMenuBinding
    private lateinit var prefsHelper: PreferencesHelper
    private lateinit var statsManager: StatisticsManager
    private lateinit var themeManager: ThemeManager

    private val presets = DifficultyPreset.values()
    private var selectedPreset = DifficultyPreset.BEGINNER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefsHelper = PreferencesHelper(this)
        statsManager = StatisticsManager(this)
        themeManager = ThemeManager(this)

        // Apply theme to UI
        applyTheme()

        setupLevelSpinner()
        setupGameTypeButtons()
        setupBottomButtons()
        updateLastSession()
    }

    override fun onResume() {
        super.onResume()
        updateLastSession()
    }

    /**
     * Apply theme colors to all UI elements.
     */
    private fun applyTheme() {
        // Set status bar color
        window.statusBarColor = ContextCompat.getColor(this, themeManager.getStatusBarColor())

        // Apply background color to root layout
        binding.root.setBackgroundColor(
            ContextCompat.getColor(this, themeManager.getBackgroundColor())
        )

        // Apply header colors
        binding.topBar.setBackgroundColor(
            ContextCompat.getColor(this, themeManager.getHeaderBgColor())
        )
        binding.titleText.setTextColor(
            ContextCompat.getColor(this, themeManager.getHeaderTextColor())
        )

        // Apply game mode button colors
        val answerColors = themeManager.getAnswerButtonColors()
        binding.trainingButton.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(this, answerColors[0])
        )
        binding.timeAttackButton.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(this, answerColors[1])
        )
        binding.perfectRunButton.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(this, answerColors[2])
        )
        binding.countdownButton.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(this, answerColors[3])
        )

        // Apply statistics button color
        binding.statisticsButton.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(this, themeManager.getHeaderBgColor())
        )
    }

    private fun setupLevelSpinner() {
        val names = presets.map { it.displayName }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.levelSpinner.adapter = adapter
        // Default to BEGINNER (index 1)
        binding.levelSpinner.setSelection(presets.indexOfFirst { it == DifficultyPreset.BEGINNER })

        binding.levelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedPreset = presets[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupGameTypeButtons() {
        binding.trainingButton.setOnClickListener {
            startGame(GameMode.TRAINING)
        }
        binding.timeAttackButton.setOnClickListener {
            startGame(GameMode.TIME_ATTACK)
        }
        binding.perfectRunButton.setOnClickListener {
            startGame(GameMode.PERFECT_RUN)
        }
        binding.countdownButton.setOnClickListener {
            startGame(GameMode.COUNTDOWN)
        }
    }

    private fun setupBottomButtons() {
        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.statisticsButton.setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }
    }

    private fun startGame(mode: GameMode) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("GAME_MODE", mode.name)
            putExtra("DIFFICULTY_PRESET", selectedPreset.name)
            putExtra("PATTERN_MODE", prefsHelper.getDefaultPatternMode().name)
        }
        startActivity(intent)
    }

    private fun updateLastSession() {
        val stats = statsManager.getStatistics()
        val last = stats.sessions.lastOrNull()

        if (last == null) {
            binding.lastSessionCard.visibility = View.GONE
            return
        }

        binding.lastSessionCard.visibility = View.VISIBLE

        val modeName = when (last.mode) {
            GameMode.TRAINING -> "Training"
            GameMode.TIME_ATTACK -> "Time Attack"
            GameMode.PERFECT_RUN -> "Perfect Run"
            GameMode.COUNTDOWN -> "Countdown"
        }
        val accuracy = if (last.totalRounds > 0) {
            last.correctAnswers.toFloat() / last.totalRounds.toFloat() * 100f
        } else 0f

        binding.lastSessionModeText.text = "$modeName  ·  ${last.startLevel.displayName}"
        binding.lastSessionScoreText.text = "${last.correctAnswers}/${last.totalRounds}  (%.0f%%)".format(accuracy)
        binding.lastSessionStreakText.text = "Streak: ${last.maxStreak}"
        binding.lastSessionLevelText.text = "Reached level ${last.finalLevel}"
    }
}
