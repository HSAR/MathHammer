package io.hsar.wh40k.combatsimulator.cli.input

data class DefenderDTO(
        val name: String = "Unnamed Unit",
        val toughness: Int,
        val wounds: Int,
        val armourSave: Int,
        val invulnSave: Int? // not always present
)