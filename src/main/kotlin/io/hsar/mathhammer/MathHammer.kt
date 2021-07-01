package io.hsar.mathhammer

import io.hsar.mathhammer.model.OffensiveProfile
import io.hsar.wh40k.combatsimulator.cli.input.AttackerDTO
import io.hsar.wh40k.combatsimulator.cli.input.DefenderDTO


class MathHammer(
        val numSimulations: Int,
        val attackers: Collection<AttackerDTO>,
        val defenders: Collection<DefenderDTO>) {

    fun runSimulation(): List<Result> {
        return (1..numSimulations)
                .map { roundNum ->
                    Result(
                            offensiveProfileToDamageDone = mapOf() // TODO
                    )
                }
    }
}

data class Result(
        val offensiveProfileToDamageDone: Map<OffensiveProfile, Double>
)