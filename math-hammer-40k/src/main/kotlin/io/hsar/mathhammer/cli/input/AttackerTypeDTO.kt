package io.hsar.wh40k.combatsimulator.cli.input

import io.hsar.mathhammer.cli.input.WeaponAbility
import io.hsar.mathhammer.cli.input.WeaponAbility.RAPID_FIRE
import io.hsar.mathhammer.cli.input.WeaponDTO
import io.hsar.mathhammer.model.AttackGroup
import io.hsar.mathhammer.model.AttackProfile
import io.hsar.mathhammer.statistics.DiceStringParser

data class AttackerTypeDTO(
    val pointsCost: Int,
    val attacks: Int,
    /**
     * An attacker type has multiple "attack groups", which are weapons will be used in sequence (e.g. "fire a pistol, then hit in melee" or "fire the sponson guns then the main turret gun").
     * Each attack group has a name, and each uniquely-named attack group within the unit.
     */
    val attackGroups: Map<String, SingleAttackGroup>
) {
    fun createAttackProfiles(attackerName: String): Map<String, AttackGroup> {
        return attackGroups
            .map { (attackGroupName, attackGroup) ->
                attackGroupName to attackGroup
                    .map { it.toProfile() }
                    .map { weapon ->
                        val weaponBaseAttacks = if ('d' in weapon.attacks) {
                            (DiceStringParser.expectedValue(weapon.attacks))
                        } else {
                            weapon.attacks.toDouble()
                        }
                        val weaponBonusAttacks = weapon.weaponAbilities.map { (ability, value) ->
                            when (ability) {
                                RAPID_FIRE -> value // RAPID FIRE 2 adds 2 attacks when in half range - assume true
                                WeaponAbility.BLAST -> TODO("Will need another cross product - # of models in defending unit matters")
                                else -> 0
                            }
                        }.sum()
                        val weaponDamage = weapon.damage.toDoubleOrNull()
                            ?: DiceStringParser.expectedValue(weapon.damage)

                        val weaponAP = weapon.AP + weapon.weaponAbilities.map { ability ->
                            when (ability) {
                                else -> 0
                            }
                        }.sum()

                        AttackProfile(
                            attackName = weapon.name,
                            attackNumber = weaponBaseAttacks + weaponBonusAttacks,
                            skill = weapon.attackSkill,
                            strength = weapon.strength,
                            AP = weaponAP,
                            damage = weaponDamage,
                            abilities = weapon.weaponAbilities
                        ) to weapon.pointsExtra
                    }.map { (attackProfile, additionalPoints) ->
                        attackProfile to additionalPoints
                    }
            }.map { (attackGroupName, attackProfilesToAdditionalPoints) ->
                // Each attack profile may cost additional points (e.g. power swords cost 5 extra), sum all costs from attack profiles with the base cost to finalise costs
                val totalExtraPoints = attackProfilesToAdditionalPoints.map { (_, pointsCost) -> pointsCost }.sum()
                val attackProfiles =
                    attackProfilesToAdditionalPoints.map { (attackProfile, _) -> attackProfile }.toSet()
                attackGroupName to AttackGroup(
                    modelName = attackerName,
                    pointsCost = (this.pointsCost + totalExtraPoints),
                    attackProfiles = attackProfiles
                )
            }.toMap()
    }
}

typealias SingleAttackGroup = List<WeaponDTO>