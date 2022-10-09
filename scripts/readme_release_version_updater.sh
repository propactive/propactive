#!/bin/bash
# shellcheck disable=SC2086
#================================================================
# HEADER
#================================================================
#% SYNOPSIS
#+    readme_versions_updater arg1 arg2
#%
#% DESCRIPTION
#%    This script will update the README.md file to the given
#%    latest version available for both the plugin and implementation
#     dependencies.
#%
#% EXAMPLES
#%    readme_versions_updater "1.0.1" ./README.md
#================================================================
# END_OF_HEADER
#================================================================
function readme_versions_updater {
    local GIVEN_LATEST_VERSION=$1
    local GIVEN_README_FILE_LOCATION=$2

    pluginPattern() { echo "id(\"io.github.propactive\") version \"$1\""; }
    dependencyPattern() { echo "implementation(\"io.github.propactive:propactive-jvm:$1\")"; }

    sed -i "s/$(pluginPattern ".*")/$(pluginPattern $GIVEN_LATEST_VERSION)/g" $GIVEN_README_FILE_LOCATION
    sed -i "s/$(dependencyPattern ".*")/$(dependencyPattern $GIVEN_LATEST_VERSION)/g" $GIVEN_README_FILE_LOCATION
  }

readme_versions_updater "$@"
