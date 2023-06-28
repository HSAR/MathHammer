package io.hsar.mathhammer.model

import io.hsar.mathhammer.cli.input.WeaponAbility

data class AttackProfile(
    val attackName: String,
    val attackNumber: Double,
    val skill: Int,
    val strength: Int,
    val AP: Int, // Normalised, sign doesn't matter
    val damage: Double,
    val abilities: Map<WeaponAbility, Int> = emptyMap()
)