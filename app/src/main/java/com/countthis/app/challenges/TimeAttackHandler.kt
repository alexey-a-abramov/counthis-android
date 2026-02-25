package com.countthis.app.challenges

import android.os.CountDownTimer

class TimeAttackHandler(
    private val onTimerUpdate: (secondsRemaining: Int) -> Unit
) : ChallengeHandler {

    private var correctAnswers = 0
    private var isFinished = false
    private var hasStarted = false
    private var timer: CountDownTimer? = null

    override fun start() {
        if (hasStarted || isFinished) return
        hasStarted = true
        startTimer()
    }

    private fun startTimer() {
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000).toInt()
                onTimerUpdate(secondsRemaining)
            }

            override fun onFinish() {
                isFinished = true
                onTimerUpdate(0)
            }
        }.start()
    }

    override fun onRoundComplete(isCorrect: Boolean): ChallengeStatus {
        if (isCorrect) {
            correctAnswers++
        }

        return ChallengeStatus(
            isGameOver = isFinished,
            statusText = if (isFinished) "Time's up! Score: $correctAnswers" else "$correctAnswers correct",
            finalScore = correctAnswers
        )
    }

    override fun getCurrentStatus(): String {
        return "$correctAnswers correct"
    }

    override fun isGameOver(): Boolean {
        return isFinished
    }

    override fun cleanup() {
        timer?.cancel()
    }
}
