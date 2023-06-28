package io.hsar.mathhammer.util

/**
 * For a collection of pairs, calculate an average of all first values and all second values separately.
 * @return A pair of average first values to average second values.
 */
fun Collection<Pair<Double, Double>>.average(): Pair<Double, Double> {
    val firstAverage = this.map { it.first }.average()
    val secondAverage = this.map { it.second }.average()

    return firstAverage to secondAverage
}