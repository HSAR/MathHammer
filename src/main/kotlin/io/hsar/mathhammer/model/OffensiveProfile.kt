package io.hsar.mathhammer.model

data class OffensiveProfile(
        val firingUnitName: String,
        val modelsFiring: Double,
        val weaponsAttacking: List<AttackProfile>
)