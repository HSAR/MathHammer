package io.hsar.mathhammer.statistics

object DiceStringParser {
    data class DiceInHand(val number: Int, val value: Int)

    fun expectedValue(diceString: String): Double = parseString(diceString)
        .let { (numDice, diceValue) ->
            numDice * diceValue
                .plus(1).div(2.0) // gives us the expected value
        }


    fun maxValue(diceString: String) = parseString(diceString)
        .let { (numDice, diceValue) ->
            numDice * diceValue
        }
        .toDouble()

    private fun parseString(diceString: String) = diceString.lowercase()
        .split("d") // remove dice character
        .let { (numberOfDice, diceValue) ->
            val diceMax = diceValue.toInt() // this is the dice number, e.g. 6 for d6
            if (numberOfDice.isEmpty()) {
                DiceInHand(1, diceMax)
            } else {
                DiceInHand(numberOfDice.toInt(), diceMax)
            }
        }


}