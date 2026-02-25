package com.countthis.app.challenges

interface ChallengeHandler {
    fun start() {}
    fun onRoundComplete(isCorrect: Boolean): ChallengeStatus
    fun getCurrentStatus(): String
    fun isGameOver(): Boolean
    fun cleanup()
}

data class ChallengeStatus(
    val isGameOver: Boolean,
    val statusText: String,
    val finalScore: Int = 0
)
