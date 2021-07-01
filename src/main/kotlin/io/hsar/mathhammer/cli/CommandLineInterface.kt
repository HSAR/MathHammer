package io.hsar.wh40k.combatsimulator.cli

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.hsar.mathhammer.MathHammer
import io.hsar.wh40k.combatsimulator.cli.input.AttackerDTO
import io.hsar.wh40k.combatsimulator.cli.input.DefenderDTO
import java.io.File
import kotlin.system.exitProcess

abstract class Command(val name: String) {
    abstract fun run()
}

class SimulateCombat : Command("math-hammer") {

    @Parameter(names = arrayOf("--attacker", "--attackers"), description = "Path to an input file describing unit profile(s) attacking", required = true)
    private var attackerFilePath = ""

    @Parameter(names = arrayOf("--defender", "--defenders"), description = "Path to an input file describing unit profile(s) defending", required = true)
    private var defenderFilePath = ""

    override fun run() {
        // Run the number of simulations requested
        MathHammer(
                // Read and parse input files
                attackers = objectMapper.readValue<List<AttackerDTO>>(File(attackerFilePath).readText()),
                defenders = objectMapper.readValue<List<DefenderDTO>>(File(defenderFilePath).readText())
        )
                .runSimulation()
                .map { (attackResults, offensiveProfile) ->
                    "${offensiveProfile.name} expects ${attackResults.expectedKills} kills against ${attackResults.targetName}: ${attackResults.expectedDamage} damage."
                }
                .also { result ->
                    println(objectWriter.writeValueAsString(result))
                }
    }

    private fun List<Int>.average() = this.sum() / this.count().toDouble()

    private fun Double.toPercentage() = this * 100

    companion object {
        private val objectMapper = jacksonObjectMapper()
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .enable(SerializationFeature.INDENT_OUTPUT)
        private val objectWriter = objectMapper.writer(
                DefaultPrettyPrinter()
                        .also { it.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE) }
        )

    }
}

fun main(args: Array<String>) {
    val instances: Map<String, Command> = listOf(
            SimulateCombat()
    )
            .associateBy { it.name }
    val commander = JCommander()
    instances.forEach { name, command -> commander.addCommand(name, command) }

    if (args.isEmpty()) {
        commander.usage()
        System.err.println("Expected some arguments")
        exitProcess(1)
    }

    try {
        commander.parse(*args)
        val command = instances[commander.parsedCommand]
        command!!.run()
    } catch (e: Exception) {
        e.printStackTrace()
        exitProcess(1)
    }
}
