package io.hsar.mathhammer.statistics

import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.jupiter.api.Test

class WoundCalculatorTest {

    @Test
    fun `massive S still only wounds on 2s`() {
        // Act
        val result = WoundCalculator.woundingHits(16, 2)

        // Assert
        MatcherAssert.assertThat(result, CoreMatchers.equalTo(5 / 6.0))
    }

    @Test
    fun `S greater than (or equal to) double T wounds on 2s`() {
        // Act
        val result = WoundCalculator.woundingHits(8, 4)

        // Assert
        MatcherAssert.assertThat(result, CoreMatchers.equalTo(5 / 6.0))
    }

    @Test
    fun `S greater than T wounds on 3s`() {
        // Act
        val result = WoundCalculator.woundingHits(5, 4)

        // Assert
        MatcherAssert.assertThat(result, CoreMatchers.equalTo(4 / 6.0))
    }

    @Test
    fun `S equals T wounds on 4s`() {
        // Act
        val result = WoundCalculator.woundingHits(4, 4)

        // Assert
        MatcherAssert.assertThat(result, CoreMatchers.equalTo(0.5))
    }

    @Test
    fun `S less than T wounds on 5s`() {
        // Act
        val result = WoundCalculator.woundingHits(3, 4)

        // Assert
        MatcherAssert.assertThat(result, CoreMatchers.equalTo(2 / 6.0))
    }

    @Test
    fun `S less than (or equal to) half T wounds on 6s`() {
        // Act
        val result = WoundCalculator.woundingHits(2, 4)

        // Assert
        MatcherAssert.assertThat(result, CoreMatchers.equalTo(1 / 6.0))
    }

}