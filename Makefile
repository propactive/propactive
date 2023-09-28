################################################################################################
# Propactive Makefile
#
# This Makefile is split into two sections:
#   - Application: for building, testing, and publishing the project.
#   - Toolchain: for building, testing, and publishing the toolchain that is used to run the project.
#
# The application section is intended to be used by developers and contributors to the project.
# The toolchain section is intended to be used by maintainers of the project.
#
# We write our rule names in the following format: [verb]-[noun]-[noun], e.g. "build-jars".
#
# Application ##################################################################################

# RECIPES

test-propactive-jvm:
	@echo "******** Running tests: propactive-jvm ... ********"
	$(call toolchain_runner,./gradlew propactive-jvm:test --info)

test-acceptance-propactive-plugin:
	@echo "******** Running tests: propactive-plugin ... ********"
	$(call toolchain_runner,./gradlew propactive-plugin:test --tests *Test --info)

test-integration-propactive-plugin:
	@echo "******** Running tests: propactive-plugin ... ********"
	$(call toolchain_runner,./gradlew propactive-plugin:test --tests *IT --info)

check-linter:
	@echo "******** Running linter: propactive-* ... ********"
	$(call toolchain_runner,./gradlew ktCh --continue)

build-jars:
	@echo "******** Building JARs ... ********"
	$(call toolchain_runner,./gradlew build -x test $(GPG_SIGNING_PROPERTIES) --info)

build-jars-no-signing:
	@echo "******** Building JARs (without signing)... ********"
	$(call toolchain_runner,./gradlew build -x test --info)

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
	$(call toolchain_runner,./gradlew propactive-jvm:publishToSonatype closeAndReleaseSonatypeStagingRepository $(GPG_SIGNING_PROPERTIES) --info)

publish-propactive-plugin-jars: validate-version-number
	@echo "******** Publishing JARs: propactive-plugin ... ********"
	$(call toolchain_runner,./gradlew propactive-plugin:publishPlugins $(GPG_SIGNING_PROPERTIES) --info)

# VARIABLES

APP_NAME=propactive
VERSION?=`./scripts/version_deriver.sh`

# Authentication details for SonaType repositories
# https://central.sonatype.org/publish/publish-gradle/#deploying-to-ossrh-with-gradle-introduction
OSSRH_USERNAME?=$(PROPACTIVE_OSSRH_USERNAME)
OSSRH_PASSWORD?=$(PROPACTIVE_OSSRH_PASSWORD)

# Authentication details for Gradle Plugin Portal
# https://plugins.gradle.org/docs/publish-plugin#portal-setup
GRADLE_PUBLISH_KEY?=$(PROPACTIVE_GRADLE_PUBLISH_KEY)
GRADLE_PUBLISH_SECRET?=$(PROPACTIVE_GRADLE_PUBLISH_SECRET)

# Signing information for propactive JARs
# https://docs.gradle.org/current/userguide/signing_plugin.html#sec:using_gpg_agent
SIGNING_GNUPG_EXECUTABLE?=$(PROPACTIVE_SIGNING_GNUPG_EXECUTABLE)
SIGNING_GNUPG_HOME_DIR?=$(PROPACTIVE_SIGNING_GNUPG_HOME_DIR)
SIGNING_GNUPG_KEY_NAME?=$(PROPACTIVE_SIGNING_GNUPG_KEY_NAME)
SIGNING_GNUPG_PASSPHRASE?=$(PROPACTIVE_SIGNING_GNUPG_PASSPHRASE)

define GPG_SIGNING_PROPERTIES
 -Psigning.gnupg.executable=$(SIGNING_GNUPG_EXECUTABLE) \
 -Psigning.gnupg.homeDir=$(SIGNING_GNUPG_HOME_DIR) \
 -Psigning.gnupg.keyName=$(SIGNING_GNUPG_KEY_NAME) \
 -Psigning.gnupg.passphrase=$(SIGNING_GNUPG_PASSPHRASE)
endef

# TOOLCHAIN ##################################################################################

# RECIPES

login-to-docker-hub:
	@echo "******** Logging in to Docker Hub ... ********"
	@echo "$(DOCKER_HUB_PASSWORD)" | docker login -u "$(DOCKER_HUB_USERNAME)" --password-stdin

build-toolchain:
	@echo "******** Building toolchain image ... ********"
	docker build -t $(TOOLCHAIN_IMAGE_ID) -f ./Dockerfile .

publish-toolchain: build-toolchain login-to-docker-hub
	@echo "******** Publishing toolchain image ... ********"
	docker push $(TOOLCHAIN_IMAGE_ID)

login-toolchain:
	@echo "******** Running toolchain image as an interactive shell ... ********"
	@$(call toolchain_runner,bash -li,-it -e TERM='xterm-256color' -h '[Propactive|Runner]')

# Example: make toolchain-exec cmd="ls -la"
exec-toolchain:
	@$(call toolchain_runner,$(cmd))

# VARIABLES

# TIP: Do you wanna avoid pulling the toolchain image every time you run a command?
#      Set the TOOLCHAIN_IMAGE_VERSION to a specific version, e.g. "snapshot", and run "make build-toolchain-image".
#      Then, you will have a local image that you can use for running commands that isn't constantly being updated
#      As it's not latest.
TOOLCHAIN_IMAGE_VERSION?=latest
TOOLCHAIN_IMAGE_ID=propactive/runner:$(TOOLCHAIN_IMAGE_VERSION)
TOOLCHAIN_CONTAINER_NAME=$(APP_NAME)-runner

DOCKER_HUB_USERNAME?=$(PROPACTIVE_DOCKER_HUB_USERNAME)
DOCKER_HUB_PASSWORD?=$(PROPACTIVE_DOCKER_HUB_PASSWORD)

DOCKER_HOME_DIR=/home/gradle
DOCKER_PROJECT_DIR=$(DOCKER_HOME_DIR)/propactive
DOCKER_GRADLE_DATA=$(DOCKER_HOME_DIR)/.gradle
DOCKER_MAVEN_DATA=$(DOCKER_HOME_DIR)/.m2

HOST_PROJECT_DIR:=$(shell dirname $(realpath $(firstword $(MAKEFILE_LIST))))
HOST_GRADLE_DATA?=$(HOME)/.gradle
HOST_MAVEN_DATA?=$(HOME)/.m2

# INJECTED VOLUMES

define TOOLCHAIN_CONTAINER_VOLUMES
 -v $(HOST_PROJECT_DIR):$(DOCKER_PROJECT_DIR):rw \
 -v $(HOST_GRADLE_DATA):$(DOCKER_GRADLE_DATA):rw \
 -v $(HOST_MAVEN_DATA):$(DOCKER_MAVEN_DATA):rw
endef

# INJECTED ENVIRONMENT VARIABLES

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

# See: scripts/entrypoint.sh
define DOCKER_USER_UID_GID_MATCHER
 -e SET_DOCKER_USER_UID=$(shell id -u) \
 -e SET_DOCKER_USER_GID=$(shell id -g)
endef

define TOOLCHAIN_CONTAINER_ENVIRONMENT_VARIABLES
 $(VERSION_ENVIRONMENT_VARIABLE) \
 $(DOCKER_USER_UID_GID_MATCHER) \
 $(OSSRH_ENVIRONMENT_VARIABLES) \
 $(GRADLE_PUBLISH_ENVIRONMENT_VARIABLES)
endef

# FUNCTIONS

# 1. Ensure there are no lingering toolchain runner containers
# 2.1. If the version is "latest", always pull the toolchain image.
# 2.2. If the version is not "latest", check if the toolchain image exists, and pull if not.
define ensure_clean_toolchain_runner_environment
 docker rm -f $(TOOLCHAIN_CONTAINER_NAME) > /dev/null 2>&1 || true; \
 if [ "$(TOOLCHAIN_IMAGE_VERSION)" = "latest" ]; then \
    docker pull $(TOOLCHAIN_IMAGE_ID); \
 else \
    docker image inspect $(TOOLCHAIN_IMAGE_ID) > /dev/null 2>&1 || docker pull $(TOOLCHAIN_IMAGE_ID); \
 fi
endef

define toolchain_runner
 $(call ensure_clean_toolchain_runner_environment) && \
 docker run $(2) --rm --name $(TOOLCHAIN_CONTAINER_NAME) \
 $(TOOLCHAIN_CONTAINER_VOLUMES) \
 $(TOOLCHAIN_CONTAINER_ENVIRONMENT_VARIABLES) \
 $(TOOLCHAIN_IMAGE_ID) "$(1)"
endef
