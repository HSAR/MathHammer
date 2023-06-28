package io.hsar.mathhammer.statistics

object HitCalculator {
    /**
     * Return the expected number of hits for a given success target number, critical target number and re-roll strategy.
     * Returns a pair of critical hits to successful hits.
     */
    fun hits(criticalTarget: Int, successTarget: Int, reroll: Reroll = Reroll.NONE): Pair<Double, Double> {
        return DiceProbability.averageCriticalsAndSuccesses(6, criticalTarget, successTarget, reroll)
    }
}