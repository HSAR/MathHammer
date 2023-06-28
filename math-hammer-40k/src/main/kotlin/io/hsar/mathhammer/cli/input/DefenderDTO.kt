package io.hsar.wh40k.combatsimulator.cli.input

import io.hsar.mathhammer.cli.input.DefenderAbilities

data class DefenderDTO(
    val name: String = "Unnamed Unit",
    val toughness: Int,
    val wounds: Int,
    val armourSave: Int,
    val invulnSave: Int = 7, // not always present
    val abilities: Map<DefenderAbilities, Int> = emptyMap()
)