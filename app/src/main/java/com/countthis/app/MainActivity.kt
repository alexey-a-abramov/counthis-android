package com.countthis.app

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.countthis.app.challenges.ChallengeHandler
import com.countthis.app.challenges.CountdownHandler
import com.countthis.app.challenges.PerfectRunHandler
import com.countthis.app.challenges.TimeAttackHandler
import com.countthis.app.data.GameSession
import com.countthis.app.databinding.ActivityMainBinding
import com.countthis.app.enums.DifficultyPreset
import com.countthis.app.enums.GameMode
import com.countthis.app.enums.PatternMode
import com.countthis.app.managers.PreferencesHelper
import com.countthis.app.managers.StatisticsManager
import com.countthis.app.managers.ThemeManager
import com.countthis.app.rendering.ItemRenderer
import java.util.UUID
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val handler = Handler(Looper.getMainLooper())
    private var currentItemCount = 0
    private var correctAnswers = 0
    private var totalRounds = 0

    // Multi-mode support
    private var gameMode: GameMode = GameMode.TRAINING
    private var patternMode: PatternMode = PatternMode.SCATTERED
    private var startingPreset: DifficultyPreset = DifficultyPreset.BEGINNER
    private var challengeHandler: ChallengeHandler? = null
    private lateinit var itemRenderer: ItemRenderer
    private lateinit var statsManager: StatisticsManager
    private lateinit var prefsHelper: PreferencesHelper
    private lateinit var themeManager: ThemeManager
    private var currentStreak = 0
    private var maxStreak = 0
    private var sessionStartTime = 0L
    private var challengeStarted = false

    // Progressive difficulty variables
    private var currentLevel = 1
    private var currentDisplayTime = 3000L // milliseconds
    private var currentMaxItems = 15
    private var baseMinItems = 6
    private var baseDisplayTime = 3000L
    private var baseMaxItems = 15

    // Less-aggressive progression: track how many correct since last item bump
    private var correctSinceItemIncrease = 0

    private var displayTimerRunnable: Runnable? = null

    // Per-button default colours for reset after feedback (populated from theme in onCreate)
    private lateinit var buttonColorRes: IntArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        itemRenderer = ItemRenderer(this)
        prefsHelper = PreferencesHelper(this)
        statsManager = StatisticsManager(this)
        themeManager = ThemeManager(this)

        // Apply theme to UI
        applyTheme()

        // Initialize button colors from theme
        buttonColorRes = themeManager.getAnswerButtonColors()

        readGameModeFromIntent()
        initializeChallengeHandler()
        loadBaseSettings()

        sessionStartTime = System.currentTimeMillis()

        binding.startButton.setOnClickListener { startNewRound() }
        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.readyButton.setOnClickListener { onReadyButtonClicked() }

        binding.answer1Button.setOnClickListener { checkAnswer(1) }
        binding.answer2Button.setOnClickListener { checkAnswer(2) }
        binding.answer3Button.setOnClickListener { checkAnswer(3) }
        binding.answer4Button.setOnClickListener { checkAnswer(4) }

        updateDisplays()
        updateChallengeUI()
    }

    /**
     * Apply theme colors to all UI elements.
     */
    private fun applyTheme() {
        // Set status bar color
        window.statusBarColor = ContextCompat.getColor(this, themeManager.getStatusBarColor())

        // Apply background colors
        binding.root.setBackgroundColor(
            ContextCompat.getColor(this, themeManager.getBackgroundColor())
        )
        binding.gameContainer.setBackgroundColor(
            ContextCompat.getColor(this, themeManager.getGameContainerBgColor())
        )

        // Apply header colors
        binding.topBar.setBackgroundColor(
            ContextCompat.getColor(this, themeManager.getHeaderBgColor())
        )

        // Apply text colors
        binding.scoreText.setTextColor(
            ContextCompat.getColor(this, themeManager.getTextPrimaryColor())
        )
        binding.streakText.setTextColor(
            ContextCompat.getColor(this, themeManager.getTextPrimaryColor())
        )
        binding.levelText.setTextColor(
            ContextCompat.getColor(this, themeManager.getTextPrimaryColor())
        )
        binding.timerText.setTextColor(
            ContextCompat.getColor(this, themeManager.getTextPrimaryColor())
        )
        binding.challengeStatusText.setTextColor(
            ContextCompat.getColor(this, themeManager.getTextPrimaryColor())
        )
        binding.instructionText.setTextColor(
            ContextCompat.getColor(this, themeManager.getTextPrimaryColor())
        )

        // Apply ready button colors using color filter
        binding.readyButton.background.setColorFilter(
            ContextCompat.getColor(this, themeManager.getReadyButtonColor()),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
    }

    private fun readGameModeFromIntent() {
        val modeStr = intent.getStringExtra("GAME_MODE")
        val presetStr = intent.getStringExtra("DIFFICULTY_PRESET")
        val patternStr = intent.getStringExtra("PATTERN_MODE")

        gameMode = try {
            GameMode.valueOf(modeStr ?: "TRAINING")
        } catch (e: IllegalArgumentException) {
            GameMode.TRAINING
        }

        startingPreset = try {
            DifficultyPreset.valueOf(presetStr ?: "BEGINNER")
        } catch (e: IllegalArgumentException) {
            DifficultyPreset.BEGINNER
        }

        patternMode = try {
            PatternMode.valueOf(patternStr ?: prefsHelper.getDefaultPatternMode().name)
        } catch (e: IllegalArgumentException) {
            prefsHelper.getDefaultPatternMode()
        }
    }

    private fun initializeChallengeHandler() {
        challengeHandler = when (gameMode) {
            GameMode.TIME_ATTACK -> TimeAttackHandler { seconds ->
                runOnUiThread {
                    binding.timerText.text = "${seconds}s"
                    if (seconds == 0) endGame()
                }
            }
            GameMode.PERFECT_RUN -> PerfectRunHandler()
            GameMode.COUNTDOWN -> CountdownHandler()
            GameMode.TRAINING -> null
        }
    }

    private fun updateChallengeUI() {
        when (gameMode) {
            GameMode.TIME_ATTACK -> {
                binding.timerText.text = "60s"
                binding.timerText.visibility = View.VISIBLE
                binding.streakText.visibility = View.GONE
                binding.challengeStatusText.visibility = View.GONE
            }
            GameMode.PERFECT_RUN -> {
                binding.timerText.visibility = View.GONE
                binding.streakText.visibility = View.VISIBLE
                binding.challengeStatusText.visibility = View.GONE
            }
            GameMode.COUNTDOWN -> {
                binding.timerText.visibility = View.GONE
                binding.streakText.visibility = View.GONE
                binding.challengeStatusText.visibility = View.VISIBLE
            }
            GameMode.TRAINING -> {
                binding.timerText.visibility = View.GONE
                binding.streakText.visibility = View.VISIBLE
                binding.challengeStatusText.visibility = View.GONE
            }
        }
    }

    private fun loadBaseSettings() {
        baseMinItems = startingPreset.minItems
        baseMaxItems = startingPreset.maxItems
        baseDisplayTime = startingPreset.displayTime

        currentDisplayTime = baseDisplayTime
        currentMaxItems = baseMaxItems
        currentLevel = 1
        correctSinceItemIncrease = 0
    }

    override fun onResume() {
        super.onResume()
        loadBaseSettings()
        updateDisplays()
    }

    private fun startNewRound() {
        if (!challengeStarted) {
            challengeHandler?.start()
            challengeStarted = true
        }

        binding.startButton.visibility = View.GONE
        binding.answerContainer.visibility = View.GONE
        binding.readyButton.visibility = View.GONE
        binding.gameContainer.removeAllViews()

        val effectiveMaxItems = maxOf(baseMinItems, currentMaxItems)
        currentItemCount = Random.nextInt(baseMinItems, effectiveMaxItems + 1)

        showInstruction()

        handler.postDelayed({
            displayItems()
            binding.readyButton.visibility = View.VISIBLE

            displayTimerRunnable = Runnable { hideItemsAndShowOptions() }
            handler.postDelayed(displayTimerRunnable!!, currentDisplayTime)
        }, 1500)
    }

    private fun onReadyButtonClicked() {
        displayTimerRunnable?.let { handler.removeCallbacks(it) }
        binding.readyButton.visibility = View.GONE
        hideItemsAndShowOptions()
    }

    private fun showInstruction() {
        binding.instructionText.text = getString(R.string.count_them)
        binding.instructionText.visibility = View.VISIBLE
    }

    private fun displayItems() {
        binding.instructionText.visibility = View.GONE
        val itemTheme = prefsHelper.getItemTheme()
        itemRenderer.renderItems(binding.gameContainer, currentItemCount, itemTheme, patternMode)
    }

    private fun hideItemsAndShowOptions() {
        binding.gameContainer.removeAllViews()
        binding.readyButton.visibility = View.GONE

        val answers = generateAnswers()

        binding.answer1Button.text = "A   ${answers[0]}"
        binding.answer2Button.text = "B   ${answers[1]}"
        binding.answer3Button.text = "C   ${answers[2]}"
        binding.answer4Button.text = "D   ${answers[3]}"

        binding.answer1Button.tag = answers[0]
        binding.answer2Button.tag = answers[1]
        binding.answer3Button.tag = answers[2]
        binding.answer4Button.tag = answers[3]

        resetButtonColors()
        binding.answerContainer.visibility = View.VISIBLE
    }

    /**
     * Generates answer options for counting exercises using an adaptive algorithm
     * that prevents sequential runs and ensures uniform distribution of the correct answer.
     *
     * Algorithm features:
     * - Adaptive offset range based on correct count
     * - Prevents consecutive sequences like [5, 6, 7, 8]
     * - Uniformly randomizes correct answer position
     * - Retry mechanism with gradual range expansion
     *
     * @param numOptions Total number of answer choices (default 4)
     * @param maxAttempts Maximum retry attempts (default 200)
     * @return List of shuffled answer options including the correct answer
     */
    private fun generateAnswers(numOptions: Int = 4, maxAttempts: Int = 200): List<Int> {
        val correct = currentItemCount

        // Adaptive range: wider for small X so we can avoid sequential runs
        val baseOffset = when {
            correct <= 5 -> 4
            correct <= 15 -> 5
            correct <= 30 -> 6
            else -> maxOf(6, (correct * 0.15).toInt())
        }

        for (attempt in 0 until maxAttempts) {
            val maxOffset = baseOffset + (attempt / 30)  // expand range if stuck

            // Build pool of possible offsets (excluding 0 and values that would be < 1)
            val possibleOffsets = (-maxOffset..maxOffset)
                .filter { o -> o != 0 && (correct + o) >= 1 }
                .toList()

            if (possibleOffsets.size < numOptions - 1) continue

            // Sample offsets and build options
            val selectedOffsets = possibleOffsets.shuffled().take(numOptions - 1)
            val options = (selectedOffsets.map { correct + it } + correct).toMutableList()

            // KEY RULE: reject consecutive sequences (e.g. [5,6,7,8])
            // A run is consecutive iff spread == numOptions - 1
            val spread = options.maxOrNull()!! - options.minOrNull()!!
            if (spread <= numOptions - 1) continue

            // Shuffle to randomize position of correct answer
            return options.shuffled()
        }

        throw IllegalStateException("Could not generate valid options for correct=$correct")
    }

    private fun checkAnswer(buttonIndex: Int) {
        totalRounds++

        val selectedAnswer = when (buttonIndex) {
            1 -> binding.answer1Button.tag as Int
            2 -> binding.answer2Button.tag as Int
            3 -> binding.answer3Button.tag as Int
            else -> binding.answer4Button.tag as Int
        }

        val isCorrect = selectedAnswer == currentItemCount

        if (isCorrect) {
            correctAnswers++
            currentStreak++
            maxStreak = maxOf(maxStreak, currentStreak)
            highlightButton(buttonIndex, true)
            progressDifficulty()
        } else {
            currentStreak = 0
            highlightButton(buttonIndex, false)
            highlightCorrectAnswer()
        }

        val challengeStatus = challengeHandler?.onRoundComplete(isCorrect)
        if (challengeStatus?.isGameOver == true) {
            updateDisplays()
            setAnswerButtonsEnabled(false)
            handler.postDelayed({ endGame() }, 1000)
            return
        }

        updateDisplays()
        updateChallengeStatus()
        setAnswerButtonsEnabled(false)

        handler.postDelayed({
            setAnswerButtonsEnabled(true)
            binding.answerContainer.visibility = View.GONE
            startNewRound()
        }, 1000)
    }

    private fun setAnswerButtonsEnabled(enabled: Boolean) {
        binding.answer1Button.isEnabled = enabled
        binding.answer2Button.isEnabled = enabled
        binding.answer3Button.isEnabled = enabled
        binding.answer4Button.isEnabled = enabled
    }

    private fun updateChallengeStatus() {
        when (gameMode) {
            GameMode.PERFECT_RUN, GameMode.TRAINING -> {
                binding.streakText.text = "Streak: $currentStreak"
            }
            GameMode.COUNTDOWN -> {
                binding.challengeStatusText.text = challengeHandler?.getCurrentStatus() ?: ""
            }
            else -> {}
        }
    }

    private fun endGame() {
        val session = GameSession(
            sessionId = UUID.randomUUID().toString(),
            mode = gameMode,
            patternMode = patternMode,
            startLevel = startingPreset,
            startTime = sessionStartTime,
            endTime = System.currentTimeMillis(),
            totalRounds = totalRounds,
            correctAnswers = correctAnswers,
            finalLevel = currentLevel,
            maxStreak = maxStreak,
            adaptiveDifficultyUsed = false
        )
        statsManager.recordSession(session)
        showGameOverDialog()
    }

    private fun showGameOverDialog() {
        val accuracy = if (totalRounds > 0) {
            (correctAnswers.toFloat() / totalRounds.toFloat() * 100f)
        } else {
            0f
        }

        AlertDialog.Builder(this)
            .setTitle("Game Over!")
            .setMessage(
                "Score: $correctAnswers/$totalRounds\n" +
                "Accuracy: %.1f%%\n".format(accuracy) +
                "Final Level: $currentLevel\n" +
                "Best Streak: $maxStreak"
            )
            .setPositiveButton("Menu") { _, _ -> finish() }
            .setNeutralButton("Play Again") { _, _ -> restartGame() }
            .setNegativeButton("View Stats") { _, _ ->
                startActivity(Intent(this, StatisticsActivity::class.java))
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun restartGame() {
        correctAnswers = 0
        totalRounds = 0
        currentStreak = 0
        maxStreak = 0
        sessionStartTime = System.currentTimeMillis()
        challengeStarted = false
        loadBaseSettings()

        challengeHandler?.cleanup()
        initializeChallengeHandler()

        updateDisplays()
        updateChallengeUI()

        binding.startButton.visibility = View.VISIBLE
        binding.answerContainer.visibility = View.GONE
    }

    /**
     * Less aggressive progression:
     *  - Display time shrinks by 2% per correct answer (was 5%).
     *  - Max items grows by 1 every 3 correct answers (was every 1).
     */
    private fun progressDifficulty() {
        currentLevel++

        currentDisplayTime = (currentDisplayTime * 0.98).toLong()
        if (currentDisplayTime < 500) currentDisplayTime = 500

        correctSinceItemIncrease++
        if (correctSinceItemIncrease >= 3) {
            currentMaxItems++
            correctSinceItemIncrease = 0
        }
    }

    private fun highlightButton(buttonIndex: Int, correct: Boolean) {
        val button = when (buttonIndex) {
            1 -> binding.answer1Button
            2 -> binding.answer2Button
            3 -> binding.answer3Button
            else -> binding.answer4Button
        }
        val colorRes = if (correct) themeManager.getCorrectColor() else themeManager.getWrongColor()
        button.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, colorRes))
    }

    private fun highlightCorrectAnswer() {
        val buttons = listOf(
            binding.answer1Button,
            binding.answer2Button,
            binding.answer3Button,
            binding.answer4Button
        )
        for (button in buttons) {
            if ((button.tag as? Int) == currentItemCount) {
                button.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, themeManager.getCorrectColor()))
                break
            }
        }
    }

    private fun resetButtonColors() {
        val buttons = listOf(
            binding.answer1Button,
            binding.answer2Button,
            binding.answer3Button,
            binding.answer4Button
        )
        buttons.forEachIndexed { index, button ->
            button.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, buttonColorRes[index]))
        }
    }

    private fun updateDisplays() {
        binding.scoreText.text = getString(R.string.score, correctAnswers, totalRounds)
        val timeInSeconds = currentDisplayTime / 1000.0
        binding.levelText.text = "Level: $currentLevel | %.1fs | Max: $currentMaxItems".format(timeInSeconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        challengeHandler?.cleanup()
    }
}
