package io.hsar.mathhammer.statistics

object DiceStringParser {
    fun expectedValue(diceString: String): Double {
        return diceString
            .split("d") // remove dice character
            .let { (numberOfDice, diceValue) ->
                val numDice = if (numberOfDice.isEmpty()) {
                    1
                } else {
                    numberOfDice.toInt()
                }
                numDice * diceValue
                    .toInt() // this is the dice number, e.g. 6 for d6
                    .plus(1).div(2.0) // gives us the expected value
            }
    }
}