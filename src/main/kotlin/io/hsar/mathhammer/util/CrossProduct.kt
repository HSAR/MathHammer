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

fun <T> createCrossProduct(thingsToCrossProduct: Collection<Set<T>>): Set<List<T>> {
    return if (thingsToCrossProduct.size == 1) {
        setOf(thingsToCrossProduct.first().toList()) // reformat into correct structure
    } else {
        if (thingsToCrossProduct.size == 2) {
            thingsToCrossProduct.toList().let { (first, second) ->
                cartesianProduct(first, second)
            }
        } else {
            val firstTwoAttackGroupKeys = thingsToCrossProduct.take(2)
            val remainingAttackGroupKeys = thingsToCrossProduct - firstTwoAttackGroupKeys

            firstTwoAttackGroupKeys.let { (first, second) ->
                cartesianProduct(first, second, *remainingAttackGroupKeys.toTypedArray())
            }
        }
    }
}