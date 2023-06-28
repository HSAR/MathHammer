package io.hsar.mathhammer.statistics

import kotlin.math.absoluteValue

object SaveCalculator {
    fun failedSaves(AP: Int = 0, save: Int, invuln: Int = 7): Double {
        return (save + AP.absoluteValue)
            .let { modifiedSave ->
                Math.min(modifiedSave, invuln)
                    .let { saveToUse ->
                        if (saveToUse >= 7) {
                            0.0 // no chance to save if the target is 7 on a rolling d6
                        } else {
                            DiceProbability.averageSuccesses(6, saveToUse)
                        }
                    }
            }
            .let { chanceToSave ->
                1.0 - chanceToSave // saves work the reverse of hitting and wounding - a successful save is bad
            }
    }
}