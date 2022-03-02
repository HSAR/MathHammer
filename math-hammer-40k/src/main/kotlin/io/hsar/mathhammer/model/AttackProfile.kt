package io.hsar.mathhammer.model

import io.hsar.mathhammer.cli.input.Ability

data class AttackProfile(
    val attackName: String,
    val attackNumber: Double,
    val skill: Int,
    val strength: Int,
    val AP: Int, // Normalised, sign doesn't matter
    val damage: Double,
    val abilities: List<Ability> = emptyList()
)