package io.hsar.mathhammer.model

import io.hsar.wh40k.combatsimulator.cli.input.DefenderDTO

data class UnitResult(
    val unitName: String,
    val pointsCost: Double,
    val defender: DefenderDTO,
    val expectedDamage: Double,
    val expectedKills: Int,
    val offensivesToResults: Map<OffensiveProfile, List<AttackResult>>
) : Comparable<UnitResult> {

    override fun compareTo(other: UnitResult): Int {
        return if (this.expectedKills == other.expectedKills) {
            this.expectedDamage.compareTo(other.expectedDamage)
        } else {
            this.expectedKills.compareTo(other.expectedKills)
        }
    }

}