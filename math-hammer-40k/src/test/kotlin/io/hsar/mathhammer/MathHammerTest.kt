package io.hsar.mathhammer

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.hsar.mathhammer.cli.input.UnitDTO
import io.hsar.wh40k.combatsimulator.cli.input.DefenderDTO
import org.junit.jupiter.api.Test
import java.io.File

internal class MathHammerTest {

    private val objectMapper = jacksonObjectMapper()
        .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)

    @Test
    fun `test running a simulation`() {
        val offensiveProfiles = objectMapper
            .readValue<List<UnitDTO>>(getResourcePath("data/attackers/hellblasters.json").readText())
            .let { attackerDTOs ->
                io.hsar.wh40k.combatsimulator.cli.SimulateCombat.Companion.generateUnitOffensives(
                    attackerDTOs,
                    io.hsar.wh40k.combatsimulator.cli.SimulateCombat.ComparisonMode.DIRECT
                )
            }
        MathHammer(
            defenders = objectMapper.readValue<List<DefenderDTO>>(getResourcePath("data/defenders/skorpekhs.json").readText())
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