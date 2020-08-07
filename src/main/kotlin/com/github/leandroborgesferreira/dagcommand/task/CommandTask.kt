package com.github.leandroborgesferreira.dagcommand.task

import com.github.leandroborgesferreira.dagcommand.domain.AdjacencyList
import com.github.leandroborgesferreira.dagcommand.domain.Config
import com.github.leandroborgesferreira.dagcommand.enums.OutputType
import com.github.leandroborgesferreira.dagcommand.logic.*
import com.github.leandroborgesferreira.dagcommand.output.writeToFile
import com.github.leandroborgesferreira.dagcommand.utils.CommandExec
import com.google.gson.Gson
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

private const val OUTPUT_DIRECTORY_NAME = "dag-command"
private const val OUTPUT_FILE_NAME_AFFECTED = "affected-modules.txt"
private const val OUTPUT_FILE_NAME_GRAPH = "adjacencies-list.json"
private const val OUTPUT_FILE_NAME_EDGE_LIST = "edge-list.json"

open class CommandTask : DefaultTask() {

    @Input
    lateinit var config: Config

    @TaskAction
    private fun command() {
        printConfig(project, config)

        val adjacencyList: AdjacencyList = parseAdjacencyList(project, config).also {
            if (config.printAdjacencyList) {
                commandWithFeedback("Writing adjacency list... ") {
                    writeToFile(
                        File(buildPath(), OUTPUT_DIRECTORY_NAME),
                        OUTPUT_FILE_NAME_GRAPH,
                        Gson().toJson(it)
                    )
                }
            }
        }

        if (config.printDataFrame) {
            createDataFrame(adjacencyList).let { frames ->
                commandWithFeedback("Writing edges list...") {
                    writeToFile(
                        File(buildPath(), OUTPUT_DIRECTORY_NAME),
                        OUTPUT_FILE_NAME_EDGE_LIST,
                        Gson().toJson(frames)
                    )
                }
            }
        }

        val changedModules: List<String> = changedModules(CommandExec, config.defaultBranch).also { modules ->
            println("Changed modules: ${modules.joinToString()}\n")
        }

        val affectedModules: Set<String> = affectedModules(adjacencyList, changedModules).also { modules ->
            println("Affected modules: ${modules.joinToString()}\n")
        }

        when (config.outputType) {
            OutputType.FILE -> commandWithFeedback("Writing affected modules list... ") {
                writeToFile(File(buildPath(), OUTPUT_DIRECTORY_NAME), OUTPUT_FILE_NAME_AFFECTED, affectedModules)
            }
            OutputType.CONSOLE -> {
                //Console was already printed
            }
        }
    }

    private fun buildPath() = project.buildDir.path
}

private fun printConfig(project: Project, config: Config) {
    println("--- Config ---")
    println("Filter: ${config.filter.value}")
    println("Default branch: ${config.defaultBranch}")
    println("Output type: ${config.outputType.value}")
    println("Output path: ${project.buildDir.path}")
    println("--------------\n")
}

private fun commandWithFeedback(message: String, func: () -> Unit) {
    print(message)
    func()
    print("Done\n\n")
}

private fun printAffectedGraph(set: Set<String>) {
    println("Affected modules: ${set.joinToString()}")
}
