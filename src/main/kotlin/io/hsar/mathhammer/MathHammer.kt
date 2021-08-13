package io.hsar.mathhammer

import io.hsar.mathhammer.cli.input.Ability
import io.hsar.mathhammer.cli.input.Ability.DOUBLE_ATTACKS
import io.hsar.mathhammer.cli.input.Ability.EXTRA_ATTACK
import io.hsar.mathhammer.cli.input.Ability.FLAMER
import io.hsar.mathhammer.cli.input.Ability.MORTAL_WOUND_ON_6
import io.hsar.mathhammer.cli.input.Ability.REROLL_1_TO_HIT
import io.hsar.mathhammer.cli.input.Ability.SHOCK_ASSAULT
import io.hsar.mathhammer.model.AttackResult
import io.hsar.mathhammer.model.UnitProfile
import io.hsar.mathhammer.model.UnitResult
import io.hsar.mathhammer.statistics.HitCalculator
import io.hsar.mathhammer.statistics.KillsCalculator
import io.hsar.mathhammer.statistics.Reroll
import io.hsar.mathhammer.statistics.SaveCalculator
import io.hsar.mathhammer.statistics.WoundCalculator
import io.hsar.wh40k.combatsimulator.cli.input.DefenderDTO


class MathHammer(
    val defenders: Collection<DefenderDTO>
) {

    fun runSimulation(unitProfiles: Collection<UnitProfile>): List<Pair<UnitResult, UnitProfile>> {
        return unitProfiles
            .flatMap { offensiveProfile ->
                // Play each offensive profile against each defensive profile
                defenders.map { defensiveProfile ->
                    apply(offensiveProfile, defensiveProfile) to offensiveProfile
                }
            }
            .sortedByDescending { it.first }
    }

    fun apply(unitProfile: UnitProfile, defensiveProfile: DefenderDTO): UnitResult {
        return unitProfile.offensiveProfiles
            .map { eachOffensiveProfile ->
                eachOffensiveProfile.weaponsAttacking.attackProfiles
                    .flatMap { attackProfile ->
                        attackProfile.abilities
                            .map { ability ->
                                when (ability) {
                                    DOUBLE_ATTACKS -> attackProfile.attackNumber // bonus number of attacks is the same as base
                                    EXTRA_ATTACK -> 1 // see chainswords etc
                                    SHOCK_ASSAULT -> 1 // extra attack on the charge
                                    else -> 0
                                }
                            }
                        when {
                            attackProfile.abilities.contains(DOUBLE_ATTACKS) -> {
                                attackProfile.attackNumber * 2
                            }
                            attackProfile.abilities.contains(EXTRA_ATTACK) -> {
                                attackProfile.attackNumber + 1
                            }
                            else -> {
                                attackProfile.attackNumber
                            }
                        }
                            .let { attacksPerModel ->
                                eachOffensiveProfile.modelsFiring * attacksPerModel
                            }
                            .let { shotsFired ->
                                if (attackProfile.abilities.contains(FLAMER)) {
                                    shotsFired // flamers auto-hit
                                } else {
                                    val rerolls = when {
                                        attackProfile.abilities.contains(REROLL_1_TO_HIT) -> Reroll.ONES
                                        else -> Reroll.NONE
                                    }
                                    shotsFired * HitCalculator.hits(attackProfile.skill, rerolls)
                                }
                            }
                            .let { mainAttackHits ->
                                mainAttackHits + if (attackProfile.abilities.contains(Ability.TESLA)) {
                                    // Tesla effect is "6s to hit cause 2 additional hits", which can be modelled as 33% extra hits
                                    mainAttackHits / 3.0
                                } else {
                                    0.0
                                }
                            }
                            .let { expectedHits ->
                                expectedHits * WoundCalculator.woundingHits(
                                    strength = attackProfile.strength,
                                    toughness = defensiveProfile.toughness
                                )
                            }
                            .let { expectedWounds ->
                                expectedWounds * SaveCalculator.failedSaves(
                                    AP = attackProfile.AP,
                                    save = defensiveProfile.armourSave,
                                    invuln = defensiveProfile.invulnSave
                                )
                            }
                            .let { expectedSuccessfulAttacks ->
                                AttackResult(
                                    name = attackProfile.attackName,
                                    expectedHits = expectedSuccessfulAttacks,
                                    damagePerHit = attackProfile.damage
                                )
                            }
                            .let { mainAttackResult ->
                                (listOf(mainAttackResult) +
                                        if (attackProfile.abilities.contains(MORTAL_WOUND_ON_6)) {
                                            listOf(
                                                AttackResult(
                                                    name = "${attackProfile.attackName} (Mortal Wounds)",
                                                    expectedHits = attackProfile.attackNumber / 6.0,
                                                    damagePerHit = 1.0
                                                )
                                            )
                                        } else {
                                            listOf(null)
                                        }).filterNotNull()
                            }
                    }.let { attackResults ->
                        eachOffensiveProfile to attackResults
                    }
            }
            .toMap()
            .let { offensivesToResults ->
                val expectedDamage = offensivesToResults.flatMap { (_, attackResults) ->
                    attackResults.map { it.expectedHits * it.damagePerHit }
                }.sum()
                val allAttackResults = offensivesToResults.values.flatten()
                UnitResult(
                    unitName = unitProfile.unitName,
                    pointsCost = unitProfile.totalPointsCost,
                    expectedDamage = expectedDamage,
                    expectedKills = KillsCalculator.getKills(defensiveProfile.wounds, allAttackResults),
                    offensivesToResults = offensivesToResults
                )
            }

    }
}