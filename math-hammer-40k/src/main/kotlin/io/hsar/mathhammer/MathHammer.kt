package io.hsar.mathhammer

import io.hsar.mathhammer.cli.input.DefenderAbilities.AP_REDUCTION
import io.hsar.mathhammer.cli.input.DefenderAbilities.DAMAGE_REDUCTION
import io.hsar.mathhammer.cli.input.DefenderAbilities.IGNORE_WOUNDS
import io.hsar.mathhammer.cli.input.DefenderAbilities.NO_HIT_REROLLS
import io.hsar.mathhammer.cli.input.DefenderAbilities.NO_WOUND_REROLLS
import io.hsar.mathhammer.cli.input.WeaponAbility
import io.hsar.mathhammer.cli.input.WeaponAbility.AUTO_HIT
import io.hsar.mathhammer.cli.input.WeaponAbility.BLAST
import io.hsar.mathhammer.cli.input.WeaponAbility.CRITS_ON
import io.hsar.mathhammer.cli.input.WeaponAbility.EXTRA_ATTACKS
import io.hsar.mathhammer.cli.input.WeaponAbility.HEAVY
import io.hsar.mathhammer.cli.input.WeaponAbility.ON_1_TO_HIT_REROLL
import io.hsar.mathhammer.cli.input.WeaponAbility.ON_1_TO_WOUND_REROLL
import io.hsar.mathhammer.cli.input.WeaponAbility.ON_ALL_TO_HIT_REROLL
import io.hsar.mathhammer.cli.input.WeaponAbility.ON_ALL_TO_WOUND_REROLL
import io.hsar.mathhammer.cli.input.WeaponAbility.ON_CRIT_TO_HIT_AUTO_WOUND
import io.hsar.mathhammer.cli.input.WeaponAbility.ON_CRIT_TO_HIT_EXTRA_HITS
import io.hsar.mathhammer.cli.input.WeaponAbility.ON_CRIT_TO_WOUND_MORTAL_WOUNDS
import io.hsar.mathhammer.model.AttackProfile
import io.hsar.mathhammer.model.AttackResult
import io.hsar.mathhammer.model.UnitProfile
import io.hsar.mathhammer.model.UnitResult
import io.hsar.mathhammer.statistics.HitCalculator
import io.hsar.mathhammer.statistics.KillsCalculator
import io.hsar.mathhammer.statistics.ReductionCalculator
import io.hsar.mathhammer.statistics.Reroll
import io.hsar.mathhammer.statistics.SaveCalculator
import io.hsar.mathhammer.statistics.WoundCalculator
import io.hsar.wh40k.combatsimulator.cli.input.DefenderDTO
import io.hsar.wh40k.combatsimulator.cli.input.DefenderKeyword


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
                        val attAbilities = attackProfile.abilities
                        attAbilities
                            .map { (ability, value) ->
                                when (ability) {
                                    BLAST -> defensiveProfile.unitSize / 5
                                    EXTRA_ATTACKS -> value
//                                    EXTRA_ATTACK_ON_CHARGE -> 1.0 // extra attack on the charge
                                    else -> 0
                                }.toDouble()
                            }.sum()
                            .let { bonusAttacksGenerated ->
                                bonusAttacksGenerated + attackProfile.attackNumber
                            }
                            .let { attacksPerModel ->
                                eachOffensiveProfile.modelsFiring * attacksPerModel
                            }
                            .let { attacksMade ->
                                woundsPerAttack(attackProfile, defensiveProfile)
                                    .let { (woundingHitsPerAttack, mortalWoundsPerAttack) ->
                                        woundingHitsPerAttack * attacksMade to mortalWoundsPerAttack * attacksMade
                                    }
                            }
                            .let { (expectedWoundingHits, mortalWounds) ->
                                woundsTaken(attackProfile, defensiveProfile, expectedWoundingHits, mortalWounds)
                            }
                    }.let { attackResults ->
                        eachOffensiveProfile to attackResults
                    }
            }
            .toMap()
            .let { offensivesToResults ->
                val expectedDamage = offensivesToResults.flatMap { (_, attackResults) ->
                    attackResults.map { it.unsavedHits * it.damagePerHit }
                }.sum()
                val allAttackResults = offensivesToResults.values.flatten()
                UnitResult(
                    unitName = unitProfile.unitName,
                    pointsCost = unitProfile.totalPointsCost,
                    defender = defensiveProfile,
                    expectedDamage = expectedDamage,
                    expectedKills = KillsCalculator.getKills(
                        targetWounds = defensiveProfile.wounds,
                        ignoreWoundsTarget = defensiveProfile.abilities[IGNORE_WOUNDS],
                        attackResults = allAttackResults
                    ),
                    offensivesToResults = offensivesToResults
                )
            }

    }

    /**
     * For each attack returns the expected number of wounding hits and mortal wounds.
     * @return Pair of wounding hits to mortal wounds
     */
    private fun woundsPerAttack(attackProfile: AttackProfile, defensiveProfile: DefenderDTO): Pair<Double, Double> {
        val attAbilities = attackProfile.abilities
        val defAbilities = defensiveProfile.abilities

        return let {
            val (crits, hits) = if (AUTO_HIT in attAbilities) {
                0.0 to 1.0 // flamers never crit but always hit
            } else {
                val critTarget = attAbilities[CRITS_ON] ?: 6
                val hitTarget = attackProfile.skill + when {
                    HEAVY in attAbilities -> -1
                    else -> 0
                }
                val rerolls = when {
                    defAbilities.contains(NO_HIT_REROLLS) -> Reroll.NONE
                    ON_ALL_TO_HIT_REROLL in attAbilities -> Reroll.ALL
                    ON_1_TO_HIT_REROLL in attAbilities -> Reroll.ONES
                    else -> Reroll.NONE
                }
                HitCalculator.hits(criticalTarget = critTarget, successTarget = hitTarget, reroll = rerolls)
            }

            // On-crit effects here
            val extraHits = attAbilities[ON_CRIT_TO_HIT_EXTRA_HITS]?.let { it * crits } ?: 0.0
            val autoWounds = attAbilities[ON_CRIT_TO_HIT_AUTO_WOUND]?.let { crits } ?: 0.0

            hits + crits + extraHits to autoWounds
        }.let { (hits, autoWounds) ->
            val toWoundTarget =
                attackerAntiDefender(attAbilities, defensiveProfile.keywords) ?: WoundCalculator.woundTarget(
                    strength = attackProfile.strength,
                    toughness = defensiveProfile.toughness
                )

            val rerolls = when {
                NO_WOUND_REROLLS in defAbilities -> Reroll.NONE
                ON_ALL_TO_WOUND_REROLL in attAbilities -> Reroll.ALL
                ON_1_TO_WOUND_REROLL in attAbilities -> Reroll.ONES
                else -> Reroll.NONE
            }
            val (critWoundRate, regularWoundRate) = WoundCalculator.woundingHits(
                woundTarget = toWoundTarget,
                reroll = rerolls
            )

            val critWounds = hits * critWoundRate
            val regularWounds = hits * regularWoundRate

            // On-crit effects here
            if (ON_CRIT_TO_WOUND_MORTAL_WOUNDS in attAbilities) {
                // mortal wound generating critical wounds don't also generate regular wounds
                regularWounds + autoWounds to critWounds * attackProfile.damage
            } else {
                // if no special rules apply critical wounds are just regular wounds
                regularWounds + critWounds + autoWounds to 0.0
            }
        }
    }

    /**
     * Checks whether the attack profile has an ANTI-X that applies to the defender.
     * If multiple apply, return lowest (ie. best to-wound skill).
     * If none, return null.
     */
    private fun attackerAntiDefender(attAbilities: Map<WeaponAbility, Int>, defAbilities: Set<DefenderKeyword>): Int? =
        defAbilities.filter { it.applicableAntiAbility in attAbilities }
            .minOfOrNull { attAbilities[it.applicableAntiAbility]!! }

    private fun woundsTaken(
        attackProfile: AttackProfile,
        defensiveProfile: DefenderDTO,
        expectedWoundingHits: Double,
        mortalWounds: Double
    ): List<AttackResult> {
        val defAbilities = defensiveProfile.abilities
        val apReduction = when {
            AP_REDUCTION in defAbilities -> 1
            else -> 0
        }
        val effectiveAP = ReductionCalculator.effectiveNumber(apReduction, attackProfile.AP)

        val damageReduction = when {
            DAMAGE_REDUCTION in defAbilities -> 1.0
            else -> 0.0
        }
        val effectiveDamage =
            ReductionCalculator.effectiveNumber(damageReduction, attackProfile.damage)

        val mainAttackResult = (expectedWoundingHits * SaveCalculator.failedSaves(
            AP = effectiveAP,
            save = defensiveProfile.armourSave,
            invuln = defensiveProfile.invulnSave
        ))
            .let { expectedSuccessfulAttacks ->
                AttackResult(
                    name = attackProfile.attackName,
                    unsavedHits = expectedSuccessfulAttacks,
                    damagePerHit = effectiveDamage
                )
            }

        // Mortal wounds pass through saves
        val mortalWoundResult =
            if (mortalWounds > 0) {
                AttackResult(
                    name = "${attackProfile.attackName} (Mortal Wounds)",
                    unsavedHits = mortalWounds,
                    damagePerHit = 1.0
                )
            } else {
                null
            }

        return listOfNotNull(mainAttackResult, mortalWoundResult)
    }
}