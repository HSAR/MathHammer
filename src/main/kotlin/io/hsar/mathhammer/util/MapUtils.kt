package io.hsar.wh40k.combatsimulator.utils

fun <K, V> List<Map<K, V>>.sum(): Map<K, V> {
    return this.fold( // Merge everything together
        initial = emptyMap()
    ) { oldValues, newValues ->
        oldValues.plus(newValues)
    }
}