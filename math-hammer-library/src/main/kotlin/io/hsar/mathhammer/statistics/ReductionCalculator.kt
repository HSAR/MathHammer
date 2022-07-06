package io.hsar.mathhammer.statistics

import kotlin.math.abs
import kotlin.math.max

object ReductionCalculator {
    fun effectiveNumber(reduction: Int, value: Int) = max(0, abs(value) - abs(reduction))
    fun effectiveNumber(reduction: Double, value: Double) = max(0.0, abs(value) - abs(reduction))
}