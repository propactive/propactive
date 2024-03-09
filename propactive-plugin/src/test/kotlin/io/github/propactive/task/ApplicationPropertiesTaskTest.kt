package io.github.propactive.task

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldHaveAnnotation
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties

class ApplicationPropertiesTaskTest {

    @Nested
    inner class Cache {
        @Test
        fun `should mark task as cacheable`() {
            ApplicationPropertiesTask::class.java
                .shouldHaveAnnotation(CacheableTask::class.java)
        }

        @Test
        fun `should mark configuration environments as stable input`() {
            ApplicationPropertiesTask::environments
                .shouldHaveAnnotation(Input::class)
        }

        @Test
        fun `should mark configuration implementationClass as stable input`() {
            ApplicationPropertiesTask::implementationClass
                .shouldHaveAnnotation(Input::class)
        }

        @Test
        fun `should mark configuration filenameOverride as stable input`() {
            ApplicationPropertiesTask::filenameOverride
                .shouldHaveAnnotation(Input::class)
        }

        @Test
        fun `should mark configuration compiledClasses as stable inputFiles with relative path`() {
            ApplicationPropertiesTask::compiledClasses
                .apply { shouldHaveAnnotation(InputFiles::class) }
                .shouldHaveAnnotation(PathSensitive::class)
                .let { it as PathSensitive }
                .value shouldBe RELATIVE
        }

        @Test
        fun `should mark configuration destination as stable output directory`() {
            ApplicationPropertiesTask::destination
                .shouldHaveAnnotation(OutputDirectory::class)
        }
    }

    private fun <V> KProperty<V>.shouldHaveAnnotation(annotation: KClass<out Any>) =
        ApplicationPropertiesTask::class.memberProperties
            .first { it.name == this.name }
            .getter
            .annotations
            .find { it.annotationClass == annotation }
            ?: fail { "Expected to find '$annotation' annotation on '${this.name}' property" }
}
