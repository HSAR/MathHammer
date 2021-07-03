package io.hsar.mathhammer.cli.input

import io.hsar.wh40k.combatsimulator.cli.input.AttackerTypeDTO

data class UnitDTO(
    val name: String,
    val unitComposition: Map<String, Int>,
    val models: Map<String, AttackerTypeDTO>
) {
    fun attackerComposition(): Map<AttackerTypeDTO, Int> {
        return unitComposition.mapKeys { (attackerName, _) ->
            models.getOrElse(attackerName) { throw IllegalArgumentException("Could not find a model matching the name [$attackerName] in the unit [$name]") }
        }
    }

    fun getAttackerName(attackerTypeDTO: AttackerTypeDTO): String {
        return (models.entries.find { it.value == attackerTypeDTO } ?: throw IllegalArgumentException("Could not find AttackerDTO $attackerTypeDTO")).key
    }
}