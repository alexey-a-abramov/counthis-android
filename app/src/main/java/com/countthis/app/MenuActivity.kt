package com.countthis.app

import android.content.Intent
import android.graphics.Color
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.countthis.app.databinding.ActivityMenuBinding
import com.countthis.app.enums.DifficultyPreset
import com.countthis.app.enums.GameMode
import com.countthis.app.managers.PreferencesHelper
import com.countthis.app.managers.RecentGameModeHandler
import com.countthis.app.managers.StatisticsManager
import com.countthis.app.managers.ThemeManager

class MenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMenuBinding
    private lateinit var prefsHelper: PreferencesHelper
    private lateinit var recentModeHandler: RecentGameModeHandler
    private lateinit var statsManager: StatisticsManager
    private lateinit var themeManager: ThemeManager

    private val presets = DifficultyPreset.values()
    private var selectedPreset = DifficultyPreset.BEGINNER
    private var recentMode = GameMode.TRAINING
    private lateinit var modeButtonBaseColors: IntArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefsHelper = PreferencesHelper(this)
        recentModeHandler = RecentGameModeHandler(this)
        statsManager = StatisticsManager(this)
        themeManager = ThemeManager(this)
        selectedPreset = prefsHelper.getSelectedDifficultyPreset()
        recentMode = recentModeHandler.getRecentMode()
        modeButtonBaseColors = IntArray(4)

        // Apply theme to UI
        applyTheme()

        setupLevelSpinner()
        setupGameTypeButtons()
        setupMeditativeMenuToggle()
        setupBottomButtons()
        applyMenuMeditativeState(prefsHelper.isMeditativeModeEnabled())
        updateLastSession()

        // Auto-launch game if meditative mode is enabled and we're not returning from a session
        if (prefsHelper.isMeditativeModeEnabled() && intent.getBooleanExtra("EXIT_MEDITATIVE", false) != true) {
            onModeSelected(recentMode)
        }
    }

    override fun onResume() {
        super.onResume()
        selectedPreset = prefsHelper.getSelectedDifficultyPreset()
        recentMode = recentModeHandler.getRecentMode()
        binding.levelSpinner.setSelection(presets.indexOfFirst { it == selectedPreset })
        binding.menuMeditativeCheckbox.isChecked = prefsHelper.isMeditativeModeEnabled()
        applyMenuMeditativeState(binding.menuMeditativeCheckbox.isChecked)
        updateRecentModeHighlight()
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
        modeButtonBaseColors = IntArray(answerColors.size) { index ->
            ContextCompat.getColor(this, answerColors[index])
        }
        applyModeButtonSaturation(prefsHelper.isMeditativeModeEnabled())

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
        binding.levelSpinner.setSelection(presets.indexOfFirst { it == selectedPreset })

        binding.levelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedPreset = presets[position]
                prefsHelper.saveSelectedDifficultyPreset(selectedPreset)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupGameTypeButtons() {
        binding.trainingButton.setOnClickListener {
            onModeSelected(GameMode.TRAINING)
        }
        binding.timeAttackButton.setOnClickListener {
            onModeSelected(GameMode.TIME_ATTACK)
        }
        binding.perfectRunButton.setOnClickListener {
            onModeSelected(GameMode.PERFECT_RUN)
        }
        binding.countdownButton.setOnClickListener {
            onModeSelected(GameMode.COUNTDOWN)
        }
        updateRecentModeHighlight()
    }

    private fun setupMeditativeMenuToggle() {
        binding.menuMeditativeCheckbox.isChecked = prefsHelper.isMeditativeModeEnabled()
        binding.menuMeditativeCheckbox.setOnCheckedChangeListener { _, checked ->
            prefsHelper.setMeditativeModeEnabled(checked)
            applyMenuMeditativeState(checked)
            updateLastSession()
            if (checked) {
                onModeSelected(recentMode)
            }
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

    private fun onModeSelected(mode: GameMode) {
        recentMode = mode
        recentModeHandler.save(mode)
        updateRecentModeHighlight()
        startGame(mode)
    }

    private fun updateRecentModeHighlight() {
        val buttons = mapOf(
            GameMode.TRAINING to binding.trainingButton,
            GameMode.TIME_ATTACK to binding.timeAttackButton,
            GameMode.PERFECT_RUN to binding.perfectRunButton,
            GameMode.COUNTDOWN to binding.countdownButton
        )

        buttons.forEach { (mode, button) ->
            styleModeButton(button, mode == recentMode)
        }
    }

    private fun styleModeButton(button: Button, isRecent: Boolean) {
        button.alpha = if (isRecent) 1f else 0.88f
        button.scaleX = if (isRecent) 1.03f else 1f
        button.scaleY = if (isRecent) 1.03f else 1f
    }

    private fun applyMenuMeditativeState(enabled: Boolean) {
        binding.topBar.visibility = if (enabled) View.GONE else View.VISIBLE
        binding.settingsButton.visibility = if (enabled) View.GONE else View.VISIBLE
        binding.selectLevelLabel.visibility = if (enabled) View.GONE else View.VISIBLE
        binding.levelCard.visibility = if (enabled) View.GONE else View.VISIBLE
        binding.gameTypeLabel.visibility = if (enabled) View.GONE else View.VISIBLE
        binding.statisticsButton.visibility = if (enabled) View.GONE else View.VISIBLE

        if (enabled) {
            binding.lastSessionCard.visibility = View.GONE
        }

        applyModeButtonSaturation(enabled)
        updateRecentModeHighlight()
    }

    private fun applyModeButtonSaturation(meditative: Boolean) {
        val neutral = ColorUtils.blendARGB(
            ContextCompat.getColor(this, themeManager.getBackgroundColor()),
            Color.WHITE,
            0.08f
        )
        val saturation = if (meditative) 0.65f else 0f
        val colors = modeButtonBaseColors.map { color ->
            ColorUtils.blendARGB(color, neutral, saturation)
        }

        binding.trainingButton.backgroundTintList = ColorStateList.valueOf(colors[0])
        binding.timeAttackButton.backgroundTintList = ColorStateList.valueOf(colors[1])
        binding.perfectRunButton.backgroundTintList = ColorStateList.valueOf(colors[2])
        binding.countdownButton.backgroundTintList = ColorStateList.valueOf(colors[3])
    }

    private fun updateLastSession() {
        if (binding.menuMeditativeCheckbox.isChecked) {
            binding.lastSessionCard.visibility = View.GONE
            return
        }

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
