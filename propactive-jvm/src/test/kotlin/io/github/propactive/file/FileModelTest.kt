package io.github.propactive.file

import io.github.propactive.support.utils.alphaNumeric
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import nl.jqno.equalsverifier.EqualsVerifier
import nl.jqno.equalsverifier.Warning
import org.junit.jupiter.api.Test
import java.io.File

import kotlin.random.Random

class FileModelTest {
    companion object {
        private val GIVEN_ENVIRONMENT = Random.alphaNumeric("environment")
        private val GIVEN_FILENAME = Random.alphaNumeric("Filename")
        private val GIVEN_CONTENT = Random.alphaNumeric("Content")
    }

    @Test
    fun `verify the equals and hashCode contract`() {
        EqualsVerifier
            .configure()
            .suppress(Warning.ALL_FIELDS_SHOULD_BE_USED)
            .forClass(FileModel::class.java)
            .verify()
    }

    @Test
    fun `should be considered equal when filename is the same`() {
        val fileA = FileModel(GIVEN_ENVIRONMENT, GIVEN_FILENAME, GIVEN_CONTENT)
        val fileB = FileModel("sameFilename", GIVEN_FILENAME, "sameContent")

        fileA shouldBe fileB
    }

    @Test
    fun `should be considered unequal when filename is different`() {
        val fileA = FileModel(GIVEN_ENVIRONMENT, GIVEN_FILENAME, GIVEN_CONTENT)
        val fileB = FileModel("differentFilename", "differentFilename", "differentContent")

        fileA shouldNotBe fileB
    }

    @Test
    fun `should write file to correct destination`() {
        val file = FileModel(GIVEN_ENVIRONMENT, GIVEN_FILENAME, GIVEN_CONTENT)
        val destination = File.createTempFile("destination", "dir").parent

        file.write(destination)

        File(destination, GIVEN_FILENAME).shouldExist()
    }

    @Test
    fun `should write file with correct content`() {
        val file = FileModel(GIVEN_ENVIRONMENT, GIVEN_FILENAME, GIVEN_CONTENT)
        val destination = File.createTempFile("destination", "dir").parent

        file.write(destination)

        File(destination, GIVEN_FILENAME).readText() shouldBe GIVEN_CONTENT
    }

    @Test
    fun `should not use filename override when it is blank`() {
        val file = FileModel(GIVEN_ENVIRONMENT, GIVEN_FILENAME, GIVEN_CONTENT)
        val destination = File.createTempFile("destination", "dir").parent
        val filenameOverride = ""

        file.write(destination, filenameOverride)

        File(destination, GIVEN_FILENAME).shouldExist()
    }

    @Test
    fun `should write file with correct filename when filename override is provided`() {
        val file = FileModel(GIVEN_ENVIRONMENT, GIVEN_FILENAME, GIVEN_CONTENT)
        val destination = File.createTempFile("destination", "dir").parent
        val filenameOverride = Random.alphaNumeric("filenameOverride")

        file.write(destination, filenameOverride)

        File(destination, filenameOverride).shouldExist()
    }
}
