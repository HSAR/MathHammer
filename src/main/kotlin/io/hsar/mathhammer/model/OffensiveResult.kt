package io.hsar.mathhammer.model

data class OffensiveResult(val expectedDamage: Double, val expectedKills: Int): Comparable<OffensiveResult> {

    override fun compareTo(other: OffensiveResult): Int {

        return if (this.expectedKills == other.expectedKills) {
            this.expectedDamage.compareTo(other.expectedDamage)
        } else {
            this.expectedKills.compareTo(other.expectedKills)
        }
    }
}