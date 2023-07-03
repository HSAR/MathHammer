package io.hsar.mathhammer.statistics

import io.hsar.mathhammer.model.AttackResult

object KillsCalculator {

    fun getKills(targetWounds: Int, ignoreWoundsTarget: Int? = null, attackResults: List<AttackResult>): Int {
        val damagingHits = attackResults
            .flatMap { eachAttackResult ->
                ((1..eachAttackResult.unsavedHits.toInt()).map { 1.0 } + // whole attacks cause full damage
                        listOf(eachAttackResult.unsavedHits % 1.0)) // partial attacks cause partial damage
                    .map { eachHit ->
                        eachHit * eachAttackResult.damagePerHit // scale by damage
                    }
            }

        var currentKills = 0
        var currentDamagePool = 0.0

        damagingHits.forEach { damagingHit ->
            /*
            TODO WARNING this doesn't accurately account for the statistical impact of the FNP ability
            For example, a FNP 6+ on a 3W unit receiving 2x D3 hits:
                Correct: (5/6 ^ 3) chance to kill immediately, passing remaining damage to second model
                As calculated here: 2.5 damage done twice and no damage passed to second model
            */
            val damageFactor = ignoreWoundsTarget?.let { SaveCalculator.failedSaves(save = it) } ?: 1.0
            currentDamagePool += (damagingHit * damageFactor)
            if (currentDamagePool >= targetWounds) {
                currentKills += 1
                currentDamagePool = 0.0
            }
        }

        return currentKills
    }
}