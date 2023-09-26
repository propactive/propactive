# MAKE VARS ######################################################

# Application meta-data
APP_NAME=propactive
# NOTE: You can always override, example: `make [recipe] VERSION="x.x.x"`
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

# Although the Gradle image has a user named "gradle",
# We might need to use the "root" user here because GH actions
# uid/gid is not 1000. Meaning that the "gradle" user
# won't have access to the mounted volumes.
# See: https://hub.docker.com/_/gradle
DOCKER_USER=gradle
DOCKER_USER_HOME=/home/$(DOCKER_USER)
DOCKER_PROJECT_DIR=$(DOCKER_USER_HOME)/$(APP_NAME)

# Host volumes to mount:
HOST_PROJECT_DIR:=$(shell dirname $(realpath $(firstword $(MAKEFILE_LIST))))
HOST_GRADLE_DATA?=$(HOME)/.gradle
HOST_MAVEN_DATA?=$(HOME)/.m2

# IMAGE DETAILS #################################################

# Gradle's official Ubuntu JDK17 image.
#   See:
#     https://github.com/keeganwitt/docker-gradle/blob/master/jdk17/Dockerfile
#   NOTE:
#     Use a Ubuntu image instead of Linux Alpine as official Alpine uses musl libc
#     instead of the more common glibc used by most other Linux distributions. (Many pre-built
#     binaries, including many JDK distributions, expect glibc, so running Alpine risks running
#     into unexpected errors)
DOCKER_GRADLE_IMAGE=gradle:8.1.1-jdk17-jammy
# Toolchain runner container name
TOOLCHAIN_RUNNER_CONTAINER_NAME=$(APP_NAME)-toolchain-runner

# INJECTED VOLUMES #################################################

# HOST_PROJECT_DIR:rw  - persist build output
# HOST_GRADLE_DATA:rw  - cache
# HOST_MAVEN_DATA:rw   - cache
define TOOLCHAIN_CONTAINER_VOLUMES
	-v $(HOST_PROJECT_DIR):$(DOCKER_PROJECT_DIR) \
	-v $(HOST_GRADLE_DATA):$(DOCKER_USER_HOME)/.gradle:rw \
	-v $(HOST_MAVEN_DATA):$(DOCKER_USER_HOME)/.m2:rw
endef

# INJECTED ENVIRONMENT #####################################################

define TOOLCHAIN_CONTAINER_ENVIRONMENT_VARIABLES
	$(VERSION_ENVIRONMENT_VARIABLE) \
    $(OSSRH_ENVIRONMENT_VARIABLES) \
    $(GRADLE_PUBLISH_ENVIRONMENT_VARIABLES)
endef

# ID MATCHER #################################################

# Match user's UID and GID (i.e. correct access to cached files)
HOST_UID=`id -u`
HOST_GID=`id -g`

# The GitHub Actions runner uses a non-standard UID/GID
# which causes permission issues when mounting volumes.
# Below definition will change the UID/GID of the "gradle"
# user in the image to match the host's UID/GID.
#
# We then run the provided command as the "gradle" user.
define MATCH_HOST_UID_GID
	usermod -u "$(HOST_UID)" gradle && \
	groupmod -g "$(HOST_GID)" gradle
endef

# USER MATCHER #################################################

# Our docker image runs as root (so we can modify gradle's UID and GID to match
# our CI runner IDs) this means once we execute our commands on user switch,
# we need to ensure the environment variables Gradle relies on are updated
# accordingly.
#
# See: https://docs.gradle.org/current/userguide/build_environment.html
define GRADLE_USER_MATCHER
    -e GRADLE_USER_HOME=$(DOCKER_USER_HOME)/.gradle \
    -e HOME=$(DOCKER_USER_HOME)
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
	docker pull $(DOCKER_GRADLE_IMAGE) && \
	docker run --rm -u root --name $(TOOLCHAIN_RUNNER_CONTAINER_NAME) \
	$(TOOLCHAIN_CONTAINER_VOLUMES) \
	$(TOOLCHAIN_CONTAINER_ENVIRONMENT_VARIABLES) \
	$(GRADLE_USER_MATCHER) \
	-w $(DOCKER_PROJECT_DIR) $(DOCKER_GRADLE_IMAGE) sh -c "$(MATCH_HOST_UID_GID) && su gradle && $(1)"
endef
