package io.hsar.wh40k.combatsimulator.utils

fun <K, L, V> Map<K, Map<L, V>>.mergeWithAddition(otherMap: Map<K, Map<L, V>>): Map<K, Map<L, V>> {
    return this.toMutableMap()
        .also { tempMutableMap ->
            otherMap.forEach { (key, value) ->
                tempMutableMap.merge(key, value) { valueA, valueB ->
                    valueA + valueB
                }
            }
        }
}

fun <K, L, V> List<Map<K, Map<L, V>>>.sum(): Map<K, Map<L, V>> {
    return this.fold( // Merge everything together
        initial = emptyMap()
    ) { oldValues, newValues ->
        oldValues.mergeWithAddition(newValues)
    }
}