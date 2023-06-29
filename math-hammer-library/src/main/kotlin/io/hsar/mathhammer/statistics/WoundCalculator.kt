package io.hsar.mathhammer.statistics

object WoundCalculator {

    fun woundTarget(strength: Int, toughness: Int) = when {
        strength <= (toughness / 2.0) -> 6
        strength < toughness -> 5
        strength == toughness -> 4
        strength >= (toughness * 2.0) -> 2
        strength > toughness -> 3
        else -> throw IllegalStateException("Unexpected failure in strength-toughness comparison")
    }

    fun woundingHits(criticalTarget: Int = 6, woundTarget: Int, reroll: Reroll = Reroll.NONE): Pair<Double, Double> =
        DiceProbability.averageCriticalsAndSuccesses(6, criticalTarget, woundTarget, reroll)
}