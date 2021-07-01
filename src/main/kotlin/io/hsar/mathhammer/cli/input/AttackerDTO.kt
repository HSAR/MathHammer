package io.hsar.wh40k.combatsimulator.cli.input

import io.hsar.mathhammer.cli.input.WeaponDTO

data class AttackerDTO(
        val name: String = "Unnamed Unit",
        val pointsCost: Int,
        val WS: Int,
        val BS: Int,
        val userStrength: Int,
        val attacks: Int,
        val weapons: List<WeaponDTO>
)