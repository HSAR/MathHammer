package io.hsar.wh40k.combatsimulator.cli.input

import io.hsar.mathhammer.cli.input.Ability.MELEE_EXTRA_AP
import io.hsar.mathhammer.cli.input.Ability.HEAVY_WEAPON_EXTRA_AP
import io.hsar.mathhammer.cli.input.Ability.ASSAULT_AND_RAPID_FIRE_EXTRA_AP
import io.hsar.mathhammer.cli.input.WeaponDTO
import io.hsar.mathhammer.cli.input.WeaponType.HEAVY
import io.hsar.mathhammer.cli.input.WeaponType.MELEE
import io.hsar.mathhammer.cli.input.WeaponType.RAPID_FIRE
import io.hsar.mathhammer.model.AttackGroup
import io.hsar.mathhammer.model.AttackProfile
import io.hsar.mathhammer.statistics.DiceStringParser

data class AttackerTypeDTO(
    val pointsCost: Int,
    val WS: Int,
    val BS: Int,
    val userStrength: Int,
    val attacks: Int,
    /**
     * An attacker type has multiple "attack groups", which are weapons will be used in sequence (e.g. "fire a pistol, then hit in melee" or "fire the sponson guns then the main turret gun").
     * Each attack group has a name, and each uniquely-named attack group within the unit.
     */
    val attackGroups: Map<String, SingleAttackGroup>
) {
    fun createAttackProfiles(attackerName: String): Map<String, AttackGroup> {
        return attackGroups.map { (attackGroupName, attackGroup) ->
            attackGroupName to attackGroup.map { weapon ->
                val weaponAttacks = when (weapon.weaponType) {
                    MELEE -> this.attacks.toDouble() * weapon.weaponValue.toDouble()
                    RAPID_FIRE -> weapon.weaponValue.toDouble() * 2.0
                    else -> weapon.weaponValue.toDoubleOrNull()
                        ?: DiceStringParser.expectedValue(weapon.weaponValue)
                }
                val weaponSkill = when (weapon.weaponType) {
                    MELEE -> this.WS
                    else -> this.BS
                }.let { baseSkill ->
                    baseSkill - weapon.hitModifier
                }
                val weaponStrength = when (weapon.weaponType) {
                    MELEE -> when (weapon.strength.lowercase()) {
                        "x2" -> this.userStrength * 2
                        "+3" -> this.userStrength + 3
                        "+2" -> this.userStrength + 2
                        "+1" -> this.userStrength + 1
                        "+0", "user" -> this.userStrength
                        else -> this.userStrength + weapon.strength.toInt()
                    }
                    else -> weapon.strength.toInt()
                }
                val weaponDamage = weapon.damage.toDoubleOrNull()
                    ?: DiceStringParser.expectedValue(weapon.damage)

                val weaponAP = when {
                    weapon.abilities.contains(HEAVY_WEAPON_EXTRA_AP) && weapon.weaponType == HEAVY -> weapon.AP + 1
                    weapon.abilities.contains(ASSAULT_AND_RAPID_FIRE_EXTRA_AP) && weapon.weaponType == RAPID_FIRE -> weapon.AP + 1
                    weapon.abilities.contains(MELEE_EXTRA_AP) && weapon.weaponType == MELEE -> weapon.AP + 1
                    else -> weapon.AP
                }

                AttackProfile(
                    attackName = weapon.name,
                    attackNumber = weaponAttacks,
                    skill = weaponSkill,
                    strength = weaponStrength,
                    AP = weaponAP,
                    damage = weaponDamage,
                    abilities = weapon.abilities
                ) to weapon.pointsExtra
            }.map { (attackProfile, additionalPoints) ->
                attackProfile to additionalPoints
            }
        }.map { (attackGroupName, attackProfilesToAdditionalPoints) ->
            // Each attack profile may cost additional points (e.g. power swords cost 5 extra), sum all costs from attack profiles with the base cost to finalise costs
            val totalExtraPoints = attackProfilesToAdditionalPoints.map { (_, pointsCost) -> pointsCost }.sum()
            val attackProfiles = attackProfilesToAdditionalPoints.map { (attackProfile, _) -> attackProfile }.toSet()
            attackGroupName to AttackGroup(
                modelName = attackerName,
                pointsCost = (this.pointsCost + totalExtraPoints),
                attackProfiles = attackProfiles
            )
        }.toMap()
    }
}

typealias SingleAttackGroup = List<WeaponDTO>