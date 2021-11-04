package io.hsar.mathhammer.statistics

object HitCalculator {
    fun hits(skill: Int, reroll: Reroll = Reroll.NONE): Double {
        return DiceProbability.averageChanceToPass(6, skill, reroll)
    }
}