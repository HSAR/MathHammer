package io.hsar.mathhammer.statistics

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.closeTo
import org.junit.jupiter.api.Test

class DiceProbabilityTest {

    @Test
    fun `d6 with 4+ to pass has half chance`() {
        // Act
        val result = DiceProbability.averageSuccesses(6, 4)

        // Assert
        assertThat(result, equalTo(0.5))
    }

    @Test
    fun `d6 on 1+ still fails on a 1`() {
        // Act
        val result = DiceProbability.averageSuccesses(6, 1)

        // Assert
        assertThat(result, not(equalTo(1)))
    }

    @Test
    fun `d6 on 7+ still passes on a 6`() {
        // Act
        val result = DiceProbability.averageSuccesses(6, 7)

        // Assert
        assertThat(result, not(equalTo(0)))
    }

    @Test
    fun `d6 on 4+ reroll all returns correctly`() {
        // Act
        val result = DiceProbability.averageSuccesses(6, 4, Reroll.ALL)

        // Assert
        assertThat(result, equalTo(0.75))
    }

    @Test
    fun `d6 on 2+ re-rolling correctly returns the same for both rerolls`() {
        // Act
        val resultRerollOnes = DiceProbability.averageSuccesses(6, 2, Reroll.ONES)
        val resultRerollAll = DiceProbability.averageSuccesses(6, 2, Reroll.ALL)

        // Assert
        assertThat(resultRerollOnes, equalTo(resultRerollAll))
        assertThat(
            resultRerollOnes,
            `is`(closeTo(35.0 / 36.0, 0.001))
        ) // 1 in 36 chance to roll two successive 1s on d6s
    }

    @Test
    fun `d6 with 4+ to pass and 6+ to crit with no rerolls returns correctly`() {
        // Act
        val result = DiceProbability.averageCriticalsAndSuccesses(
            diceNumber = 6,
            criticalTarget = 6,
            successTarget = 4,
            rerolls = Reroll.NONE
        )

        // Assert
        assertThat(result.first + result.second, equalTo(0.5))
        assertThat(result.first, equalTo(1.0 / 6.0))
        assertThat(result.second, equalTo(2.0 / 6.0))
    }

    @Test
    fun `d6 with 4+ to pass and 6+ to crit with reroll ones returns correctly`() {
        // Act
        val result = DiceProbability.averageCriticalsAndSuccesses(
            diceNumber = 6,
            criticalTarget = 6,
            successTarget = 4,
            rerolls = Reroll.ONES
        )

        // Assert
        assertThat(result.first + result.second, `is`(closeTo(0.5 + (0.5 / 6.0), 0.001)))
        assertThat(result.first, `is`(closeTo((1.0 / 6.0) + (1.0 / 36.0), 0.001)))
        assertThat(result.second, `is`(closeTo((2.0 / 6.0) + (2.0 / 36.0), 0.001)))
    }

    @Test
    fun `d6 with 4+ to pass and 6+ to crit with reroll all returns correctly`() {
        // Act
        val result = DiceProbability.averageCriticalsAndSuccesses(
            diceNumber = 6,
            criticalTarget = 6,
            successTarget = 4,
            rerolls = Reroll.ALL
        )

        // Assert
        assertThat(result.first + result.second, `is`(closeTo(0.75, 0.001)))
        assertThat(result.first, `is`(closeTo((1.0 / 6.0) + (3.0 / 36.0), 0.001)))
        assertThat(result.second, `is`(closeTo((2.0 / 6.0) + (6.0 / 36.0), 0.001)))
    }


}