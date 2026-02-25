package com.countthis.app.data

import com.countthis.app.enums.GameMode
import com.countthis.app.enums.PatternMode
import com.countthis.app.enums.DifficultyPreset

data class GameSession(
    val sessionId: String,
    val mode: GameMode,
    val patternMode: PatternMode,
    val startLevel: DifficultyPreset,
    val startTime: Long,
    val endTime: Long,
    val totalRounds: Int,
    val correctAnswers: Int,
    val finalLevel: Int,
    val maxStreak: Int,
    val adaptiveDifficultyUsed: Boolean
)
