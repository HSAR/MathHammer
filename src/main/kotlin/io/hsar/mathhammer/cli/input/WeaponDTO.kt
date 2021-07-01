package io.hsar.mathhammer.cli.input

data class WeaponDTO(
    val name: String,
    val weaponType: WeaponType,
    val weaponValue: Int,
    val strength: Int,
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