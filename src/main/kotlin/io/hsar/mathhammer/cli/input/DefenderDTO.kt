package io.hsar.wh40k.combatsimulator.cli.input

data class DefenderDTO(
        val toughness: Int,
        val armourSave: Int,
        val invulnSave: Int? // not always present
)