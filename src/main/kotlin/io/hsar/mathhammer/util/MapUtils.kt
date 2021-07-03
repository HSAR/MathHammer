package io.hsar.wh40k.combatsimulator.utils

fun <K, V> Map<K, V>.mergeWithAddition(otherMap: Map<K, V>): Map<K, V> {
    return this.toMutableMap()
        .also { tempMutableMap ->
            otherMap.forEach { (key, value) ->
                tempMutableMap.merge(key, value) { valueA, valueB ->
                    valueB
                }
            }
        }
}

fun <K, V> List<Map<K, V>>.sum(): Map<K, V> {
    return this.fold( // Merge everything together
        initial = emptyMap()
    ) { oldValues, newValues ->
        oldValues.mergeWithAddition(newValues)
    }
}