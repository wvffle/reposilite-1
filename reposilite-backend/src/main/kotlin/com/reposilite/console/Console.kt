/*
 * Copyright (c) 2021 dzikoysk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.reposilite.console

import com.reposilite.VERSION
import com.reposilite.console.CommandStatus.FAILED
import com.reposilite.console.api.ExecutionResponse
import com.reposilite.console.api.ReposiliteCommand
import com.reposilite.failure.FailureFacade
import com.reposilite.journalist.Journalist
import com.reposilite.journalist.Logger
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.MissingParameterException
import picocli.CommandLine.UnmatchedArgumentException
import java.io.InputStream
import java.util.function.Consumer

@Command(name = "", version = ["Reposilite $VERSION"])
internal class Console(
    private val journalist: Journalist,
    failureFacade: FailureFacade,
    source: InputStream
) : Journalist {

    private val consoleThread = ConsoleThread(this, source, failureFacade)
    private val commandExecutor = CommandLine(this)

    fun execute(command: String): ExecutionResponse {
        logger.info("")
        val response = execute(command) { logger.info(it) }
        logger.info("")
        return response
    }

    fun execute(command: String, outputConsumer: Consumer<String>): ExecutionResponse {
        val response = executeCommand(command)

        response.response.forEach { message ->
            message.replace(System.lineSeparator(), "\n").split("\n").toTypedArray().forEach {
                outputConsumer.accept(it)
            }
        }

        return response
    }

    private fun executeCommand(command: String): ExecutionResponse {
        val processedCommand = command.trim()

        if (processedCommand.isEmpty()) {
            return ExecutionResponse(FAILED, emptyList())
        }

        val response: MutableList<String> = ArrayList()

        return try {
            val parseResult = commandExecutor.parseArgs(*processedCommand.split(" ").toTypedArray())
            val commandObject = parseResult.subcommand().commandSpec().userObject() as? ReposiliteCommand

            commandObject
                ?.let {
                    val context = CommandContext()
                    commandObject.execute(context)
                    ExecutionResponse(context.status, context.output())
                }
                ?: ExecutionResponse(FAILED, listOf(commandExecutor.usageMessage))
        }
        catch (unmatchedArgumentException: UnmatchedArgumentException) {
            ExecutionResponse(FAILED, listOf("Unknown command $processedCommand"))
        }
        catch (missingParameterException: MissingParameterException) {
            response.add(missingParameterException.message.toString())
            response.add("")
            response.add(missingParameterException.commandLine.usageMessage)
            ExecutionResponse(FAILED, response)
        }
    }

    fun registerCommand(command: ReposiliteCommand): CommandLine =
        commandExecutor.addSubcommand(command)

    fun getCommands(): Map<String, CommandLine> =
        commandExecutor.subcommands

    fun hook() =
        consoleThread.start()

    fun stop() =
        consoleThread.interrupt()

    override fun getLogger(): Logger =
        journalist.logger

}