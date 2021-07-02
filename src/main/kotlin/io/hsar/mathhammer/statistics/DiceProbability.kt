package io.hsar.mathhammer.statistics

object DiceProbability {

    /**
     * Returns the expectation of dice that will MEET OR EXCEED the target.
     */
    fun averageChanceToPass(diceNumber: Int, diceTarget: Int): Double {
        return (1..diceNumber)
                .map { currentRoll ->
                    when {
                        currentRoll == 1 -> 0 // rolling 1 always fails
                        currentRoll == diceNumber -> 1 // rolling max always succeeds
                        currentRoll >= diceTarget -> 1
                        else -> 0
                    }
                }
                .average()
    }
}