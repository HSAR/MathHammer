package io.hsar.mathhammer.fortyk.model

/**
 * Each unit configuration makes multiple distinct offensive profiles, one per attacker type.
 */
data class UnitProfile(
    val unitName: String,
    val totalPointsCost: Double,
    val offensiveProfiles: List<OffensiveProfile>
)