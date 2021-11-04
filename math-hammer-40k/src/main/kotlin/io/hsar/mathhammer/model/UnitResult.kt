package io.hsar.mathhammer.model

data class UnitResult(
    val unitName: String,
    val pointsCost: Double,
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