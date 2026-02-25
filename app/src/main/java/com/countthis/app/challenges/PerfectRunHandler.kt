package com.countthis.app.challenges

class PerfectRunHandler : ChallengeHandler {

    private var streak = 0
    private var isFinished = false

    override fun onRoundComplete(isCorrect: Boolean): ChallengeStatus {
        if (isCorrect) {
            streak++
            return ChallengeStatus(
                isGameOver = false,
                statusText = "Perfect Streak: $streak",
                finalScore = streak
            )
        } else {
            isFinished = true
            return ChallengeStatus(
                isGameOver = true,
                statusText = "Game Over! Final Streak: $streak",
                finalScore = streak
            )
        }
    }

    override fun getCurrentStatus(): String {
        return "Streak: $streak"
    }

    override fun isGameOver(): Boolean {
        return isFinished
    }

    override fun cleanup() {
        // No cleanup needed
    }
}
