package io.hsar.mathhammer.statistics

object DiceProbability {

    /**
     * Returns the expectation of dice that will MEET OR EXCEED the target.
     */
    fun averageChanceToPass(diceNumber: Int, diceTarget: Int, rerolls: Reroll = Reroll.NONE): Double {
        return (1..diceNumber)
            .map { currentRoll ->
                when {
                    currentRoll == 1 -> 0.0 // rolling 1 always fails
                    currentRoll == diceNumber -> 1.0 // rolling max always succeeds
                    currentRoll >= diceTarget -> 1.0
                    else -> 0.0
                }.let { rollSucceeded ->
                    // If we didn't hit, re-roll if allowed
                    if (rollSucceeded != 1.0) {
                        if (rerolls == Reroll.ONES && currentRoll == 1) {
                            averageChanceToPass(diceNumber, diceTarget, Reroll.NONE) // Rerolls don't continue
                        } else if (rerolls == Reroll.ALL) {
                            averageChanceToPass(diceNumber, diceTarget, Reroll.NONE) // Rerolls don't continue
                        } else {
                            rollSucceeded // No re-roll allowed
                        }
                    } else {
                        rollSucceeded // No re-roll needed
                    }
                }
            }
            .average()
    }

}