package com.countthis.app.challenges

class CountdownHandler : ChallengeHandler {

    private var lives = 3
    private var correctAnswers = 0
    private var isFinished = false

    override fun onRoundComplete(isCorrect: Boolean): ChallengeStatus {
        if (isCorrect) {
            correctAnswers++
        } else {
            lives--
            if (lives <= 0) {
                isFinished = true
            }
        }

        return ChallengeStatus(
            isGameOver = isFinished,
            statusText = if (isFinished) "Game Over! Score: $correctAnswers" else "Lives: $lives | Score: $correctAnswers",
            finalScore = correctAnswers
        )
    }

    override fun getCurrentStatus(): String {
        return "Lives: $lives"
    }

    override fun isGameOver(): Boolean {
        return isFinished
    }

    override fun cleanup() {
        // No cleanup needed
    }
}
