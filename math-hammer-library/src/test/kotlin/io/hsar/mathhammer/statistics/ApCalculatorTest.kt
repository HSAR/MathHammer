package io.hsar.mathhammer.statistics

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

class ApCalculatorTest {

    @Test
    fun `correctly reduces AP`() {
        // Act
        val result = ApCalculator.effectiveAP(1, 2)

        // Assert
        assertThat(result, equalTo(1))
    }

    @Test
    fun `correctly reduces AP to 0`() {
        // Act
        val result = ApCalculator.effectiveAP(1, 1)

        // Assert
        assertThat(result, equalTo(0))
    }

    @Test
    fun `never goes below AP0`() {
        // Act
        val result = ApCalculator.effectiveAP(1, 0)

        // Assert
        assertThat(result, equalTo(0))
    }
}