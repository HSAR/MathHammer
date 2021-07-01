package io.hsar.mathhammer.statistics

object HitCalculator {
    fun hits(skill: Int, attacks: Double): Double {
        return (skill / 6.0) * attacks
    }
}