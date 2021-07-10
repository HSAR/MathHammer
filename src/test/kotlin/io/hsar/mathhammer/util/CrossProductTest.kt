package io.hsar.mathhammer.util

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test

class CrossProductTest {

    @Test
    fun `cross product with just one collection works as expected`() {
        // Arrange
        val input = listOf(setOf("A", "B", "C"))

        // Act
        val result = createCrossProduct(input)

        // Assert
        assertThat(result, hasSize(input.first().size))
        assertThat(result, equalTo(setOf(listOf("A"), listOf("B"), listOf("C"))))
    }

    // TODO Tests for correctly cross producting 2 and 3 collections
}