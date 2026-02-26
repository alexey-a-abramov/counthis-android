package com.countthis.app.utils

import org.junit.Assert.*
import org.junit.Test

class AnswerGeneratorTest {

    @Test
    fun `generates exactly 4 options by default`() {
        val result = AnswerGenerator.generateOptions(correct = 20)
        assertEquals(4, result.size)
    }

    @Test
    fun `includes the correct answer`() {
        val correct = 15
        val result = AnswerGenerator.generateOptions(correct)
        assertTrue("Result should contain correct answer", correct in result)
    }

    @Test
    fun `all options are unique`() {
        val result = AnswerGenerator.generateOptions(correct = 30)
        assertEquals("All options should be unique", result.size, result.toSet().size)
    }

    @Test
    fun `prevents consecutive sequences`() {
        repeat(100) { iteration ->
            val correct = 5 + (iteration % 50)
            val result = AnswerGenerator.generateOptions(correct).sorted()

            val spread = result.last() - result.first()
            val isConsecutive = spread == 3 // For 4 options: [N, N+1, N+2, N+3]

            assertFalse(
                "Options $result should not be consecutive (correct=$correct)",
                isConsecutive
            )
        }
    }

    @Test
    fun `ensures max min ratio is at least 1_4x`() {
        val testCases = listOf(10, 15, 20, 30, 40, 50, 75, 100, 150, 200)

        testCases.forEach { correct ->
            repeat(10) {
                val result = AnswerGenerator.generateOptions(correct)
                val min = result.minOrNull()!!
                val max = result.maxOrNull()!!
                val ratio = max.toDouble() / min.toDouble()

                assertTrue(
                    "For correct=$correct, options=$result: ratio $ratio should be >= 1.4",
                    ratio >= 1.4
                )
            }
        }
    }

    @Test
    fun `ensures minimum difference of 2 between options`() {
        val testCases = listOf(8, 12, 20, 35, 50, 80, 100, 150)

        testCases.forEach { correct ->
            repeat(10) {
                val result = AnswerGenerator.generateOptions(correct).sorted()

                for (i in 0 until result.size - 1) {
                    val diff = result[i + 1] - result[i]
                    assertTrue(
                        "For correct=$correct, options=$result: diff between ${result[i]} and ${result[i+1]} should be >= 2",
                        diff >= 2
                    )
                }
            }
        }
    }

    @Test
    fun `randomizes correct answer position in display order`() {
        val correct = 50
        val positions = mutableMapOf<Int, Int>()

        // Generate 100 samples
        repeat(100) {
            val result = AnswerGenerator.generateOptions(correct)
            val position = result.indexOf(correct)
            positions[position] = positions.getOrDefault(position, 0) + 1
        }

        // Each position should appear at least once
        // With 100 samples and 4 positions, we expect roughly 25 per position
        // We check that each position appears at least 10 times (lenient for randomness)
        for (pos in 0..3) {
            val count = positions.getOrDefault(pos, 0)
            assertTrue(
                "Position $pos appeared $count times out of 100, should be > 10 for uniform distribution",
                count > 10
            )
        }
    }

    @Test
    fun `correct answer appears at all sorted positions including extremes`() {
        val testCases = listOf(10, 20, 50, 100)

        testCases.forEach { correct ->
            val sortedPositions = mutableMapOf<Int, Int>()

            // Generate 200 samples to get good statistics
            repeat(200) {
                val result = AnswerGenerator.generateOptions(correct)
                val sorted = result.sorted()
                val positionInSorted = sorted.indexOf(correct)
                sortedPositions[positionInSorted] = sortedPositions.getOrDefault(positionInSorted, 0) + 1
            }

            println("\nCorrect=$correct - Sorted position distribution:")
            for (pos in 0..3) {
                val count = sortedPositions.getOrDefault(pos, 0)
                val percentage = (count * 100.0 / 200.0)
                val label = when(pos) {
                    0 -> "MIN"
                    3 -> "MAX"
                    else -> "MID"
                }
                println("  Position $pos ($label): $count times (%.1f%%)".format(percentage))
            }

            // Verify correct appears as minimum (position 0)
            val minCount = sortedPositions.getOrDefault(0, 0)
            assertTrue(
                "For correct=$correct, should appear as MIN at least 20 times out of 200 (got $minCount)",
                minCount >= 20
            )

            // Verify correct appears as maximum (position 3)
            val maxCount = sortedPositions.getOrDefault(3, 0)
            assertTrue(
                "For correct=$correct, should appear as MAX at least 20 times out of 200 (got $maxCount)",
                maxCount >= 20
            )

            // Verify correct appears in middle positions
            val midCount = sortedPositions.getOrDefault(1, 0) + sortedPositions.getOrDefault(2, 0)
            assertTrue(
                "For correct=$correct, should appear in MIDDLE at least 40 times out of 200 (got $midCount)",
                midCount >= 40
            )
        }
    }

    @Test
    fun `all options are positive`() {
        repeat(50) {
            val correct = 1 + it
            val result = AnswerGenerator.generateOptions(correct)

            result.forEach { option ->
                assertTrue(
                    "Option $option should be positive (correct=$correct)",
                    option >= 1
                )
            }
        }
    }

    @Test
    fun `works for small numbers`() {
        val testCases = listOf(1, 2, 3, 4, 5, 6, 7, 8)

        testCases.forEach { correct ->
            val result = AnswerGenerator.generateOptions(correct)

            assertEquals(4, result.size)
            assertTrue(correct in result)
            assertTrue(result.all { it >= 1 })

            val sorted = result.sorted()
            val ratio = sorted.last().toDouble() / sorted.first().toDouble()
            assertTrue(
                "For correct=$correct, options=$result: ratio should be >= 1.4",
                ratio >= 1.4
            )
        }
    }

    @Test
    fun `works for large numbers`() {
        val testCases = listOf(100, 200, 500, 1000, 2000)

        testCases.forEach { correct ->
            val result = AnswerGenerator.generateOptions(correct)

            assertEquals(4, result.size)
            assertTrue(correct in result)

            val sorted = result.sorted()
            val ratio = sorted.last().toDouble() / sorted.first().toDouble()
            assertTrue(
                "For correct=$correct, options=$result: ratio should be >= 1.4",
                ratio >= 1.4
            )

            // Check minimum difference
            for (i in 0 until sorted.size - 1) {
                val diff = sorted[i + 1] - sorted[i]
                assertTrue(
                    "For large number $correct, adjacent options should differ by >= 2",
                    diff >= 2
                )
            }
        }
    }

    @Test
    fun `comprehensive validation for range of values`() {
        // Test comprehensive range from small to large numbers
        val testRange = listOf(
            1, 2, 3, 5, 8, 10, 12, 15, 20, 25, 30, 40, 50,
            60, 75, 100, 150, 200, 300, 500, 1000
        )

        testRange.forEach { correct ->
            repeat(5) { iteration ->
                val result = AnswerGenerator.generateOptions(correct)
                val sorted = result.sorted()

                // Validation 1: Correct size
                assertEquals("Test iteration $iteration", 4, result.size)

                // Validation 2: Contains correct answer
                assertTrue("Contains correct=$correct", correct in result)

                // Validation 3: No consecutive sequence
                val spread = sorted.last() - sorted.first()
                assertFalse(
                    "Not consecutive: $sorted",
                    spread == 3
                )

                // Validation 4: 1.4x ratio
                val ratio = sorted.last().toDouble() / sorted.first().toDouble()
                assertTrue(
                    "Ratio >= 1.4 for $sorted (ratio=$ratio)",
                    ratio >= 1.4
                )

                // Validation 5: Min diff of 2
                var minDiff = Int.MAX_VALUE
                for (i in 0 until sorted.size - 1) {
                    minDiff = minOf(minDiff, sorted[i + 1] - sorted[i])
                }
                assertTrue(
                    "Min diff >= 2 for $sorted (minDiff=$minDiff)",
                    minDiff >= 2
                )

                // Validation 6: All positive
                assertTrue(
                    "All positive: $sorted",
                    sorted.all { it >= 1 }
                )
            }
        }
    }

    @Test
    fun `prints sample outputs for manual verification`() {
        println("\n=== Sample Answer Generation Outputs ===")
        val samples = listOf(5, 10, 20, 50, 100, 200)

        samples.forEach { correct ->
            repeat(3) { // Generate 3 samples per value to show variety
                val result = AnswerGenerator.generateOptions(correct)
                val sorted = result.sorted()
                val ratio = sorted.last().toDouble() / sorted.first().toDouble()
                val spread = sorted.last() - sorted.first()
                val sortedPos = sorted.indexOf(correct)
                val posLabel = when(sortedPos) {
                    0 -> "MIN"
                    sorted.size - 1 -> "MAX"
                    else -> "mid"
                }

                println("correct=$correct → $sorted (pos=$sortedPos/$posLabel, spread=$spread, ratio=%.2f)".format(ratio))
            }
            println()
        }
        println("=========================================\n")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws exception for invalid correct value`() {
        AnswerGenerator.generateOptions(correct = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws exception for too few options`() {
        AnswerGenerator.generateOptions(correct = 10, numOptions = 1)
    }
}
