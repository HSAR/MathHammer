package io.hsar.mathhammer.statistics

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThan
import org.junit.jupiter.api.Test

class SaveCalculatorTest {

    @Test
    fun `4+ save has half chance`() {
        // Act
        val result = SaveCalculator.failedSaves(0, 4)

        // Assert
        assertThat(result, equalTo(0.5))
    }

    @Test
    fun `AP increases failed saves`() {
        // Act
        val control = SaveCalculator.failedSaves(0, 4)
        val test = SaveCalculator.failedSaves(1, 4)

        // Assert
        assertThat(test, greaterThan(control))
    }

    @Test
    fun `AP can cause no-save`() {
        // Act
        val result = SaveCalculator.failedSaves(-3, 4)

        // Assert
        assertThat(result, equalTo(1.0))
    }
}