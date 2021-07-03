package io.hsar.wh40k.combatsimulator.cli

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.beust.jcommander.converters.FileConverter
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.hsar.mathhammer.MathHammer
import io.hsar.mathhammer.cli.input.UnitDTO
import io.hsar.mathhammer.model.OffensiveProfile
import io.hsar.mathhammer.model.UnitProfile
import io.hsar.mathhammer.util.cartesianProduct
import io.hsar.wh40k.combatsimulator.cli.input.DefenderDTO
import io.hsar.wh40k.combatsimulator.utils.sum
import java.io.File
import kotlin.system.exitProcess

abstract class Command(val name: String) {
    abstract fun run()
}

class SimulateCombat : Command("math-hammer") {

    enum class ComparisonMode { DIRECT, NORMALISED }

    @Parameter(
        names = arrayOf("--attacker", "--attackers"),
        description = "Path to an input file describing unit profile(s) attacking",
        required = true,
        converter = FileConverter::class
    )
    private lateinit var attackerFilePaths: List<File>

    @Parameter(
        names = arrayOf("--defender", "--defenders"),
        description = "Path to an input file describing unit profile(s) defending",
        required = true,
        converter = FileConverter::class
    )
    private lateinit var defenderFilePaths: List<File>

    @Parameter(
        names = arrayOf("--mode"),
        description = "Comparison mode: DIRECT for un-normalised values or NORMALISED to normalise for 100pts of each attacking profile",
        required = false
    )
    private var mode = ComparisonMode.DIRECT

    override fun run() {
        // Generate offensive profiles according to the mode requested
        attackerFilePaths
            .flatMap { attackerFilePath ->
                objectMapper.readValue<List<UnitDTO>>(attackerFilePath.readText())
            }
            .let { attackerDTOs ->
                generateUnitOffensives(attackerDTOs, mode)
            }
            .let { offensiveProfiles ->
                MathHammer(
                    defenders = defenderFilePaths
                        .flatMap { defenderFilePath ->
                            objectMapper.readValue<List<DefenderDTO>>(defenderFilePath.readText())
                        }
                )
                    .runSimulation(
                        offensiveProfiles
                    )
                    .map { (unitResult, unitProfile) ->
                        unitResult.offensivesToResults.map { (offensiveProfile, offensiveResults) ->
                            val weapons = offensiveResults.map { attackResult ->
                                "${
                                    String.format(
                                        "%.2f",
                                        attackResult.expectedHits
                                    )
                                } ${attackResult.name}"
                            }

                            "${String.format("%.2f", offensiveProfile.modelsFiring)} ${offensiveProfile.firingModelName}s making $weapons hits"
                        }.let { attackProfiles ->
                            val unitName = unitProfile.unitName
                            """
                                $unitName $attackProfiles: 
                                Expecting ${unitResult.expectedKills} kills with ${String.format("%.3f", unitResult.expectedDamage)} damage.
                            """.trimIndent()
                        }
                    }
                    .forEach { result ->
                        println(result)
                    }
            }
    }

    companion object {
        val NORMALISED_POINTS = 1000.0

        private val objectMapper = jacksonObjectMapper()
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .enable(JsonParser.Feature.ALLOW_COMMENTS)
            .enable(SerializationFeature.INDENT_OUTPUT)
        private val objectWriter = objectMapper.writer(
            DefaultPrettyPrinter()
                .also { it.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE) }
        )

        fun generateUnitOffensives(units: List<UnitDTO>, mode: ComparisonMode): List<UnitProfile> {
            return units.flatMap { unit ->
                // Convert DTOs into necessary objects
                val attackGroupNamesToAttackGroupsAndModelCount = unit.attackerComposition()
                    .mapKeys { (attackerTypeDTO, modelCount) -> attackerTypeDTO.createAttackProfiles(unit.getAttackerName(attackerTypeDTO)) }
                    .map { (attackGroups, modelCount) ->
                        attackGroups.mapValues { (_, attackGroup) -> attackGroup to modelCount }
                    }.sum()


                unit.attackerComposition()
                    .let { attackerComposition ->
                        // Generate combinations of attack group names we will compare against each other
                        unit.models.values.map { it.attackGroups.keys }.let { attackGroupKeys ->

                            if (attackGroupKeys.size == 1) {
                                setOf(attackGroupKeys.first().toList()) // reformat into correct structure
                            } else {
                                if (attackGroupKeys.size == 2) {
                                    attackGroupKeys.let { (first, second) ->
                                        cartesianProduct(first, second)
                                    }
                                } else {
                                    val firstTwoAttackGroupKeys = attackGroupKeys.take(2)
                                    val remainingAttackGroupKeys = attackGroupKeys - firstTwoAttackGroupKeys

                                    firstTwoAttackGroupKeys.let { (first, second) ->
                                        cartesianProduct(first, second, *remainingAttackGroupKeys.toTypedArray())
                                    }
                                }
                            }
                        }
                    }
                    .map { attackGroupNamesInSimulation ->
                        val attackGroupsToNumberOfModels = attackGroupNamesInSimulation
                            .map { attackGroupName ->
                                attackGroupNamesToAttackGroupsAndModelCount.getOrElse(attackGroupName) { throw IllegalStateException("Could not find attack group with name: $attackGroupName") }
                            }.toMap()

                        attackGroupsToNumberOfModels
                            .map { (attackGroup, numberOfModels) ->
                                attackGroup.pointsCost * numberOfModels
                            }.sum()
                            .let { totalPointsCost ->
                                when (mode) {
                                    ComparisonMode.DIRECT -> 1.0
                                    ComparisonMode.NORMALISED -> NORMALISED_POINTS / totalPointsCost
                                }.let { scaleFactor ->
                                    totalPointsCost to attackGroupsToNumberOfModels.map { (attackGroup, numberOfModels) ->
                                        OffensiveProfile(
                                            firingModelName = attackGroup.modelName,
                                            modelsFiring = numberOfModels * scaleFactor,
                                            weaponsAttacking = attackGroup
                                        )
                                    }
                                }
                            }
                            .let { (totalPointsCost, scaledUnitOffensiveProfiles) ->
                                UnitProfile(
                                    unitName = unit.name,
                                    totalPointsCost = totalPointsCost.toDouble(), // TODO: Is this necessary?
                                    offensiveProfiles = scaledUnitOffensiveProfiles
                                )
                            }

                    }
                    .map { attackGroupsToNumberOfModels ->
                        attackGroupsToNumberOfModels
                    }
            }
        }
    }
}

fun main(args: Array<String>) {
    val instances: Map<String, Command> = listOf(
        SimulateCombat()
    )
        .associateBy { it.name }
    val commander = JCommander()
    instances.forEach { name, command -> commander.addCommand(name, command) }

    if (args.isEmpty()) {
        commander.usage()
        System.err.println("Expected some arguments")
        exitProcess(1)
    }

    try {
        commander.parse(*args)
        val command = instances[commander.parsedCommand]
        command!!.run()
    } catch (e: Exception) {
        e.printStackTrace()
        exitProcess(1)
    }
}
