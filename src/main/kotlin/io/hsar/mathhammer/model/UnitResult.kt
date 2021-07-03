package io.hsar.mathhammer.model

data class UnitResult(val unitName: String, val pointsCost: Double, val offensivesToResults: Map<OffensiveProfile, OffensiveResult>) : Comparable<UnitResult> {

    val expectedKills = offensivesToResults.values.map { it.expectedKills }.sum() // TODO: This isn't how kills sum - use the kills calculator to calculate it by attack results
    val expectedDamage = offensivesToResults.values.map { it.expectedDamage }.sum()

    override fun compareTo(other: UnitResult): Int {
        return if (this.expectedKills == other.expectedKills) {
            this.expectedDamage.compareTo(other.expectedDamage)
        } else {
            this.expectedKills.compareTo(other.expectedKills)
        }
    }

}