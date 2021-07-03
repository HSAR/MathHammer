package io.hsar.mathhammer.util

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

class IntersectionTest {

    @Test
    fun `intersection performs as expected`() {
        // Arrange
        val result = mapOf("X" to setOf("A", "B", "C"), "Y" to setOf("A", "B", "D"))
            // Act
            .intersection()

        // Assert
        assertThat(result, equalTo(mapOf("X" to setOf("A", "B"), "Y" to setOf("A", "B"))))
    }

}