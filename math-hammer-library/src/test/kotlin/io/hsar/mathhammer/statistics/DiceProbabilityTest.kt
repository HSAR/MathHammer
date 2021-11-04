package io.hsar.mathhammer.statistics

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

class DiceProbabilityTest {

    @Test
    fun `4 on a d6 has half chance`() {
        // Act
        val result = DiceProbability.averageChanceToPass(6, 4)

        // Assert
        assertThat(result, equalTo(0.5))
    }

    @Test
    fun `1 on a d6 still fails on a 1`() {
        // Act
        val result = DiceProbability.averageChanceToPass(6, 1)

        // Assert
        assertThat(result, not(equalTo(1)))
    }

    @Test
    fun `7 on a d6 still passes on a 6`() {
        // Act
        val result = DiceProbability.averageChanceToPass(6, 7)

        // Assert
        assertThat(result, not(equalTo(0)))
    }

}