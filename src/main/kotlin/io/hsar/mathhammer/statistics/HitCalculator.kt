package io.hsar.mathhammer.statistics

object HitCalculator {
    fun hits(skill: Int): Double {
        return DiceProbability.averageChanceToPass(6, skill)
    }
}