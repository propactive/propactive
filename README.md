# Propactive

[![CI](https://github.com/u-ways/propactive/actions/workflows/ci.yml/badge.svg)](https://github.com/u-ways/propactive/actions/workflows/ci.yml)
[![Code Climate](https://codeclimate.com/github/u-ways/propactive.png)](https://codeclimate.com/github/u-ways/propactive)
[![Latest Version](https://img.shields.io/github/v/release/U-ways/propactive)](https://github.com/u-ways/propactive/releases)
[![GitHub License](https://badgen.net/badge/license/MIT/blue)](https://github.com/u-ways/propactive/blob/master/LICENSE)

An application property generator framework that validates your property values on runtime.

- [Setup](#setup)
- [Gradle Plugin configurations](#gradle-plugin-configurations)
- [Why Propactive was developed? (Historical background)](#why-propactive-was-developed-historical-background)
- [Runtime Property Validation](#runtime-property-validation)
- [Natively Supported Property Types](#natively-supported-property-types)
- [Writing Your Own Custom Property Types](#writing-your-own-custom-property-types)
- [Working With Multiple Environments](#working-with-multiple-environments)
- [Blank Values For Properties (Relaxing Mandatory Values Condition)](#blank-values-for-properties-relaxing-mandatory-values-condition)
- [Further Test Support With The Environment Factory](#further-test-support-with-the-environment-factory)
- [Integrating With Your CICD](#integrating-with-your-CICD)
- [Demo Project](#demo-project)

## Setup

Using the `plugins` & `dependencies` blocks, you can set up Propactive as follows:

#### Kotlin DSL:

```kotlin
plugins {
    id("io.github.propactive")
}

dependencies {
    implementation("io.github.propactive:propactive-jvm")
}
```

#### Minimal example:

```kotlin
/** Placed at root of the main source directory. (i.e. src/main/kotlin/ApplicationProperties.kt) */
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

The file will be named `application.properties` and it will be located within the `dist` 
directory of your set build destination. If you want to learn [how to configure the location of your properties object](#gradle-plugin-configurations),
[how to set a custom application properties filename](#gradle-plugin-configurations), or [how to work with multiple environments when using Propactive](#working-with-multiple-environments), 
then have a look at the rest this guide.

## Gradle Plugin configurations:

Proactive provides a plugin extension that allows you to specify the destination of the created application properties file,
set the location of the implementation class, and/or specify which environments you want to generate application properties
files for by default. 

Here is an example that generates the files to a directory called `dist` within your build folder, locates the implementation
class of the application properties object at `propactive.demo.Properties`, and will only generate `prod` environment 
application properties when no options are passed to the `generateApplicationProperties` task:

```kotlin
propactive {
    destination = layout.buildDirectory.dir("dist").get().asFile.absolutePath
    implementationClass = "propactive.demo.Properties"
    environments = "prod"
}
```

## Why Propactive was developed? (Historical background)

[//]: # (TODO: convert to diagram when complete using "Google Draw"...)

In one of the places I worked in, a sum of the projects had a streamlined process to define, test, and maintain application properties.
The application source code and deployment details (i.e. helm charts files, application properties, JKS files...etc.)
were seperated in two different project repositories. This led to the following inconvenience:   

- Duplication: both application project and deployment project are testing against expected properties such as checking for compulsory 
  keys and typed values (i.e. if a values in an integer as excepted), since we do have several microservices within our stack,
  these types of tests were duplicated across several projects. 
- Maintainability: Since we have multiple environments to deploy our application, we usually end up with many application 
  properties files. We also have a separate module to deploy the application locally (i.e. "dev" environment) and another
  project that we use for E2E integration testing across our stack, so more application properties files were being maintained there.
  This means whenever we add a new application property, the developer needs to be domain-aware of all application property files 
  and update each, accordingly.       
- Incoherence: Some application property values are tested to validate if they confirm to a certain type (i.e. URL,
  BASE64, Decimal...etc.). Usually, we have a shared library that extracts common functions and is reused across several 
  projects. However, as the addition/removal of application properties is a fast-paced process, we ended with several 
  methods to test the same thing. (e.g. using Regex to validate a URL whereas another would use Java's built-in `net.URL` API)

For such a critical part of our deployment pipeline, this left me wondering if there is a way we can improve the development 
and maintainability of our application properties. After all, a single incorrect application property key or value can 
lead to unexpected downtime, and it is an issue we experienced in the past.

**Therefore, Propactive was developed with the following requirements in mind:**
- Eliminate the need to store multiple application properties files in deployment projects.
- The ability to test/validate application properties files with a higher degree of accuracy and flexibility.
- Reduce code duplication/maintenance by having a single source of truth for application property keys and concise way to define values.
- Provide a set of commonly used property types for validation on runtime to reduce code boilerplate.

## Runtime Property Validation

One of the key features Propactive has is the ability to validate given property values on runtime in a modular manner. 
Let's consider the following scenario, You have an environment dependant URL value for a property called `app.web.server.url`:
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

Usually, this is fine, but as you scale, have many environments, and dozens of application properties that has different
values for each environment, it becomes more mundane and error-prone, not only you will need to define a constant for 
`app.web.server.url` to test your property values, and perhaps another constant to reference it on your application side, 
you will also need to parse each file if you want to test if the URL value is of valid format, if such precision is required.

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

Now locally, or [within your CI/CD](#integrating-with-your-CICD), you can generate the required application properties 
file by running the following command: (omit `-Penvironments` option to generate the files for all environments)  

```shell
# This can be added as part of your deployment or build process as required
./gradlew generateApplicationProperties -Penvironments=prod 
```

This will generate a file named `prod-application.properties` with the following entries:

```yaml
app.web.server.url=https://www.prodland.com
```

On top of that, it will validate the key value as set by type (e.g. `URL`) such that if it's an invalid type, it will 
fail with a verbose error. For example, the error message below is produced by having a malformed protocol keyword: (i.e. "htps") 

```log
Property named: propactive.demo.url.key within environment named: prod was expected to be of type: URL, but value was: htps://www.prodland.com
```

You can have a look below for the [list of natively supported property types](#natively-supported-property-types) or learn 
[how to write your own custom property types](#writing-your-own-custom-property-types) that you can use for runtime validation. 

## Natively Supported Property Types

Propactive comes with a set of natively supported property types that you can use for validating
your property values on runtime. Below is a reference for each type and the specification followed:

- [BASE64](propactive-jvm/src/main/kotlin/propactive/type/BASE64.kt): BASE64 type as defined by [RFC 4648](https://www.ietf.org/rfc/rfc4648.txt)
- [BOOLEAN](propactive-jvm/src/main/kotlin/propactive/type/BOOLEAN.kt): BOOLEAN type as defined by your JVM.
- [DECIMAL](propactive-jvm/src/main/kotlin/propactive/type/DECIMAL.kt): DECIMAL type as defined by [IEEE 754](https://standards.ieee.org/ieee/754/6210/)
- [INTEGER](propactive-jvm/src/main/kotlin/propactive/type/INTEGER.kt): INTEGER type is a 32-bit signed integer, as defined by your JVM.
- [JSON](propactive-jvm/src/main/kotlin/propactive/type/JSON.kt): JSON type as defined by [RFC 8259](https://datatracker.ietf.org/doc/html/rfc8259)
- [STRING](propactive-jvm/src/main/kotlin/propactive/type/STRING.kt): STRING type represents character strings, as defined by your JVM.
- [URI](propactive-jvm/src/main/kotlin/propactive/type/URI.kt): URI type as defined by [RFC 3986](https://www.ietf.org/rfc/rfc3986.txt)
- [URL](propactive-jvm/src/main/kotlin/propactive/type/URL.kt): URL type as defined by [RFC 2396](https://www.ietf.org/rfc/rfc2396.txt)
- [UUID](propactive-jvm/src/main/kotlin/propactive/type/UUID.kt): UUID type as defined by [RFC 4122](https://www.ietf.org/rfc/rfc4122.txt)

If you believe I missed a common property type, feel free to let me know, or PR and I will be happy to merge. 
Otherwise, please see the next section to learn [how to write your own custom property types](#writing-your-own-custom-property-types).

## Writing Your Own Custom Property Types

Writing your custom property types is quite straightforward, you just need to implement the `propactive.type.Type` interface, 
override the `validate` type, return `true` (or the constant `propactive.type.Type.VALID`) when validation pass or `false` (or the constant `propactive.type.Type.INVALID`) 
when the validation fails, and then you can use the type within your `@Property` annotation as usual. 

Here is a `PORT_NUMBER` type that you can use to validate if a port number is within a valid range: (i.e. `0 till 65535`)

```kotlin
import propactive.type.Type

object PORT_NUMBER : Type {
  override fun validate(value: Any) = value
    .runCatching { toString().toInt() }
    .getOrDefault(-1)
    .let { number -> number in (1..65535) }
}
```

```kotlin
import propactive.environment.Environment
import propactive.property.Property

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
typed port number validation will occur at runtime. You can [see this code running within our demo project](#demo-project).

## Working With Multiple Environments

Working with multiple environments' means you will need a way to distinguish between different environment filenames and
different environment values. Proactive provide you the option to define multiple environments per `ApplicationProperties`
object and allows you to cascade multiple keys entries against a single value.  

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
  @Property(
    value = [""],
    mandatory = false
  )
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

You can [see this code running within our demo project](#demo-project).

## Integrating With Your CICD

Documentation for this will be published as soon as the Demo project is complete...

## Demo Project

Work in progress...

___