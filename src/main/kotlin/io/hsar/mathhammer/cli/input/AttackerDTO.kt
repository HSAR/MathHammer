package io.hsar.wh40k.combatsimulator.cli.input

import io.hsar.mathhammer.cli.input.WeaponDTO

data class AttackerDTO(
        val WS: Int,
        val BS: Int,
        val userStrength: Int,
        val attacks: Int,
        val weapons: WeaponDTO
)