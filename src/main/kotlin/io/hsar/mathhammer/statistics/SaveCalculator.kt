package io.hsar.mathhammer.statistics

object SaveCalculator {
    fun failedSaves(AP: Int, save: Int, invuln: Int = 7): Double {
        return (save + AP)
                .let { modifiedSave ->
                    Math.min(modifiedSave, invuln)
                            .let { saveToUse ->
                                if (saveToUse >= 7) {
                                    0.0 // no chance to save if the target is 7 on a rolling d6
                                } else {
                                    saveToUse / 6.0
                                }
                            }
                }
                .let { chanceToSave ->
                    1.0 - chanceToSave
                }
    }
}