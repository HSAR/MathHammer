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
import io.hsar.mathhammer.cli.input.WeaponType.MELEE
import io.hsar.mathhammer.cli.input.WeaponType.RAPID_FIRE
import io.hsar.mathhammer.model.AttackProfile
import io.hsar.mathhammer.model.OffensiveProfile
import io.hsar.mathhammer.statistics.DiceStringParser
import io.hsar.wh40k.combatsimulator.cli.input.AttackerDTO
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
                objectMapper.readValue<List<AttackerDTO>>(attackerFilePath.readText())
            }
            .let { attackerDTOs ->
                generateOffensiveProfiles(attackerDTOs, mode)
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
                    .map { (offensiveResult, offensiveProfile) ->
                        val weaponNames = offensiveResult.attackResults.map {
                            "${
                                String.format(
                                    "%.2f",
                                    it.expectedHits
                                )
                            } ${it.name}"
                        }
                        """${
                            String.format(
                                "%.2f",
                                offensiveProfile.modelsFiring
                            )
                        } ${offensiveProfile.firingUnitName}s attacking with $weaponNames:
                           Expecting ${offensiveResult.expectedKills} kills with ${
                            String.format(
                                "%.3f",
                                offensiveResult.expectedDamage
                            )
                        } damage."""
                    }
                    .forEach() { result ->
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

        fun generateOffensiveProfiles(attackers: List<AttackerDTO>, mode: ComparisonMode): List<OffensiveProfile> {
            return attackers
                .map { attacker ->
                    attacker.weapons
                        .map { weaponProfiles ->
                            val attackProfiles = weaponProfiles.map { weapon ->
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
                                )
                            }

                            val modelsFiring: Double = when (mode) {
                                ComparisonMode.DIRECT -> 1.0 // TODO: Use default unit size
                                ComparisonMode.NORMALISED -> NORMALISED_POINTS / (attacker.pointsCost + weaponProfiles.map { it.pointsExtra }
                                    .sum())
                            }

                            OffensiveProfile(
                                firingUnitName = attacker.name,
                                modelsFiring = modelsFiring,
                                weaponsAttacking = attackProfiles
                            )
                        }
                }
                .flatten()
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
