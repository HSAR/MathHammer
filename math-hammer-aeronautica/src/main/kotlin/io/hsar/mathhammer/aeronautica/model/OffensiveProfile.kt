package io.hsar.mathhammer.aeronautica.model

import io.hsar.mathhammer.aeronautica.cli.input.WeaponDTO

data class OffensiveProfile(
    val name: String,
    val modelsAttacking: Double, // This is needed for points normalisation
    val weaponsAttacking: List<WeaponDTO>
)
