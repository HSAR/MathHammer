package io.hsar.mathhammer.statistics

import io.hsar.mathhammer.model.AttackResult
import io.hsar.mathhammer.model.OffensiveResult
import io.hsar.wh40k.combatsimulator.cli.input.DefenderDTO

object KillsCalculator {
    fun getOffensiveResult(defensiveProfile: DefenderDTO, attackResults: List<AttackResult>): OffensiveResult {
        val damagingHits = attackResults
                .flatMap { eachAttackResult ->
                    ((1..eachAttackResult.expectedHits.toInt()).map { 1.0 } + // whole attacks cause full damage
                            listOf(eachAttackResult.expectedHits % 1)) // partial attacks cause partial damage
                            .map { eachHit ->
                                eachHit * eachAttackResult.damagePerHit // scale by damage
                            }
                }

        var currentKills = 0
        var currentDamagePool = 0.0

        damagingHits.forEach { damagingHit ->
            currentDamagePool += damagingHit
            if (currentDamagePool > defensiveProfile.wounds) {
                currentKills += 1
                currentDamagePool = 0.0
            }
        }

        return OffensiveResult(
                expectedDamage = damagingHits.sum(),
                expectedKills = currentKills,
                attackResults = attackResults
        )
    }
}