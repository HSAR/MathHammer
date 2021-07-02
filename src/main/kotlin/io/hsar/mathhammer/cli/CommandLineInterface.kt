package io.hsar.wh40k.combatsimulator.cli

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.hsar.mathhammer.MathHammer
import io.hsar.mathhammer.cli.input.WeaponType.*
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
            required = true
    )
    private var attackerFilePath = ""

    @Parameter(
            names = arrayOf("--defender", "--defenders"),
            description = "Path to an input file describing unit profile(s) defending",
            required = true
    )
    private var defenderFilePath = ""

    @Parameter(
            names = arrayOf("--mode"),
            description = "Comparison mode: DIRECT for un-normalised values or NORMALISED to normalise for 100pts of each attacking profile",
            required = false
    )
    private var mode = ComparisonMode.DIRECT

    override fun run() {
        // Generate offensive profiles according to the mode requested
        objectMapper.readValue<List<AttackerDTO>>(File(attackerFilePath).readText())
                .let { attackerDTOs ->
                    generateOffensiveProfiles(attackerDTOs, mode)
                }
                .let { offensiveProfiles ->
                    MathHammer(
                            defenders = objectMapper.readValue<List<DefenderDTO>>(File(defenderFilePath).readText())
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
        val NORMALISED_POINTS = 100.0

        private val objectMapper = jacksonObjectMapper()
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
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
                                            MELEE -> attacker.attacks.toDouble() // TODO: Melee weapons with +1 attack, etc.
                                            RAPID_FIRE -> weapon.weaponValue.toDouble() * 2.0
                                            else -> weapon.weaponValue.toDoubleOrNull()
                                                    ?: DiceStringParser.expectedValue(weapon.weaponValue)
                                        }
                                        val weaponSkill = when (weapon.weaponType) {
                                            MELEE -> attacker.WS
                                            else -> attacker.BS
                                        }
                                        val weaponStrength = when (weapon.weaponType) {
                                            MELEE -> when (weapon.strength.lowercase()) {
                                                "x2" -> attacker.userStrength * 2
                                                "+3" -> attacker.userStrength + 3
                                                "+2" -> attacker.userStrength + 2
                                                "+1" -> attacker.userStrength + 1
                                                "+0" -> attacker.userStrength
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
