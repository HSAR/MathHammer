package io.hsar.mathhammer

import io.hsar.mathhammer.cli.input.Ability
import io.hsar.mathhammer.cli.input.Ability.DOUBLE_ATTACKS
import io.hsar.mathhammer.cli.input.Ability.EXTRA_ATTACK
import io.hsar.mathhammer.cli.input.Ability.FLAMER
import io.hsar.mathhammer.cli.input.Ability.ON_1_TO_HIT_REROLL
import io.hsar.mathhammer.cli.input.Ability.ON_6_TO_WOUND_MORTAL_WOUND
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
                                        attackProfile.abilities.contains(ON_1_TO_HIT_REROLL) -> Reroll.ONES
                                        else -> Reroll.NONE
                                    }
                                    shotsFired * HitCalculator.hits(attackProfile.skill, rerolls)
                                }
                            }
                            .let { mainAttackHits ->
                                mainAttackHits + if (attackProfile.abilities.contains(Ability.ON_6_TO_HIT_TWO_EXTRA_HITS)) {
                                    // Tesla effect is "6s to hit cause 2 additional hits", which can be modelled as 33% extra hits
                                    mainAttackHits / 3.0
                                } else {
                                    0.0
                                }
                            }
                            .let { expectedHits ->
                                val expectedWoundingHits = expectedHits * WoundCalculator.woundingHits(
                                    strength = attackProfile.strength,
                                    toughness = defensiveProfile.toughness
                                )

                                val mortalWounds = if (attackProfile.abilities.contains(ON_6_TO_WOUND_MORTAL_WOUND)) {
                                    expectedWoundingHits / 6.0
                                } else {
                                    0.0
                                }

                                expectedWoundingHits to mortalWounds
                            }
                            .let { (expectedWoundingHits, mortalWounds) ->
                                val mainAttackResult = (expectedWoundingHits * SaveCalculator.failedSaves(
                                    AP = attackProfile.AP,
                                    save = defensiveProfile.armourSave,
                                    invuln = defensiveProfile.invulnSave
                                ))
                                    .let { expectedSuccessfulAttacks ->
                                        AttackResult(
                                            name = attackProfile.attackName,
                                            expectedHits = expectedSuccessfulAttacks,
                                            damagePerHit = attackProfile.damage
                                        )
                                    }

                                // Mortal wounds pass through saves
                                val mortalWoundResult =
                                                if (mortalWounds > 0) {
                                                        AttackResult(
                                                            name = "${attackProfile.attackName} (Mortal Wounds)",
                                                            expectedHits = mortalWounds,
                                                            damagePerHit = 1.0
                                                        )
                                                } else {
                                                    null
                                                }

                                listOf(mainAttackResult, mortalWoundResult).filterNotNull()
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