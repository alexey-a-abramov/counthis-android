package com.countthis.app

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.countthis.app.data.PersonalRecord
import com.countthis.app.databinding.ActivityStatisticsBinding
import com.countthis.app.managers.StatisticsManager
import com.countthis.app.managers.ThemeManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.util.concurrent.TimeUnit

class StatisticsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStatisticsBinding
    private lateinit var statsManager: StatisticsManager
    private lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        statsManager = StatisticsManager(this)
        themeManager = ThemeManager(this)

        // Apply theme to UI
        applyTheme()

        loadStatistics()
    }

    /**
     * Apply theme colors to all UI elements.
     */
    private fun applyTheme() {
        // Set status bar color
        window.statusBarColor = ContextCompat.getColor(this, themeManager.getStatusBarColor())

        // Apply background color
        binding.root.setBackgroundColor(
            ContextCompat.getColor(this, themeManager.getBackgroundColor())
        )

        // Apply title color (use header color for consistency)
        binding.titleText.setTextColor(
            ContextCompat.getColor(this, themeManager.getHeaderBgColor())
        )

        // Apply stat value colors (use header color for emphasis)
        val headerColor = ContextCompat.getColor(this, themeManager.getHeaderBgColor())
        binding.accuracyText.setTextColor(headerColor)
        binding.bestStreakText.setTextColor(headerColor)
        binding.totalGamesText.setTextColor(headerColor)
        binding.timePlayedText.setTextColor(headerColor)
    }

    private fun loadStatistics() {
        val stats = statsManager.getStatistics()

        // Calculate and display accuracy
        val accuracy = if (stats.totalRounds > 0) {
            (stats.totalCorrect.toFloat() / stats.totalRounds.toFloat() * 100f)
        } else {
            0f
        }
        binding.accuracyText.text = "%.1f%%".format(accuracy)

        // Display best streak
        binding.bestStreakText.text = stats.bestStreak.toString()

        // Display total games
        binding.totalGamesText.text = stats.totalGames.toString()

        // Display time played
        binding.timePlayedText.text = formatTimePlayed(stats.totalTimePlayed)

        // Setup accuracy chart
        setupAccuracyChart(stats.accuracyHistory)

        // Setup personal records
        setupPersonalRecords(stats.personalRecords.values.toList())
    }

    private fun formatTimePlayed(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60

        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "<1m"
        }
    }

    private fun setupAccuracyChart(accuracyHistory: List<com.countthis.app.data.AccuracyPoint>) {
        if (accuracyHistory.isEmpty()) {
            binding.accuracyChart.visibility = View.GONE
            return
        }

        val entries = accuracyHistory.mapIndexed { index, point ->
            Entry(index.toFloat(), point.accuracy)
        }

        val dataSet = LineDataSet(entries, "Accuracy %").apply {
            val chartColor = ContextCompat.getColor(this@StatisticsActivity, themeManager.getHeaderBgColor())
            color = chartColor
            setCircleColor(chartColor)
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        val lineData = LineData(dataSet)
        binding.accuracyChart.apply {
            data = lineData
            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false
            axisLeft.axisMinimum = 0f
            axisLeft.axisMaximum = 100f
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            setTouchEnabled(false)
            invalidate()
        }
    }

    private fun setupPersonalRecords(records: List<PersonalRecord>) {
        binding.recordsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.recordsRecyclerView.adapter = PersonalRecordsAdapter(records)
    }

    private class PersonalRecordsAdapter(
        private val records: List<PersonalRecord>
    ) : RecyclerView.Adapter<PersonalRecordsAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val modeText: TextView = view.findViewById(R.id.modeText)
            val levelText: TextView = view.findViewById(R.id.levelText)
            val accuracyText: TextView = view.findViewById(R.id.accuracyText)
            val streakText: TextView = view.findViewById(R.id.streakText)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_personal_record, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val record = records[position]
            holder.modeText.text = record.mode.name.replace("_", " ")
            holder.levelText.text = "Level: ${record.bestLevel}"
            holder.accuracyText.text = "Accuracy: %.1f%%".format(record.bestAccuracy)
            holder.streakText.text = "Streak: ${record.bestStreak}"
        }

        override fun getItemCount() = records.size
    }
}
