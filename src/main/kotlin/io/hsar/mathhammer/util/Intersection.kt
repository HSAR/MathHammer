package io.hsar.mathhammer.util

fun Map<String, Set<String>>.intersection(): Map<String, Set<String>> {
    return this.values.toList().let { setsOfElements ->
        if (setsOfElements.size == 1) {
            this
        } else {
            if (setsOfElements.size == 2) {
                setsOfElements.let { (first, second) ->
                    first.intersect(second)
                }
            } else {
                setsOfElements.fold( // Merge everything together
                    initial = emptySet<String>()
                ) { oldValues, newValues ->
                    oldValues.intersect(newValues)
                }
            }.let { mutualStrings ->
                this.keys.associate { key -> key to mutualStrings }
            }
        }
    }
}