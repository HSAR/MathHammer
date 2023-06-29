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
import io.hsar.mathhammer.MathHammer
import io.hsar.mathhammer.cli.InputParser.parseAttackers
import io.hsar.mathhammer.cli.InputParser.parseDefenders
import io.hsar.mathhammer.cli.input.UnitDTO
import io.hsar.mathhammer.model.OffensiveProfile
import io.hsar.mathhammer.model.UnitProfile
import io.hsar.mathhammer.util.createCrossProduct
import io.hsar.mathhammer.util.intersection
import io.hsar.wh40k.combatsimulator.utils.sum
import java.io.File
import kotlin.system.exitProcess

abstract class Command(val name: String) {
    abstract fun run()
}

class CommandLineInterface : Command("math-hammer") {

    enum class ComparisonMode { DIRECT, NORMALISED }

    @Parameter(
        names = ["--attacker", "--attackers"],
        description = "Comma-separated list of input files or directories containing input files for unit profile(s) attacking",
        required = true,
        converter = FileConverter::class
    )
    private lateinit var attackerFilePaths: List<File>

    @Parameter(
        names = ["--defender", "--defenders"],
        description = "Path to an input file describing unit profile(s) defending",
        required = true,
        converter = FileConverter::class
    )
    private lateinit var defenderFilePaths: List<File>

    @Parameter(
        names = ["--mode"],
        description = "Comparison mode: DIRECT for un-normalised values or NORMALISED to normalise for 1000pts of each attacking profile",
        required = false
    )
    private var comparisonMode = ComparisonMode.DIRECT

    override fun run() {
        val defenders = defenderFilePaths
            .parseDefenders()

        val offensiveProfiles = attackerFilePaths
            .parseAttackers()
            .let { attackers ->
                generateUnitOffensives(attackers, comparisonMode)
            }

        MathHammer(
            defenders = defenders
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

                    "${
                        String.format(
                            "%.2f",
                            offensiveProfile.modelsFiring
                        )
                    } ${offensiveProfile.firingModelName}s making $weapons hits"
                }.let { attackProfiles ->
                    val unitName = unitProfile.unitName
                    """
                                $unitName $attackProfiles: 
                                Expecting ${unitResult.expectedKills} kills on ${unitResult.defender.name} with ${
                        String.format(
                            "%.3f",
                            unitResult.expectedDamage
                        )
                    } damage.
                            """.trimIndent()
                }
            }
            .forEach { result ->
                println(result)
            }


    }

    companion object {
        val NORMALISED_POINTS = 1000.0

        val objectMapper = jacksonObjectMapper()
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .enable(JsonParser.Feature.ALLOW_COMMENTS)
            .enable(SerializationFeature.INDENT_OUTPUT)
        val objectWriter = objectMapper.writer(
            DefaultPrettyPrinter()
                .also { it.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE) }
        )

        fun generateUnitOffensives(
            attackers: List<UnitDTO>,
            mode: ComparisonMode
        ): List<UnitProfile> {
            return attackers.flatMap { unit ->
                // Convert DTOs into necessary objects
                val modelNameAndAttackGroupNameToAttackGroup = unit.models.map { (modelName, attackerTypeDTO) ->
                    attackerTypeDTO.createAttackProfiles(modelName)
                        .map { (attackGroupName, attackGroup) ->
                            (modelName to attackGroupName) to attackGroup
                        }.toMap()
                }.sum()

                unit.attackerComposition()
                    .let { attackerComposition -> // TODO What's going on here?
                        // Generate combinations of attack group names we will compare against each other
                        unit.models.map { (modelName, attackerTypeDTO) ->
                            attackerTypeDTO.attackGroups.keys
                                .map { eachKey -> modelName to eachKey }
                                .toSet()
                        }.let { attackGroupsForCrossProduct ->

                            val naiveCrossProduct = createCrossProduct(attackGroupsForCrossProduct)

                            val illegalCombinations = unit.models.map { (modelName, attackerTypeDTO) ->
                                modelName to attackerTypeDTO.attackGroups.keys
                            }.toMap()
                                .intersection() // Don't ask
                                .map { (modelName, setOfAttackGroupNames) ->
                                    setOfAttackGroupNames.map { attackGroupName -> modelName to attackGroupName }
                                        .toSet()
                                }
                                .let { listOfSetsOfPairsOfModelNameToAttackGroupName ->
                                    createCrossProduct(listOfSetsOfPairsOfModelNameToAttackGroupName)
                                }
                                .filter { listOfPairsOfModelNameToAttackGroupName ->
                                    listOfPairsOfModelNameToAttackGroupName.toMap().values.toSet().size != 1
                                }
                                .toSet()

                            naiveCrossProduct - illegalCombinations
                        }
                    }
                    .map { attackGroupNamesInSimulation ->
                        val attackGroupsToNumberOfModels = attackGroupNamesInSimulation
                            .map { modelNameToAttackGroupName ->
                                val attackGroup = modelNameAndAttackGroupNameToAttackGroup
                                    .getOrElse(modelNameToAttackGroupName) { throw IllegalStateException("Could not find: $modelNameToAttackGroupName") }
                                val numberOfModels = unit.unitComposition.getOrElse(modelNameToAttackGroupName.first) {
                                    throw IllegalStateException("Could not find: $modelNameToAttackGroupName")
                                }
                                attackGroup to numberOfModels
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
                                    totalPointsCost = totalPointsCost.toDouble(),
                                    offensiveProfiles = scaledUnitOffensiveProfiles
                                )
                            }
                    }
            }
                .map { attackGroupsToNumberOfModels ->
                    attackGroupsToNumberOfModels
                }
        }
    }
}

fun main(args: Array<String>) {
    val instances: Map<String, Command> = listOf(
        CommandLineInterface()
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
