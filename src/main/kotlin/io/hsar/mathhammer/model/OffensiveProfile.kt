package io.hsar.mathhammer.model

/**
 * An offensive profile represents a number of models making attacks at once.
 */
data class OffensiveProfile(
        val firingModelName: String,
        val modelsFiring: Int,
        val weaponsAttacking: AttackGroup
)