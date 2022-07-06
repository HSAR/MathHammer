package io.hsar.wh40k.combatsimulator.utils

fun <K, V> Map<K, V>.mergeWithReplacement(otherMap: Map<K, V>): Map<K, V> {
    return this.toMutableMap()
        .also { tempMutableMap ->
            otherMap.forEach { (key, value) ->
                tempMutableMap.merge(key, value) { _, valueB ->
                    valueB
                }
            }
        }
}

fun <K, V> List<Map<K, V>>.sum(): Map<K, V> {
    return this.fold( // Merge everything together
        initial = emptyMap()
    ) { oldValues, newValues ->
        oldValues.mergeWithReplacement(newValues)
    }
}