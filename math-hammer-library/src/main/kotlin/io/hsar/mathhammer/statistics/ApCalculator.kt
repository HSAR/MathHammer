package io.hsar.mathhammer.statistics

import java.lang.Math.abs
import kotlin.math.max

object ApCalculator {
    /**
     * Calculate AP reduction.
     * @return a positive integer representing AP
     */
    fun effectiveAP(apReduction: Int, apOfAttack: Int) = max(0, abs(apOfAttack) - abs(apReduction))
}