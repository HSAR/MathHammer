package io.hsar.wh40k.combatsimulator.cli.input

import io.hsar.mathhammer.cli.input.DefenderAbilities
import io.hsar.mathhammer.cli.input.WeaponAbility
import io.hsar.mathhammer.cli.input.WeaponAbility.ANTI_FLY
import io.hsar.mathhammer.cli.input.WeaponAbility.ANTI_INFANTRY
import io.hsar.mathhammer.cli.input.WeaponAbility.ANTI_PSYKER
import io.hsar.mathhammer.cli.input.WeaponAbility.ANTI_VEHICLE

data class DefenderDTO(
    val name: String = "Unnamed Unit",
    val toughness: Int,
    val wounds: Int,
    val armourSave: Int,
    val invulnSave: Int = 7, // not always present
    val abilities: Map<DefenderAbilities, Int> = emptyMap(),
    val keywords: Set<DefenderKeyword> = emptySet(),
    val unitSize: Int = 1 // for vulnerability to blast
)

enum class DefenderKeyword(val applicableAntiAbility: WeaponAbility) {
    INFANTRY(ANTI_INFANTRY),
    FLY(ANTI_FLY),
    VEHICLE(ANTI_VEHICLE),
    PSYKER(ANTI_PSYKER)
}