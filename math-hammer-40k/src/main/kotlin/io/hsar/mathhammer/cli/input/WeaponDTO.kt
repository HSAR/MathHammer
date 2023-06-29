package io.hsar.mathhammer.cli.input

data class WeaponProfile(
    val name: String,
    val pointsExtra: Int = 0,
    val weaponAbilities: Map<WeaponAbility, Int> = emptyMap(), // For ANTI-X implementation see WeaponDTO::toProfile
    val attacks: String,
    val attackSkill: Int = 7,
    val strength: Int,
    val AP: Int,
    val damage: String,
)

data class WeaponDTO(
    val name: String,
    val pointsExtra: Int = 0,
    val weaponAbilities: Set<String> = emptySet(),
    val attacks: String,
    val attackSkill: Int = 7,
    val strength: Int,
    val AP: Int,
    val damage: String,
) {
    fun toProfile(): WeaponProfile = this.weaponAbilities
        .map { abilityString ->
            if (' ' in abilityString) {
                abilityString.split(' ', ignoreCase = true, limit = 2)
                    .let { (abilityKeyString, abilityValueString) ->
                        val abilityKey = WeaponAbility.valueOf(abilityKeyString)
                        val abilityValue = abilityValueString.toInt()
                        abilityKey to abilityValue
                    }
            } else {
                WeaponAbility.valueOf(abilityString) to -1
            }
        }
        .toMap()
        .let { weaponAbilityMap ->
            WeaponProfile(
                name = name,
                pointsExtra = pointsExtra,
                weaponAbilities = weaponAbilityMap,
                attacks = attacks,
                attackSkill = attackSkill,
                strength = strength,
                AP = AP,
                damage = damage
            )
        }
}