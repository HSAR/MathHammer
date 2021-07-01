package io.hsar.mathhammer.model

class AttackResult(val expectedHits: Double, val damage: Int, val expectedKills: Int, val targetName: String): Comparable<AttackResult> {
    val expectedDamage: Double
        get() = damage * expectedHits

    override fun compareTo(other: AttackResult): Int {

        return if (this.expectedKills == other.expectedKills) {
            this.expectedDamage.compareTo(other.expectedDamage)
        } else {
            this.expectedKills.compareTo(other.expectedKills)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AttackResult

        if (expectedHits != other.expectedHits) return false
        if (damage != other.damage) return false
        if (targetName != other.targetName) return false
        if (expectedDamage != other.expectedDamage) return false

        return true
    }

    override fun hashCode(): Int {
        var result = expectedHits.hashCode()
        result = 31 * result + damage
        result = 31 * result + targetName.hashCode()
        return result
    }


}