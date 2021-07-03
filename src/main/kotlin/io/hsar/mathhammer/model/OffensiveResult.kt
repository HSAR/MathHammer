package io.hsar.mathhammer.model

data class OffensiveResult(val attacksMade: Double, val expectedDamage: Double, val expectedKills: Int, val attackResults: List<AttackResult>) : Comparable<OffensiveResult> {
    override fun compareTo(other: OffensiveResult): Int {
        return if (this.expectedKills == other.expectedKills) {
            this.expectedDamage.compareTo(other.expectedDamage)
        } else {
            this.expectedKills.compareTo(other.expectedKills)
        }
    }
}