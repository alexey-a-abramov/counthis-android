package com.countthis.app.data

import com.countthis.app.enums.GameMode

data class Statistics(
    val totalGames: Int = 0,
    val totalCorrect: Int = 0,
    val totalRounds: Int = 0,
    val totalTimePlayed: Long = 0L,
    val bestStreak: Int = 0,
    val sessions: List<GameSession> = emptyList(),
    val accuracyHistory: List<AccuracyPoint> = emptyList(),
    val personalRecords: Map<GameMode, PersonalRecord> = emptyMap()
)

data class AccuracyPoint(
    val timestamp: Long,
    val accuracy: Float
)

data class PersonalRecord(
    val mode: GameMode,
    val bestLevel: Int = 0,
    val bestAccuracy: Float = 0f,
    val bestStreak: Int = 0,
    val timestamp: Long = 0L
)
