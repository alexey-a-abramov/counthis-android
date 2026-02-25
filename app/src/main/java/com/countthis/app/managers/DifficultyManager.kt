package com.countthis.app.managers

import java.util.ArrayDeque

class DifficultyManager(
    private val isAdaptiveEnabled: Boolean
) {
    private val recentAnswers = ArrayDeque<Boolean>(10)
    private var hasAdjusted = false

    fun recordAnswer(isCorrect: Boolean) {
        if (!isAdaptiveEnabled) return

        if (recentAnswers.size >= 10) {
            recentAnswers.removeFirst()
        }
        recentAnswers.addLast(isCorrect)
    }

    fun shouldAdjustDifficulty(isCorrect: Boolean): Boolean {
        if (!isAdaptiveEnabled || isCorrect || hasAdjusted) return false

        if (recentAnswers.size < 10) return false

        val correctCount = recentAnswers.count { it }
        val accuracy = correctCount.toFloat() / 10f

        return accuracy < 0.6f
    }

    fun adjustDifficulty(currentDisplayTime: Long, currentMaxItems: Int): Pair<Long, Int> {
        hasAdjusted = true
        val newDisplayTime = (currentDisplayTime * 1.1).toLong()
        val newMaxItems = maxOf(3, currentMaxItems - 2)
        return Pair(newDisplayTime, newMaxItems)
    }

    fun reset() {
        recentAnswers.clear()
        hasAdjusted = false
    }
}
