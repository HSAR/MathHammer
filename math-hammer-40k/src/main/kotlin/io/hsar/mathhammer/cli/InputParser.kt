package io.hsar.mathhammer.cli

import com.fasterxml.jackson.module.kotlin.readValue
import io.hsar.mathhammer.cli.input.UnitDTO
import io.hsar.wh40k.combatsimulator.cli.CommandLineInterface
import io.hsar.wh40k.combatsimulator.cli.input.DefenderDTO
import java.io.File

object InputParser {

    fun List<File>.parseAttackers(): List<UnitDTO> {
        return this
            .flatMap { attackerFilePath ->
                if (attackerFilePath.isDirectory) {
                    (attackerFilePath.listFiles() ?: throw IllegalStateException("Failed to retrieve files in directory: $attackerFilePath"))
                        .toList()
                } else {
                    listOf(attackerFilePath)
                }
            }
            .flatMap { attackerFilePath ->
                CommandLineInterface.objectMapper.readValue<List<UnitDTO>>(attackerFilePath.readText())
            }
    }

    fun List<File>.parseDefenders(): List<DefenderDTO> {
        return this
            .flatMap { defenderFilePath ->
                CommandLineInterface.objectMapper.readValue<List<DefenderDTO>>(defenderFilePath.readText())
            }
    }
}