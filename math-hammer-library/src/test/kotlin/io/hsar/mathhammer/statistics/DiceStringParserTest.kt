package io.hsar.mathhammer.statistics

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

class DiceStringParserTest {

    @Test
    fun `returns expected value when dice number is not given`() {
        // Act
        val result = DiceStringParser.expectedValue("d6")

        // Assert
        assertThat(result, equalTo(3.5))
    }

    @Test
    fun `returns max value when dice number is not given`() {
        // Act
        val result = DiceStringParser.maxValue("d6")

        // Assert
        assertThat(result, equalTo(6.0))
    }

    @Test
    fun `returns expected value when dice number is given`() {
        // Act
        val result = DiceStringParser.expectedValue("3d10")

        // Assert
        assertThat(result, equalTo(16.5))
    }

    @Test
    fun `returns max value when dice number is given`() {
        // Act
        val result = DiceStringParser.maxValue("3d10")

        // Assert
        assertThat(result, equalTo(30.0))
    }
}