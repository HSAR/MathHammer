package io.hsar.mathhammer.statistics

object HitCalculator {
    fun hits(skill: Int, attacks: Double): Double {
        return DiceProbability.averageChanceToPass(6, skill) * attacks
    }
}