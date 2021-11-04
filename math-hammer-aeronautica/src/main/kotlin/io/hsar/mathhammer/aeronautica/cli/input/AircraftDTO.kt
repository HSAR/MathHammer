package io.hsar.mathhammer.aeronautica.cli.input

data class AircraftDTO(
    val name: String,
    val pointsCost: Int,
    val attackGroups: Map<String, List<WeaponDTO>>
)
