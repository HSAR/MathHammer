package io.hsar.mathhammer.statistics

object WoundCalculator {
    fun woundingHits(strength: Int, toughness: Int, reroll: Reroll = Reroll.NONE): Double {
        return when {
            (strength < toughness && strength <= (toughness / 2)) -> 6
            strength < toughness -> 5
            strength == toughness -> 4
            (strength > toughness && strength >= (toughness * 2)) -> 2
            strength > toughness -> 3
            else -> throw IllegalStateException("Should be impossible to reach here")
        }
            .let { toWoundTarget ->
                DiceProbability.averageChanceToPass(6, toWoundTarget, reroll)
                }
    }
}