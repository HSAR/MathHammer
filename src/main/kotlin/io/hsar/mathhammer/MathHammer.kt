package io.hsar.mathhammer

import io.hsar.mathhammer.cli.input.Ability
import io.hsar.mathhammer.cli.input.Ability.*
import io.hsar.mathhammer.model.AttackResult
import io.hsar.mathhammer.model.OffensiveProfile
import io.hsar.mathhammer.model.OffensiveResult
import io.hsar.mathhammer.statistics.HitCalculator
import io.hsar.mathhammer.statistics.KillsCalculator
import io.hsar.mathhammer.statistics.SaveCalculator
import io.hsar.mathhammer.statistics.WoundCalculator
import io.hsar.wh40k.combatsimulator.cli.input.DefenderDTO


class MathHammer(
        val defenders: Collection<DefenderDTO>
) {

    fun runSimulation(offensiveProfiles: Collection<OffensiveProfile>): List<Pair<OffensiveResult, OffensiveProfile>> {
        return offensiveProfiles
                .flatMap { offensiveProfile ->
                    // Play each offensive profile against each defensive profile
                    defenders.map { defensiveProfile ->
                        apply(offensiveProfile, defensiveProfile) to offensiveProfile
                    }
                }
                .sortedByDescending { it.first }
    }

    fun apply(offensiveProfile: OffensiveProfile, defensiveProfile: DefenderDTO): OffensiveResult {
        return offensiveProfile.weaponsAttacking
                .map { attackProfile ->
                    offensiveProfile
                            .let { (_, modelsFiring) ->
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
                                            modelsFiring * attackProfile.attackNumber
                                        }
                                        .let { shotsFired ->
                                            if (attackProfile.abilities.contains(FLAMER)) {
                                                shotsFired // flamers auto-hit
                                            } else {
                                                shotsFired * HitCalculator.hits(attackProfile.skill)
                                            }
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
                }.let { attackResults ->
                    KillsCalculator.getOffensiveResult(defensiveProfile, attackResults)
                }
    }
}