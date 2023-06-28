package io.hsar.mathhammer.util

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

class AverageTest {

    @Test
    fun `average works as expected`() {
        // Arrange
        val input = listOf(0.0 to 1.0, 2.0 to 3.0, 4.0 to 5.0)

        // Act
        val result = input.average()

        // Assert
        assertThat(result, equalTo(2.0 to 3.0))
    }
}