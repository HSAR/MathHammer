package io.hsar.mathhammer.aeronautica.model

/**
 * Aircraft may be configured multiple ways
 */
data class AircraftProfile(
    val name: String,
    val totalPointsCost: Double,
    val offensiveProfiles: List<OffensiveProfile>
)