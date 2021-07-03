package io.hsar.wh40k.combatsimulator.cli.input

import io.hsar.mathhammer.cli.input.WeaponDTO

data class AttackerTypeDTO(
    val pointsCost: Int,
    val WS: Int,
    val BS: Int,
    val userStrength: Int,
    val attacks: Int,
    /**
     * An attacker type has multiple "attack groups", which are weapons will be used in sequence (e.g. "fire a pistol, then hit in melee" or "fire the sponson guns then the main turret gun").
     * Each attack group will be compared against each other attack group, including within the unit.
     */
    val attackGroups: List<SingleAttackGroup>
)

typealias SingleAttackGroup = List<WeaponDTO>