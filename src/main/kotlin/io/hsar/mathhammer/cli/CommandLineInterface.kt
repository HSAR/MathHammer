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
import io.hsar.mathhammer.cli.input.WeaponType.MELEE
import io.hsar.mathhammer.cli.input.WeaponType.RAPID_FIRE
import io.hsar.mathhammer.model.AttackGroup
import io.hsar.mathhammer.model.AttackProfile
import io.hsar.mathhammer.model.OffensiveProfile
import io.hsar.mathhammer.model.UnitProfile
import io.hsar.mathhammer.statistics.DiceStringParser
import io.hsar.mathhammer.util.cartesianProduct
import io.hsar.wh40k.combatsimulator.cli.input.DefenderDTO
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
                        unitResult.offensivesToResults.map { (offensiveProfile, offensiveResult) ->
                            val weapons = offensiveResult.attackResults.map {
                                "${
                                    String.format(
                                        "%.2f",
                                        it.expectedHits
                                    )
                                } ${it.name}"
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
                unit.attackerComposition()
                    .map { (attacker, numberOfModels) ->
                        attacker.attackGroups
                            .map { attackGroup ->
                                attackGroup.map { weapon ->
                                    val weaponAttacks = when (weapon.weaponType) {
                                        MELEE -> attacker.attacks.toDouble() * weapon.weaponValue.toDouble()
                                        RAPID_FIRE -> weapon.weaponValue.toDouble() * 2.0
                                        else -> weapon.weaponValue.toDoubleOrNull()
                                            ?: DiceStringParser.expectedValue(weapon.weaponValue)
                                    }
                                    val weaponSkill = when (weapon.weaponType) {
                                        MELEE -> attacker.WS
                                        else -> attacker.BS
                                    }.let { baseSkill ->
                                        baseSkill - weapon.hitModifier
                                    }
                                    val weaponStrength = when (weapon.weaponType) {
                                        MELEE -> when (weapon.strength.lowercase()) {
                                            "x2" -> attacker.userStrength * 2
                                            "+3" -> attacker.userStrength + 3
                                            "+2" -> attacker.userStrength + 2
                                            "+1" -> attacker.userStrength + 1
                                            "+0", "user" -> attacker.userStrength
                                            else -> attacker.userStrength + weapon.strength.toInt()
                                        }
                                        else -> weapon.strength.toInt()
                                    }
                                    val weaponDamage = weapon.damage.toDoubleOrNull()
                                        ?: DiceStringParser.expectedValue(weapon.damage)

                                    AttackProfile(
                                        attackName = weapon.name,
                                        attackNumber = weaponAttacks,
                                        skill = weaponSkill,
                                        strength = weaponStrength,
                                        AP = weapon.AP,
                                        damage = weaponDamage,
                                        abilities = weapon.abilities
                                    ) to weapon.pointsExtra
                                }.map { (attackProfile, additionalPoints) ->
                                    attackProfile to additionalPoints
                                }
                            }.map { attackProfilesToAdditionalPoints ->
                                val totalExtraPoints = attackProfilesToAdditionalPoints.map { (_, pointsCost) -> pointsCost }.sum()
                                val attackProfiles = attackProfilesToAdditionalPoints.map { (attackProfile, _) -> attackProfile }.toSet()
                                AttackGroup(
                                    modelName = unit.getAttackerName(attacker),
                                    pointsCost = (attacker.pointsCost + totalExtraPoints),
                                    attackProfiles = attackProfiles
                                ) to numberOfModels
                            }.toSet()
                    }
                    .let { modelAttackGroupsToTimesUsed ->
                        if (modelAttackGroupsToTimesUsed.size == 1) {
                            modelAttackGroupsToTimesUsed
                        } else {
                            if (modelAttackGroupsToTimesUsed.size == 2) {
                                modelAttackGroupsToTimesUsed.let { (first, second) ->
                                    cartesianProduct(first, second)
                                }
                            } else {
                                val firstTwoAttackGroupsToAttacksWithGroup = modelAttackGroupsToTimesUsed.take(2)
                                val remainingAttackGroupsToAttacksWithGroup = modelAttackGroupsToTimesUsed - firstTwoAttackGroupsToAttacksWithGroup

                                firstTwoAttackGroupsToAttacksWithGroup.let { (first, second) ->
                                    cartesianProduct(first, second, *remainingAttackGroupsToAttacksWithGroup.toTypedArray())
                                }
                            }
                        }
                    }
                    .map { attackGroupsToNumberOfModels ->
                        attackGroupsToNumberOfModels
                            .map { (attackGroup, numberOfModels) ->
                                // Generate initial offensive profiles
                                OffensiveProfile(
                                    firingModelName = attackGroup.modelName,
                                    modelsFiring = numberOfModels.toDouble(),
                                    weaponsAttacking = attackGroup
                                )
                            }
                    }.map { unitOffensiveProfiles ->
                        unitOffensiveProfiles.map { offensiveProfile -> offensiveProfile.modelsFiring * offensiveProfile.weaponsAttacking.pointsCost }.sum()
                            .let { totalPointsCost ->
                                totalPointsCost to when (mode) {
                                    ComparisonMode.DIRECT -> 1.0
                                    ComparisonMode.NORMALISED -> NORMALISED_POINTS / totalPointsCost
                                }
                            }
                            .let { (totalPointsCost, scaleFactor) ->
                                totalPointsCost to unitOffensiveProfiles.map { baseOffensiveProfile ->
                                    OffensiveProfile( // TODO: Do this all in one pass rather than re-created objects
                                        firingModelName = baseOffensiveProfile.firingModelName,
                                        modelsFiring = baseOffensiveProfile.modelsFiring * scaleFactor,
                                        weaponsAttacking = baseOffensiveProfile.weaponsAttacking
                                    )
                                }
                            }.let { (totalPointsCost, scaledUnitOffensiveProfiles) ->
                                UnitProfile(
                                    unitName = unit.name,
                                    totalPointsCost = totalPointsCost, // TODO: Is this necessary?
                                    offensiveProfiles = scaledUnitOffensiveProfiles
                                )
                            }
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
