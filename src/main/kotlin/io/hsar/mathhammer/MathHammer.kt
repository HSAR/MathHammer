package io.hsar.mathhammer

import io.hsar.mathhammer.cli.input.WeaponType
import io.hsar.mathhammer.model.OffensiveProfile
import io.hsar.mathhammer.statistics.HitCalculator
import io.hsar.mathhammer.statistics.SaveCalculator
import io.hsar.mathhammer.statistics.WoundCalculator
import io.hsar.wh40k.combatsimulator.cli.input.AttackerDTO
import io.hsar.wh40k.combatsimulator.cli.input.DefenderDTO


class MathHammer(
        val attackers: Collection<AttackerDTO>,
        val defenders: Collection<DefenderDTO>) {

    fun runSimulation(): Result {
        return attackers.map { attacker ->
            attacker.weapons
                    .map { weapon ->
                        val weaponAttacks = if (weapon.weaponType == WeaponType.RAPID_FIRE) {
                            weapon.weaponValue * 2
                        } else {
                            weapon.weaponValue
                        }
                        OffensiveProfile(
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
                        offensiveProfile to apply(offensiveProfile, defensiveProfile)
                    }
                }
                .toMap()
                .let { resultMap ->
                    Result(
                            offensiveProfileToDamageDone = resultMap
                    )
                }
    }

    fun apply(offensiveProfile: OffensiveProfile, defensiveProfile: DefenderDTO): Double {
        return offensiveProfile
                .let { (skill, attacks) ->
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
    }
}

data class Result(
        val offensiveProfileToDamageDone: Map<OffensiveProfile, Double>
)