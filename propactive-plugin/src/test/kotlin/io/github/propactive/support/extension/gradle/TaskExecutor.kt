package io.github.propactive.support.extension.gradle

import io.github.propactive.support.extension.gradle.TaskExecutor.Result
import io.github.propactive.support.extension.project.ProjectDirectory
import java.io.StringWriter
import java.io.Writer
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome

/**
 * A utility class for executing Gradle tasks on a project directory.
 *
 * This class is responsible for executing Gradle tasks on a given project directory and returning the [Result].
 * It also provides the ability to set the [log] level for the task execution and [expectFailure] for the task execution.
 *
 * @property projectDirectory The project directory to execute the tasks on.
 */
class TaskExecutor(
    private val projectDirectory: ProjectDirectory,
) {
    private val stdOutputWriter: Writer = StringWriter()
    private val stdErrorWriter: Writer = StringWriter()
    private var logLevel: String = "--debug"
    private var expectFailure: Boolean = false

    /**
     * The result of a task execution.
     *
     * @property outcome The outcome of the task execution.
     * @property output The output of the task execution.
     */
    data class Result(
        val outcome: Outcome,
        val output: String,
    )

    enum class Outcome {
        SUCCESS, FAILED, UP_TO_DATE, SKIPPED, FROM_CACHE, NO_SOURCE
    }

    enum class Level {
        ERROR, WARN, INFO, DEBUG, TRACE
    }

    /**
     * Set the log [level] for the task execution, default is DEBUG.
     *
     * WARNING: These are log levels for the Gradle build API, not the log levels for the project itself.
     */
    fun log(level: Level): TaskExecutor = apply {
        logLevel = when (level) {
            Level.ERROR -> "--error"
            Level.WARN -> "--warn"
            Level.INFO -> "--info"
            Level.DEBUG -> "--debug"
            Level.TRACE -> "--stacktrace"
        }
    }

    /**
     * Enable this flag to prime the executor to expect a failure.
     */
    fun expectFailure() = apply {
        expectFailure = true
    }

    /**
     * Execute the given task and return the result.
     *
     * @param task The task to execute.
     * @return The [Result] of the task execution.
     */
    fun execute(task: String): Result = GradleRunner
        .create()
        .forwardStdOutput(stdOutputWriter)
        .forwardStdError(stdErrorWriter)
        .withProjectDir(projectDirectory)
        .withPluginClasspath()
        .withArguments(task, logLevel)
        .run { if (expectFailure) buildAndFail() else build() }
        .let { result ->
            val t = checkNotNull(result.task(":$task")) { "Task not found: $task" }
            Result(fromTask(t.outcome), result.output ?: "")
        }
        .apply { outputFilteredLogs() }

    private fun outputFilteredLogs() {
        val propactiveLogId = "io.github.propactive.plugin.Propactive"
        val startOfANewLogEntry = { line: String -> Regex("""^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}\+\d{4} \[\w+]""").containsMatchIn(line) }
        val isAPropactiveLog = { line: String -> propactiveLogId in line }
        var isARelevantLine = false

        (stdOutputWriter.toString() + stdErrorWriter.toString())
            .lineSequence()
            .filter { line ->
                if (startOfANewLogEntry(line)) isARelevantLine = isAPropactiveLog(line)
                isARelevantLine // As long as a new log entry line is relevant, keep it.
            }
            .joinToString("\n") { line ->
                line
                    .replace(Regex("""\[${Regex.escape(propactiveLogId)}] """), "")
                    .replace(Regex("""\+0000"""), "")
            }
            .also(::println)
    }

    private fun fromTask(outcome: TaskOutcome): Outcome = when (outcome) {
        TaskOutcome.SUCCESS -> Outcome.SUCCESS
        TaskOutcome.FAILED -> Outcome.FAILED
        TaskOutcome.UP_TO_DATE -> Outcome.UP_TO_DATE
        TaskOutcome.SKIPPED -> Outcome.SKIPPED
        TaskOutcome.FROM_CACHE -> Outcome.FROM_CACHE
        TaskOutcome.NO_SOURCE -> Outcome.NO_SOURCE
    }
}
