package io.hsar.mathhammer.cli.input

data class WeaponDTO(
    val name: String,
    val hitModifier: Int = 0,
    val pointsExtra: Int = 0,
    val weaponType: WeaponType,
    val weaponValue: String,
    val strength: String,
    val AP: Int,
    val damage: String,
    val abilities: List<Ability> = emptyList()
)

enum class WeaponType {
    MELEE,
    ASSAULT,
    RAPID_FIRE,
    HEAVY,
    PISTOL
}

enum class Ability {
    FLAMER,
    BLAST
}