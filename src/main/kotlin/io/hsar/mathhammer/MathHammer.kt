package io.hsar.mathhammer

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.hsar.mathhammer.cli.input.WeaponType
import io.hsar.mathhammer.model.AttackResult
import io.hsar.mathhammer.model.OffensiveProfile
import io.hsar.mathhammer.statistics.HitCalculator
import io.hsar.mathhammer.statistics.SaveCalculator
import io.hsar.mathhammer.statistics.WoundCalculator
import io.hsar.wh40k.combatsimulator.cli.input.AttackerDTO
import io.hsar.wh40k.combatsimulator.cli.input.DefenderDTO


class MathHammer(
        val attackers: Collection<AttackerDTO>,
        val defenders: Collection<DefenderDTO>) {

    fun runSimulation(): List<Pair<AttackResult, OffensiveProfile>> {
        return attackers.map { attacker ->
            attacker.weapons
                    .map { weapon ->
                        val weaponAttacks = if (weapon.weaponType == WeaponType.RAPID_FIRE) {
                            weapon.weaponValue * 2
                        } else {
                            weapon.weaponValue
                        }
                        OffensiveProfile(
                                name = "${attacker.name} firing ${weapon.name}",
                                skill = attacker.BS,
                                attacks = weaponAttacks,
                                strength = weapon.strength,
                                AP = weapon.AP,
                                damage = weapon.damage
                        )
                    }
        }
                .flatten()
                .flatMap { offensiveProfile ->
                    // Play each offensive profile against each defensive profile
                    defenders.map { defensiveProfile ->
                        apply(offensiveProfile, defensiveProfile) to offensiveProfile
                    }
                }
                .sortedByDescending { it.first }
    }

    fun apply(offensiveProfile: OffensiveProfile, defensiveProfile: DefenderDTO): AttackResult {
        val numHitsRequiredToKill = Math.ceil(defensiveProfile.wounds.toDouble() / offensiveProfile.damage).toInt()

        return offensiveProfile
                .let { (_, skill, attacks) ->
                    HitCalculator.hits(skill, attacks)
                }
                .let { expectedHits ->
                    expectedHits * WoundCalculator.woundingHits(
                            strength = offensiveProfile.strength,
                            toughness = defensiveProfile.toughness)
                }
                .let { expectedWounds ->
                    expectedWounds * SaveCalculator.failedSaves(
                            AP = offensiveProfile.AP,
                            save = defensiveProfile.armourSave,
                            invuln = defensiveProfile.invulnSave)
                }
                .let { expectedSuccessfulAttacks ->
                    AttackResult(
                            expectedHits = expectedSuccessfulAttacks,
                            damage = offensiveProfile.damage,
                            expectedKills = expectedSuccessfulAttacks.toInt() / numHitsRequiredToKill,
                            targetName = defensiveProfile.name
                    )
                }
    }
}