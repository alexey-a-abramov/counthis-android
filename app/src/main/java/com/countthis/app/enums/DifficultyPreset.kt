package com.countthis.app.enums

enum class DifficultyPreset(
    val displayName: String,
    val minItems: Int,
    val maxItems: Int,
    val displayTime: Long
) {
    NOVICE("Novice", 3, 8, 4000L),
    BEGINNER("Beginner", 6, 15, 3000L),
    INTERMEDIATE("Intermediate", 12, 25, 2000L),
    ADVANCED("Advanced", 20, 40, 1500L),
    MACHINE("Machine", 30, 60, 1000L)
}
