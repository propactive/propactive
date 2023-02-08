package io.github.propactive.logging.utils

import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestInstancePostProcessor
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * A handy extension that starts and stops all [LogsCollector]s in a test class.
 *
 * It is required to annotate the test class with [org.junit.jupiter.api.extension.ExtendWith].
 * The extension will find all [LogsCollector]s in the test class and start them before each test.
 * After each test, the extension will stop all [LogsCollector]s.
 */
class LogsCollectorExtension : BeforeTestExecutionCallback, AfterTestExecutionCallback, TestInstancePostProcessor {
    private var collectors: List<LogsCollector> = emptyList()

    @Throws(Exception::class)
    override fun postProcessTestInstance(testInstance: Any, context: ExtensionContext) {
        collectors = testInstance::class.memberProperties.asSequence()
            .filter { it.returnType == LogsCollector::class.createType() }
            .onEach { it.isAccessible = true }
            .map { it.getter.call(testInstance) as LogsCollector }
            .toList()
    }

    @Throws(Exception::class)
    override fun beforeTestExecution(context: ExtensionContext) =
        collectors.forEach(LogsCollector::start)

    @Throws(Exception::class)
    override fun afterTestExecution(context: ExtensionContext) =
        collectors.forEach(LogsCollector::stop)
}
