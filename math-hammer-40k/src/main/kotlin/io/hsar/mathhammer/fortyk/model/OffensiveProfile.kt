package io.hsar.mathhammer.fortyk.model

/**
 * An offensive profile represents a number of models making attacks at once.
 */
data class OffensiveProfile(
    val firingModelName: String,
    val modelsFiring: Double,
    val weaponsAttacking: AttackGroup
)