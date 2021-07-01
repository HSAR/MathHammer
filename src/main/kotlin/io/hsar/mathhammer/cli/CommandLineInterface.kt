package io.hsar.wh40k.combatsimulator.cli

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.hsar.mathhammer.MathHammer
import io.hsar.wh40k.combatsimulator.cli.input.AttackerDTO
import io.hsar.wh40k.combatsimulator.cli.input.DefenderDTO
import kotlin.system.exitProcess

abstract class Command(val name: String) {
    abstract fun run()
}

class SimulateCombat : Command("math-hammer") {

    @Parameter(names = arrayOf("--attacker", "--attackers"), description = "Path to an input file describing unit profile(s) attacking", required = true)
    private var attackerFilePath = ""

    @Parameter(names = arrayOf("--defender", "--defenders"), description = "Path to an input file describing unit profile(s) defending", required = true)
    private var defenderFilePath = ""

    @Parameter(names = arrayOf("--simulations", "--times"), description = "How many times to run the simulation (default: 10)", required = false)
    private var numRuns = 10

    override fun run() {
        // Read and parse input files
        val attackers = objectMapper.readValue<List<AttackerDTO>>(attackerFilePath)
        val defenders = objectMapper.readValue<List<DefenderDTO>>(attackerFilePath)
        // Run the number of simulations requested
        (1..numRuns)
                .map { runNumber ->
                    println("===== SIMULATION STARTING (#${runNumber.toString().padStart(3, '0')}) =====")
                    // Create World and CombatSimulation instances, then initiate combat
                    MathHammer(
                            attackers = attackers,
                            defenders = defenders,
                            numSimulations = numRuns
                    )
                            .runSimulation()
                            .also { result ->
                                println("===== SIMULATION RESULTS (#${runNumber.toString().padStart(3, '0')}) =====")
                                println(objectWriter.writeValueAsString(result))
                                println("===== SIMULATION COMPLETE (#${runNumber.toString().padStart(3, '0')}) =====")
                            }
                }
                .let { results ->
                    // Process results for summarised digest
                    println("===== ALL SIMULATIONS COMPLETE ($numRuns) =====")
                    println("===== SUMMARY STARTS =====")
                    println(results)
                    println("===== SUMMARY ENDS =====")
                }
    }

    private fun List<Int>.average() = this.sum() / this.count().toDouble()

    private fun Double.toPercentage() = this * 100

    companion object {
        private val objectMapper = jacksonObjectMapper()
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        private val objectWriter = objectMapper.writerWithDefaultPrettyPrinter()
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
