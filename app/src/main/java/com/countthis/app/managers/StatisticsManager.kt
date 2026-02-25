package com.countthis.app.managers

import android.content.Context
import android.content.SharedPreferences
import com.countthis.app.data.AccuracyPoint
import com.countthis.app.data.GameSession
import com.countthis.app.data.PersonalRecord
import com.countthis.app.data.Statistics
import com.countthis.app.enums.GameMode
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StatisticsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("statistics", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun recordSession(session: GameSession) {
        val stats = getStatistics()

        // Update aggregates
        val totalGames = stats.totalGames + 1
        val totalCorrect = stats.totalCorrect + session.correctAnswers
        val totalRounds = stats.totalRounds + session.totalRounds
        val totalTimePlayed = stats.totalTimePlayed + (session.endTime - session.startTime)
        val bestStreak = maxOf(stats.bestStreak, session.maxStreak)

        // Add to sessions list (keep last 50)
        val sessions = (stats.sessions + session).takeLast(50)

        // Add accuracy point
        val accuracy = if (session.totalRounds > 0) {
            (session.correctAnswers.toFloat() / session.totalRounds.toFloat()) * 100f
        } else {
            0f
        }
        val accuracyPoint = AccuracyPoint(session.endTime, accuracy)
        val accuracyHistory = (stats.accuracyHistory + accuracyPoint).takeLast(30)

        // Update personal records
        val personalRecords = updatePersonalRecords(stats.personalRecords, session, accuracy)

        // Save to preferences
        prefs.edit().apply {
            putInt("total_games", totalGames)
            putInt("total_correct", totalCorrect)
            putInt("total_rounds", totalRounds)
            putLong("total_time_played", totalTimePlayed)
            putInt("best_streak", bestStreak)
            putString("sessions_json", gson.toJson(sessions))
            putString("accuracy_history_json", gson.toJson(accuracyHistory))
            putString("personal_records_json", gson.toJson(personalRecords))
            apply()
        }
    }

    fun getStatistics(): Statistics {
        val totalGames = prefs.getInt("total_games", 0)
        val totalCorrect = prefs.getInt("total_correct", 0)
        val totalRounds = prefs.getInt("total_rounds", 0)
        val totalTimePlayed = prefs.getLong("total_time_played", 0L)
        val bestStreak = prefs.getInt("best_streak", 0)

        val sessionsJson = prefs.getString("sessions_json", "[]") ?: "[]"
        val sessions: List<GameSession> = try {
            gson.fromJson(sessionsJson, object : TypeToken<List<GameSession>>() {}.type)
        } catch (e: Exception) {
            emptyList()
        }

        val accuracyJson = prefs.getString("accuracy_history_json", "[]") ?: "[]"
        val accuracyHistory: List<AccuracyPoint> = try {
            gson.fromJson(accuracyJson, object : TypeToken<List<AccuracyPoint>>() {}.type)
        } catch (e: Exception) {
            emptyList()
        }

        val recordsJson = prefs.getString("personal_records_json", "{}") ?: "{}"
        val personalRecords: Map<GameMode, PersonalRecord> = try {
            gson.fromJson(recordsJson, object : TypeToken<Map<GameMode, PersonalRecord>>() {}.type)
        } catch (e: Exception) {
            emptyMap()
        }

        return Statistics(
            totalGames = totalGames,
            totalCorrect = totalCorrect,
            totalRounds = totalRounds,
            totalTimePlayed = totalTimePlayed,
            bestStreak = bestStreak,
            sessions = sessions,
            accuracyHistory = accuracyHistory,
            personalRecords = personalRecords
        )
    }

    fun getAccuracyHistory(): List<AccuracyPoint> {
        return getStatistics().accuracyHistory
    }

    private fun updatePersonalRecords(
        currentRecords: Map<GameMode, PersonalRecord>,
        session: GameSession,
        accuracy: Float
    ): Map<GameMode, PersonalRecord> {
        val records = currentRecords.toMutableMap()
        val currentRecord = records[session.mode]

        val shouldUpdate = currentRecord == null ||
                session.finalLevel > currentRecord.bestLevel ||
                accuracy > currentRecord.bestAccuracy ||
                session.maxStreak > currentRecord.bestStreak

        if (shouldUpdate) {
            records[session.mode] = PersonalRecord(
                mode = session.mode,
                bestLevel = maxOf(currentRecord?.bestLevel ?: 0, session.finalLevel),
                bestAccuracy = maxOf(currentRecord?.bestAccuracy ?: 0f, accuracy),
                bestStreak = maxOf(currentRecord?.bestStreak ?: 0, session.maxStreak),
                timestamp = session.endTime
            )
        }

        return records
    }
}
