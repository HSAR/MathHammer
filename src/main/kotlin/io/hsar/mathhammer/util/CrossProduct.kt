package io.hsar.mathhammer.util

/**
 * Credit: https://stackoverflow.com/a/62378736
 */
fun <T> cartesianProduct(a: Set<T>, b: Set<T>, vararg sets: Set<T>): Set<List<T>> =
    (setOf(a, b).plus(sets))
        .fold(listOf(listOf<T>())) { acc, set ->
            acc.flatMap { list -> set.map { element -> list + element } }
        }
        .toSet()