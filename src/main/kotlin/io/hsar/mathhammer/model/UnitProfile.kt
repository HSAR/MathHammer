package io.hsar.mathhammer.model

/**
 * Each unit configuration makes multiple distinct offensive profiles, one per attacker type.
 */
data class UnitProfile(
    val unitName: String,
    val totalPointsCost: Int,
    val offensiveProfiles: List<OffensiveProfile>
)