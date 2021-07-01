package io.hsar.mathhammer.statistics

object HitCalculator {
    fun hits(skill: Int, attacks: Int): Double {
        return (skill / 6.0) * attacks
    }
}