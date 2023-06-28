package io.hsar.mathhammer.statistics

import io.hsar.mathhammer.util.average

object DiceProbability {

    /**
     * Returns the expectation of dice that will meet or exceed the target, with a given re-roll strategy.
     */
    fun averageSuccesses(diceNumber: Int, successTarget: Int, rerolls: Reroll = Reroll.NONE): Double =
        averageCriticalsAndSuccesses(diceNumber, successTarget, successTarget, rerolls).first

    /**
     * Returns a pair of expected values: dice that will meet or exceed a critical threshold,
     * and dice that will meet or exceed a lower success threshold. Re-rolls will apply to failed rolls only.
     */
    fun averageCriticalsAndSuccesses(
        diceNumber: Int,
        criticalTarget: Int,
        successTarget: Int,
        rerolls: Reroll = Reroll.NONE
    ): Pair<Double, Double> {
        require(criticalTarget >= successTarget) { "Critical target must be >= success target, but: critTarget=$criticalTarget, successTarget=$successTarget." }
        return (1..diceNumber)
            .map { currentRoll ->
                when {
                    currentRoll == 1 -> 0.0 to 0.0 // rolling 1 always fails
                    currentRoll == diceNumber -> 1.0 to 0.0 // rolling max always crits
                    currentRoll >= criticalTarget -> 1.0 to 0.0
                    currentRoll >= successTarget -> 0.0 to 1.0
                    else -> 0.0 to 0.0
                }.let { initialResult ->
                    val (critical, success) = initialResult
                    // If we didn't hit, re-roll if allowed
                    if (critical != 1.0 && success != 1.0) {
                        if (rerolls == Reroll.ALL || (rerolls == Reroll.ONES && currentRoll == 1)) {
                            averageCriticalsAndSuccesses(
                                diceNumber,
                                criticalTarget,
                                successTarget,
                                Reroll.NONE
                            ) // Rerolls don't continue
                        } else {
                            initialResult // No re-roll allowed
                        }
                    } else {
                        initialResult // No re-roll needed
                    }
                }
            }
            .average()
    }

}