# MAKE VARS ######################################################

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

# Application meta-data
# NOTE: You can always override, example: `make recipe VERSION="x.x.x"`
VERSION?=`./scripts/version_deriver.sh`

# RECIPES #############################################################

test-propactive-jvm:
	@echo "******** Running tests: propactive-jvm ... ********"
	./gradlew propactive-jvm:test --info

test-propactive-plugin:
	@echo "******** Running tests: propactive-plugin ... ********"
	./gradlew propactive-plugin:test --info

check-linter:
	@echo "******** Running linter: propactive-* ... ********"
	./gradlew ktCh --continue

build-jars:
	@echo "******** Building JARs ... ********"
	$(VERSION_ENVIRONMENT_VARIABLE) && \
	./gradlew build -x test $(GPG_SIGNING_PROPERTIES) --info

validate-version-number:
	@echo "******** Validating version: '$(VERSION)' ... ********"
	./scripts/release_version_validator.sh $(VERSION)

publish-latest-version-tag: validate-version-number
	@echo "******** Publishing latest version tag '$(VERSION)' ... ********"
	git tag $(VERSION) $(git log --pretty=format:"%H" -n 1) && \
	git push origin --tags

publish-propactive-jvm-jars: validate-version-number
	@echo "******** Publishing JARs: propactive-jvm ... ********"
	$(VERSION_ENVIRONMENT_VARIABLE) && \
	$(OSSRH_ENVIRONMENT_VARIABLES) && \
	./gradlew propactive-jvm:publish $(GPG_SIGNING_PROPERTIES) --info

publish-propactive-plugin-jars: validate-version-number
	@echo "******** Publishing JARs: propactive-plugin ... ********"
	$(VERSION_ENVIRONMENT_VARIABLE) && \
	$(GRADLE_PUBLISH_ENVIRONMENT_VARIABLES) && \
	./gradlew propactive-plugin:publishPlugins $(GPG_SIGNING_PROPERTIES) --info

# ENVIRONMENT VARIABLES ###############################################

define VERSION_ENVIRONMENT_VARIABLE
   export VERSION=$(VERSION)
endef

define OSSRH_ENVIRONMENT_VARIABLES
   export OSSRH_USERNAME=$(OSSRH_USERNAME) && \
   export OSSRH_PASSWORD=$(OSSRH_PASSWORD)
endef

define GRADLE_PUBLISH_ENVIRONMENT_VARIABLES
	export GRADLE_PUBLISH_KEY=$(GRADLE_PUBLISH_KEY) && \
	export GRADLE_PUBLISH_SECRET=$(GRADLE_PUBLISH_SECRET)
endef

# PROPERTIES ##########################################################

define GPG_SIGNING_PROPERTIES
   -Psigning.gnupg.executable=$(SIGNING_GNUPG_EXECUTABLE) \
   -Psigning.gnupg.homeDir=$(SIGNING_GNUPG_HOME_DIR) \
   -Psigning.gnupg.keyName=$(SIGNING_GNUPG_KEY_NAME) \
   -Psigning.gnupg.passphrase=$(SIGNING_GNUPG_PASSPHRASE)
endef
