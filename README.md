# Propactive

[![CICD](https://github.com/u-ways/propactive/actions/workflows/CICD.yml/badge.svg)](https://github.com/u-ways/propactive/actions/workflows/CICD.yml)
[![Code Climate](https://badgen.net/badge/CodeClimate/A/green)](https://codeclimate.com/github/propactive/propactive)
[![Latest Version](https://img.shields.io/github/v/release/U-ways/propactive)](https://github.com/u-ways/propactive/releases)
[![GitHub License](https://badgen.net/badge/license/MIT/blue)](https://github.com/u-ways/propactive/blob/master/LICENSE)

An application property generator framework that validates and generates your `application.properties` file on runtime.

## Table of Contents

- [Setup](#setup)
- [Gradle Plugin configurations](#gradle-plugin-configurations)
- [Plugin Tasks](#plugin-tasks)
- [Runtime Property Validation](#runtime-property-validation)
- [Natively Supported Property Types](#natively-supported-property-types)
- [Writing Your Custom Property Types](#writing-your-custom-property-types)
- [Working With Multiple Environments](#working-with-multiple-environments)
- [Blank Values For Properties (Relaxing Mandatory Values Condition)](#blank-values-for-properties-relaxing-mandatory-values-condition)
- [Further Test Support With The Environment Factory](#further-test-support-with-the-environment-factory)
- [Demo Project (w/ a CICD Pipeline)](#demo-project-w-a-cicd-pipeline)

## Setup

Using the `plugins` & `dependencies` blocks, you can set up Propactive as follows:

#### Kotlin DSL:

```kotlin
plugins {
    id("io.github.propactive") version "2.1.0"
}

dependencies {
  implementation("io.github.propactive:propactive-jvm:2.1.0")
}
```

#### Minimal example:

```kotlin
/** Placed at the root of the main source directory. (i.e. src/main/kotlin/ApplicationProperties.kt) */
@Environment
object ApplicationProperties {
  @Property(["HelloWorld"])
  const val property = "propactive.property.key"
}
```

Running the Propactive task `./gradlew generateApplicationProperties` will generate the following properties file:

```yaml
propactive.property.key=HelloWorld
```

The file will be named `application.properties` and it will be located within the `dist` directory of your set build
destination. If you want to learn [how to configure the location of your properties object](#gradle-plugin-configurations),
[how to set a custom application properties filename](#gradle-plugin-configurations), or [how to work with multiple environments when using Propactive](#working-with-multiple-environments),
then have a look at the rest this guide.

## Gradle Plugin configurations:

Proactive provides a plugin extension that allows you to specify the destination of the created application properties file,
set the location of the implementation class, and/or specify which environments you want to generate application properties
files for by default.

Here is an example that generates the files to a directory called `properties` within your build folder, locates the implementation
class of the application properties object at `io.github.propactive.demo.Properties`, and will only generate the `prod` environment
application properties within a file named `application.properties` when the task `generateApplicationProperties` is executed:

```kotlin
propactive {
    environments = "prod"
    implementationClass = "io.github.propactive.demo.Properties"
    destination = layout.buildDirectory.dir("properties").get().asFile.absolutePath
    filenameOverride = "application.properties"
}
```

The default values for the `propactive` extension are as follows:

```kotlin
propactive {
    environments = "*"
    implementationClass = "ApplicationProperties"
    destination = layout.buildDirectory.dir("resources/main").get().asFile.absolutePath
    autoGenerateApplicationProperties = true
    filenameOverride = null
    classCompileDependency = null
}
```

### Optional: Enable Class Compile Optimisation For Custom Compilation Tasks

Since Propactive is a runtime property generator that relies on loading a properties class at runtime, we need to ensure that
the properties class is compiled before the `generateApplicationProperties` task is executed. By default, the plugin will set
the `classCompileDependency` option to `compileJava` or/and `compileKotlin` if you are using Kotlin.

However, you can set the `classCompileDependency` option to something else if you want to optimise your build time, or if
you are compiling your classes from a different source set. For example, if you are [using Scala](https://xebia.com/blog/scala-and-kotlin-under-one-roof), 
you can set the `classCompileDependency` option to `compileScala`:

```kotlin
propactive {
    classCompileDependency = "compileScala"
}
```

### Optional: Disable Auto-Generated Properties File

By default, the plugin will generate the properties file when the `classes` task is executed. If you want to disable this
behaviour, you can set the `autoGenerateApplicationProperties` option to `false`:

```kotlin
propactive {
    autoGenerateApplicationProperties = false
}
```

## Plugin Tasks

Propactive provides 2 tasks that you can use to generate and validate your application properties files:

```
Propactive tasks
----------------
generateApplicationProperties - Generates application properties file for each given environment.

  Optional configurations:
    -Penvironments
        Description: Comma separated list of environments to generate the properties for.
        Example: "test,stage,prod"
        Default: "*" (All provided environments)
    -PimplementationClass
        Description: Sets the location of your properties object.
        Example: "com.package.path.to.your.ApplicationProperties"
        Default: "ApplicationProperties" (At the root of your project, without a package path.)
    -Pdestination
        Description: Sets the location of your generated properties file within the build directory.
        Example: layout.buildDirectory.dir("properties").get().asFile.absolutePath
        Default: layout.buildDirectory.dir("resources/main").get().asFile.absolutePath (In the main resources directory)
    -PfilenameOverride
        Description: Allows overriding given filename for when you're generating properties for a single environment.
        Example: "dev-application.properties"
        Note: This can only be used when generating application properties for a singular environment.

validateApplicationProperties - Validates the application properties without generating any files.

  Optional configurations:
    -PimplementationClass
        Description: Sets the location of your properties object.
        Example: "com.package.path.to.your.ApplicationProperties"
        Default: "ApplicationProperties" (At the root of your project, without a package path.)
```

## Runtime Property Validation

One of the key features Propactive has is the ability to validate given property values on runtime in a modular manner.
Let's consider the following scenario, You have an environment dependant URL values for a property called `app.web.server.url`:
- prod: `https://www.prodland.com`
- test: `http://www.nonprodland.com`
- dev:  `http://127.0.0.1/`

Therefore, you will end up creating 3 `application.properties` files:
```yaml
# prod-application.properties
app.web.server.url=https://www.prodland.com
```
```yaml
# test-application.properties
app.web.server.url=http://www.nonprodland.com
```
```yaml
# dev-application.properties
app.web.server.url=http://127.0.0.1/
```

Usually, this is fine, but as you scale, you have many environments, and dozens of application properties that have different
values for each environment. Therefore, this becomes a mundane process and error-prone. Not only you will need to define a constant for
`app.web.server.url` to test your property values, and perhaps another constant to reference it on your application side,
you will also need to parse each file if you want to test that the URL value is of valid format, if such precision is required.

With Propactive, this could simply be written like so:
```kotlin
@Environment(["prod/test/dev: *-application.properties"])
object Properties {
  @Property(
    value = [
      "prod: https://www.prodland.com",
      "test: http://www.nonprodland.com",
      "dev:  http://127.0.0.1/",
    ],
    type = URL::class
  )
  const val appWebServerUrlPropertyKey = "app.web.server.url"
}
```

Now locally, or [within your CI/CD](#demo-project-w-a-cicd-pipeline), you can generate the required application properties
file by running the following command: (omit `-Penvironments` option to generate the files for all environments)  

```shell
# TIP: This can be added as part of your deployment or build process as required
./gradlew generateApplicationProperties -Penvironments=prod
```

This will generate a file named `prod-application.properties` with the following entries:

```yaml
app.web.server.url=https://www.prodland.com
```

On top of that, it will validate the key value set by type (e.g. `URL`), if it's an invalid type, it will
fail with a verbose error. For example, the error message below is produced by having a malformed protocol keyword: (e.g. "htps" instead of "https")

```log
Property named: "propactive.demo.url.key" within environment named: "prod" was expected to be of type: "URL", but value was: "htps://www.prodland.com"
```

You can have a look below for the [list of natively supported property types](#natively-supported-property-types) or learn
[how to write your custom property types](#writing-your-custom-property-types) that you can use for runtime validation.

## Natively Supported Property Types

Propactive comes with a set of natively supported property types that you can use for validating
your property values on runtime. Below is a reference for each type and the specification followed:

- [BASE64](propactive-jvm/src/main/kotlin/io/github/propactive/type/BASE64.kt): type as defined by [RFC 4648](https://www.ietf.org/rfc/rfc4648.txt)
- [BOOLEAN](propactive-jvm/src/main/kotlin/io/github/propactive/type/BOOLEAN.kt): type as defined by your JVM.
- [DECIMAL](propactive-jvm/src/main/kotlin/io/github/propactive/type/DECIMAL.kt): type as defined by [IEEE 754](https://standards.ieee.org/ieee/754/6210/)
- [INTEGER](propactive-jvm/src/main/kotlin/io/github/propactive/type/INTEGER.kt): type is a 32-bit signed integer, as defined by your JVM.
- [JSON](propactive-jvm/src/main/kotlin/io/github/propactive/type/JSON.kt): type as defined by [RFC 8259](https://datatracker.ietf.org/doc/html/rfc8259)
- [STRING](propactive-jvm/src/main/kotlin/io/github/propactive/type/STRING.kt): type represents character strings, as defined by your JVM.
- [URI](propactive-jvm/src/main/kotlin/io/github/propactive/type/URI.kt): type as defined by [RFC 3986](https://www.ietf.org/rfc/rfc3986.txt)
- [URL](propactive-jvm/src/main/kotlin/io/github/propactive/type/URL.kt): type as defined by [RFC 2396](https://www.ietf.org/rfc/rfc2396.txt)
- [UUID](propactive-jvm/src/main/kotlin/io/github/propactive/type/UUID.kt): type as defined by [RFC 4122](https://www.ietf.org/rfc/rfc4122.txt)
- [CLASS](propactive-jvm/src/main/kotlin/io/github/propactive/type/CLASS.kt): type defined as a syntactically valid format as per [JLS 3.8](https://docs.oracle.com/javase/specs/jls/se21/html/jls-3.html#jls-3.8).
- [PORT](propactive-jvm/src/main/kotlin/io/github/propactive/type/PORT.kt): type as defined by [RFC 6335](https://www.ietf.org/rfc/rfc6335.txt)

If you believe we missed a common property type, feel free to let us know by opening an [issue](https://github.com/propactive/propactive/issues/new/choose) or make a PR, and we will be happy to merge.
Otherwise, please see the next section to learn [how to write your custom property types](#writing-your-custom-property-types).

## Writing Your Custom Property Types

Writing your custom property types is quite straightforward, you just need to implement the `propactive.type.Type` interface,
override the `validate` type, return `true` (or the constant `io.github.propactive.type.Type.VALID`) when validation pass or `false` (or the constant `io.github.propactive.type.Type.INVALID`)
when the validation fails, then you can use the type within your `@Property` annotation as usual.

Here is a `PORT_NUMBER` type that you can use to validate if a port number is within a valid range: (i.e. `0 till 65535`)

```kotlin
import io.github.propactive.type.Type

object PORT_NUMBER : Type {
  override fun validate(value: Any) = value
    .runCatching { toString().toInt() }
    .getOrDefault(-1)
    .let { number -> number in (1..65535) }
}
```

```kotlin
import io.github.propactive.environment.Environment
import io.github.propactive.property.Property

@Environment([
    "prod:       application.properties",
    "stage/test: *-application.properties",
    "dev:        localhost-application.properties",
])
object ApplicationProperties {
    @Property(
        value = [
            "prod:       433",
            "stage/test: 80",
            "dev:        8080",
        ],
        type = PORT_NUMBER::class
    )
    const val appWebServerPortPropertyKey = "app.web.server.port"
}
```

Running `./gradlew generateApplicationProperties` will generate the relevant application properties files, and the
typed port number validation will occur at runtime. You can [see this code running within our demo project](#demo-project-w-a-cicd-pipeline).

## Working With Multiple Environments

Working with multiple environments' means you will need a way to distinguish between different environment filenames and
different environment values. Proactive provides you the option to define multiple environments per `ApplicationProperties`
object and allows you to cascade multiple key entries against a single value.  

Below is an example with 4 environments where `stage` and `test` share the same values, but `prod` and `dev` have
separate entries. Note that the `@Environment` annotation supports a special wildcard expansion key (`*`) that is evaluated
to the environment name. (i.e. in the following example `stage` and `test` entries will generate 2 files named
`stage-application.properties` and `test-application.properties`)

```kotlin
@Environment([
  "prod:       application.properties",
  "stage/test: *-application.properties",
  "dev:        localhost-application.properties"
])
object ApplicationProperties {
  @Property(
    value = [
      "prod:       https://www.prodland.com",
      "stage/test: http://www.nonprodland.com",
      "dev:        http://127.0.0.1/",
    ],
    type = URL::class
  )
  const val appWebServerUrlPropertyKey = "app.web.server.url"
}
```

You can also map a property value with multiple environment keys. Above example shows that the `stage` and `test` entries
will share the same `"app.web.server.url"` value. (i.e. `http://www.nonprodland.com`)

## Blank values for properties (Relaxing mandatory values condition)

By default, properties key are expected to have a value assigned to it, and will error out if not. (i.e. an environment key cannot
have a blank value) This condition can be relaxed by setting the `mandatory` option to false:

```kotlin
@Environment
object ApplicationProperties {
  @Property(mandatory = false)
  const val property = "propactive.property.key"
}
```

This will generate a YAML file with `propactive.property.key=` and assign no value (blank) to it.

## Further Test Support With The Environment Factory

Sometimes you might want to do more granular testing on the application property keys and values. For that,
we provide the `EnvironmentFactory` object for creating an Environment model that you can use for extracting
any property name or value to uphold any assertions. Here is an example:

```kotlin
// The properties object
@Environment([
  "prod:       application.properties",
  "stage/test: *-application.properties",
  "dev:        localhost-application.properties",
])
object Properties {
  @Property(
    value = [
      "prod:       3000",
      "stage/test: 10000",
      "dev:        30000",
    ],
    type = INTEGER::class
  )
  const val timoutInMsPropertyKey = "propactive.demo.timout-in-ms.key"
}

// The test class that's making use of the EnvironmentFactory object:
class PropertiesTest {
    @Test
    fun shouldHaveTimeoutLargerThan250ms() {
        findAllMatchingPropertiesFor(timoutInMsPropertyKey)
            .forEach {
                assertTrue(
                    it.value.toInt() > 250,
                    "Expected: $timoutInMsPropertyKey for environment: ${it.environment} to have a value larger than 250ms but was: ${it.value}"
                )
            }
    }

  private fun findAllMatchingPropertiesFor(propertyKey: String): List<PropertyModel> = EnvironmentFactory
    .create(Properties::class)
    .mapNotNull { env -> env.properties.firstOrNull { it.name == propertyKey } }
}
```

You can [see this code running within our demo project](#demo-project-w-a-cicd-pipeline).

## Demo Project (w/ a CICD Pipeline)

To make the usecase of the Proactive framework clear, we provide an example project that makes use of above-mentioned features 
and is integrated with its own CI/CD pipeline. You will see how the application properties are validated and generated per environment. 
To top it up, a docker image is created/ran for each environment on deployment with a job summary outputted for each environment properties.

The project can be found here: [propactive/proactive-demo](https://github.com/propactive/propactive-demo)

___
