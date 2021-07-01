package io.hsar.mathhammer.cli.input

data class WeaponDTO(
    val weaponType: WeaponType,
    val weaponValue: Int,
    val strength: AttackStrength,
    val AP: Int,
    val damage: Int
// TODO: Effects (e.g. "6s to hit auto-wound", "6s to hit cause a mortal wound", etc)
)

enum class WeaponType {
    MELEE,
    ASSAULT,
    RAPID_FIRE,
    HEAVY,
    PISTOL
}

interface AttackStrength {
    fun getValue(userStrength: Int): Int
}

data class MeleeStrength(
        val bonusToUserStrength: Int
): AttackStrength {
    override fun getValue(userStrength: Int): Int {
        return userStrength + bonusToUserStrength
    }
}

data class RangedStrength(
        val value: Int
): AttackStrength {
    override fun getValue(userStrength: Int): Int {
        return value
    }
}