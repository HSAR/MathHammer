package io.hsar.mathhammer.statistics

import io.hsar.mathhammer.model.AttackResult
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

class KillsCalculatorTest {

    @Test
    fun `hits cause the correct number of kills`() {
        // Arrange
        val targetWounds = 2
        val attackResult = AttackResult(name = "test attack result", expectedHits = 4.0, damagePerHit = 1.0)

        // Act
        val result = KillsCalculator.getKills(
            targetWounds = targetWounds,
            attackResults = listOf(attackResult)
        )

        // Assert
        assertThat(result, equalTo(2))
    }

    @Test
    fun `incomplete kills don't count`() {
        // Arrange
        val targetWounds = 2
        val attackResult = AttackResult(name = "test attack result", expectedHits = 3.0, damagePerHit = 1.0)

        // Act
        val result = KillsCalculator.getKills(
            targetWounds = targetWounds,
            attackResults = listOf(attackResult)
        )

        // Assert
        assertThat(result, equalTo(1))
    }

    @Test
    fun `damage doesn't overflow`() {
        // Arrange
        val targetWounds = 3
        val attackResult = AttackResult(
            name = "test attack result",
            expectedHits = 3.0,
            damagePerHit = 2.0
        ) // this does 6 total damage which is enough to make 2 kills, but as damage is lost in overflow only kills 1

        // Act
        val result = KillsCalculator.getKills(
            targetWounds = targetWounds,
            attackResults = listOf(attackResult)
        )

        // Assert
        assertThat(result, equalTo(1))
    }
}