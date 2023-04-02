# MAKE VARS ######################################################

# Application meta-data
APP_NAME=propactive
# NOTE: You can always override, example: `make recipe VERSION="x.x.x"`
VERSION?=`./scripts/version_deriver.sh`

# Signing information for propactive JARs
# https://docs.gradle.org/current/userguide/signing_plugin.html#sec:using_gpg_agent
SIGNING_GNUPG_EXECUTABLE?=$(PROPACTIVE_SIGNING_GNUPG_EXECUTABLE)
SIGNING_GNUPG_HOME_DIR?=$(PROPACTIVE_SIGNING_GNUPG_HOME_DIR)
SIGNING_GNUPG_KEY_NAME?=$(PROPACTIVE_SIGNING_GNUPG_KEY_NAME)
SIGNING_GNUPG_PASSPHRASE?=$(PROPACTIVE_SIGNING_GNUPG_PASSPHRASE)

# Authentication details for SonaType repositories
# https://central.sonatype.org/publish/publish-gradle/#deploying-to-ossrh-with-gradle-introduction
OSSRH_USERNAME?=$(PROPACTIVE_OSSRH_USERNAME)
OSSRH_PASSWORD?=$(PROPACTIVE_OSSRH_PASSWORD)

# Authentication details for Gradle Plugin Portal
# https://plugins.gradle.org/docs/publish-plugin#portal-setup
GRADLE_PUBLISH_KEY?=$(PROPACTIVE_GRADLE_PUBLISH_KEY)
GRADLE_PUBLISH_SECRET?=$(PROPACTIVE_GRADLE_PUBLISH_SECRET)

# RECIPES #############################################################

test-propactive-jvm:
	@echo "******** Running tests: propactive-jvm ... ********"
	$(call toolchain_runner, ./gradlew propactive-jvm:test --info)

test-acceptance-propactive-plugin:
	@echo "******** Running tests: propactive-plugin ... ********"
	$(call toolchain_runner, ./gradlew propactive-plugin:test --tests '*Test' --info)

test-integration-propactive-plugin:
	@echo "******** Running tests: propactive-plugin ... ********"
	$(call toolchain_runner, ./gradlew propactive-plugin:test --tests '*IT' --info)

check-linter:
	@echo "******** Running linter: propactive-* ... ********"
	$(call toolchain_runner, ./gradlew ktCh --continue)

build-jars:
	@echo "******** Building JARs ... ********"
	$(call toolchain_runner, ./gradlew build -x test $(GPG_SIGNING_PROPERTIES) --info)

validate-version-number:
	@echo "******** Validating version: '$(VERSION)' ... ********"
	./scripts/release_version_validator.sh $(VERSION)

update-readme-documented-versions:
	@echo "******** Updating documented versions in ./README.md: '$(VERSION)' ... ********"
	./scripts/readme_release_version_updater.sh $(VERSION) ./README.md

publish-latest-version-tag: validate-version-number
	@echo "******** Publishing latest version tag '$(VERSION)' ... ********"
	git tag $(VERSION) $(git log --pretty=format:"%H" -n 1) && \
	git push origin --tags

publish-propactive-jvm-jars: validate-version-number
	@echo "******** Publishing JARs: propactive-jvm ... ********"
	$(call toolchain_runner, ./gradlew propactive-jvm:publishToSonatype closeAndReleaseSonatypeStagingRepository $(GPG_SIGNING_PROPERTIES) --info)

publish-propactive-plugin-jars: validate-version-number
	@echo "******** Publishing JARs: propactive-plugin ... ********"
	$(call toolchain_runner, ./gradlew propactive-plugin:publishPlugins $(GPG_SIGNING_PROPERTIES) --info)

# APPLICATION-SPECIFIC ENVIRONMENT VARIABLES ###############################################

define VERSION_ENVIRONMENT_VARIABLE
   -e VERSION=$(VERSION)
endef

define OSSRH_ENVIRONMENT_VARIABLES
   -e OSSRH_USERNAME=$(OSSRH_USERNAME) \
   -e OSSRH_PASSWORD=$(OSSRH_PASSWORD)
endef

define GRADLE_PUBLISH_ENVIRONMENT_VARIABLES
	-e GRADLE_PUBLISH_KEY=$(GRADLE_PUBLISH_KEY) \
	-e GRADLE_PUBLISH_SECRET=$(GRADLE_PUBLISH_SECRET)
endef

# PROPERTIES ##########################################################

define GPG_SIGNING_PROPERTIES
   -Psigning.gnupg.executable=$(SIGNING_GNUPG_EXECUTABLE) \
   -Psigning.gnupg.homeDir=$(SIGNING_GNUPG_HOME_DIR) \
   -Psigning.gnupg.keyName=$(SIGNING_GNUPG_KEY_NAME) \
   -Psigning.gnupg.passphrase=$(SIGNING_GNUPG_PASSPHRASE)
endef

# TOOLCHAIN #########################################################

# SETUP #####################################################

# User is given by the Gradle image we are running.
# It runs using uid/gid 1000 to avoid running as root.
# See: https://hub.docker.com/_/gradle
USER=gradle
# User home directory inside the container
USER_HOME=/home/$(USER)
# Working directory inside the container
PROJECT_DIR=$(USER_HOME)/$(APP_NAME)
# Volumes to mount:
HOST_PROJECT_DIR:=$(shell dirname $(realpath $(firstword $(MAKEFILE_LIST))))
HOST_GRADLE_DATA?=$(HOME)/.gradle
HOST_MAVEN_DATA?=$(HOME)/.m2
# Image details
ALPINE_GRADLE_IMAGE=gradle:8.0.2-jdk17-alpine
# Toolchain runner container name
TOOLCHAIN_RUNNER_CONTAINER_NAME=$(APP_NAME)-toolchain-runner

# INJECTED VOLUMES #################################################

# HOST_PROJECT_DIR:rw  - persist build output
# HOST_GRADLE_DATA:rw  - cache
# HOST_MAVEN_DATA:rw   - cache
define TOOLCHAIN_CONTAINER_VOLUMES
	-v $(HOST_PROJECT_DIR):$(PROJECT_DIR) \
	-v $(HOST_GRADLE_DATA):$(USER_HOME)/.gradle:rw \
	-v $(HOST_MAVEN_DATA):$(USER_HOME)/.m2:rw
endef

# INJECTED ENVIRONMENT #####################################################

define TOOLCHAIN_CONTAINER_ENVIRONMENT_VARIABLES
	$(VERSION_ENVIRONMENT_VARIABLE) \
    $(OSSRH_ENVIRONMENT_VARIABLES) \
    $(GRADLE_PUBLISH_ENVIRONMENT_VARIABLES)
endef

# RUNNER #########################################################

# TOOLCHAIN Steps:
#  1. Remove any existing/lingering toolchain runner containers
#  2. Pull the toolchain image
#  3. Run the container with set variables, volumes, toolchain image,
#     and command given. (Note that the toolchain is mounted to project
#     directory so any output generated will be written there...)
#
#  Usage example:
#    $(call toolchain_runner, ./gradlew build -x test --info)
#    $(call toolchain_runner, ./gradlew tasks)
#
#  See: https://www.gnu.org/software/make/manual/html_node/Call-Function.html
define toolchain_runner
	(docker rm -f $(TOOLCHAIN_RUNNER_CONTAINER_NAME) || true) && \
	docker pull $(ALPINE_GRADLE_IMAGE) && \
	docker run --rm -u gradle --name $(TOOLCHAIN_RUNNER_CONTAINER_NAME) \
	$(TOOLCHAIN_CONTAINER_VOLUMES) \
	$(TOOLCHAIN_CONTAINER_ENVIRONMENT_VARIABLES) \
	-w $(PROJECT_DIR) $(ALPINE_GRADLE_IMAGE) \
	$(1)
endef
