package io.hsar.mathhammer

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.hsar.mathhammer.cli.input.UnitDTO
import io.hsar.wh40k.combatsimulator.cli.CommandLineInterface
import io.hsar.wh40k.combatsimulator.cli.CommandLineInterface.Companion.generateUnitOffensives
import org.junit.jupiter.api.Test
import java.io.File

internal class MathHammerTest {

    private val objectMapper = jacksonObjectMapper()
        .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)

    @Test
    fun `test running a simulation`() {
        val offensiveProfiles = objectMapper
            .readValue<List<UnitDTO>>(getResourcePath("data/attackers/assault_intercessors.json").readText())
            .let { attackerDTOs ->
                generateUnitOffensives(
                    attackerDTOs,
                    CommandLineInterface.ComparisonMode.DIRECT
                )
            }
        MathHammer(
            defenders = objectMapper.readValue(getResourcePath("data/defenders/skorpekhs.json").readText())
        )
            .runSimulation(offensiveProfiles)
            .let { result ->
                println(result)
            }
    }

    private fun getResourcePath(resource: String): File {
        return File(this::class.java.classLoader.getResource(resource)?.file ?: throw IllegalArgumentException("Resource not found: $resource"))
    }
}