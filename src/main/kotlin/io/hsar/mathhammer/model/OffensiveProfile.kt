package io.hsar.mathhammer.model

data class OffensiveProfile(
        val firingUnitName: String,
        val skill: Int,
        val modelsFiring: Double,
        val weaponsAttacking: List<AttackProfile>
)