package io.hsar.mathhammer.statistics

import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

class HitCalculatorTest {

    @Test
    fun `6+ crit & 4+ hit returns correctly`() {
        // Act
        val result = HitCalculator.hits(criticalTarget = 6, successTarget = 4)

        // Assert
        assertThat(result.first + result.second, CoreMatchers.equalTo(0.5))
        assertThat(result.first, CoreMatchers.equalTo(1.0 / 6.0))
        assertThat(result.second, CoreMatchers.equalTo(2.0 / 6.0))
    }
}