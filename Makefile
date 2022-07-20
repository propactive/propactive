# MAKE VARS ######################################################

# Meta information of app build:
APP_NAME=propactive-demo
VERSION?=DEV-SNAPSHOT

# Authentication details for SonaType repositories for propactive
# https://central.sonatype.org/publish/publish-gradle/#deploying-to-ossrh-with-gradle-introduction
OSSRH_USERNAME?=$(PROPACTIVE_OSSRH_USERNAME)
OSSRH_PASSWORD?=$(PROPACTIVE_OSSRH_PASSWORD)

# Signing information for propactive JARs
# https://docs.gradle.org/current/userguide/signing_plugin.html#sec:using_gpg_agent
SIGNING_GNUPG_EXECUTABLE?=$(PROPACTIVE_SIGNING_GNUPG_EXECUTABLE)
SIGNING_GNUPG_HOME_DIR?=$(PROPACTIVE_SIGNING_GNUPG_HOME_DIR)
SIGNING_GNUPG_KEY_NAME?=$(PROPACTIVE_SIGNING_GNUPG_KEY_NAME)
SIGNING_GNUPG_PASSPHRASE?=$(PROPACTIVE_SIGNING_GNUPG_PASSPHRASE)

# RECIPES ######################################################

assemble-jars:
	@echo "******** Assembling JARs... ********"
	export VERSION=$(VERSION) && \
	./gradlew build -x test $(GPG_SIGNING_PROPERTIES) --info

sign-jars:
	@echo "******** Assembling JARs... ********"
	export VERSION=$(VERSION) && \
	./gradlew build -x test --info

publish-jars:
	@echo "******** Publishing JARs... ********"
	export VERSION=$(VERSION) && \
	$(OSSRH_ENVIRONMENT_VARIABLES) && \
	./gradlew propactive-jvm:publish $(GPG_SIGNING_PROPERTIES) --info

# GPG SIGNING PROPERTIES ####################################################

define GPG_SIGNING_PROPERTIES
   -Psigning.gnupg.executable=$(SIGNING_GNUPG_EXECUTABLE) \
   -Psigning.gnupg.homeDir=$(SIGNING_GNUPG_HOME_DIR) \
   -Psigning.gnupg.keyName=$(SIGNING_GNUPG_KEY_NAME) \
   -Psigning.gnupg.passphrase=$(SIGNING_GNUPG_PASSPHRASE)
endef

# OSSRH ENVIRONMENT VARIABLES ####################################################

define OSSRH_ENVIRONMENT_VARIABLES
   export OSSRH_USERNAME=$(OSSRH_USERNAME) && \
   export OSSRH_PASSWORD=$(OSSRH_PASSWORD)
endef