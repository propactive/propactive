#!/bin/bash
# shellcheck disable=SC2155
#================================================================
# HEADER
#================================================================
#% SYNOPSIS
#+    version_deriver
#%
#% DESCRIPTION
#%    This script will derive current release version for a given
#%    Git project that follows [Semantic Versioning 2.0.0][1].
#%
#%    On the release/main branch, the script will attempt to derive
#%    the version based on latest merge commit (e.g. if you follow
#%    the [Github flow][2] with sem-versioned branch names) or
#%    by the latest git tag created.
#%
#%    If the sem-versioned value could NOT be derived from the tag
#%    or merged commits, we assume it's a first release version.
#%    (i.e. "v1.0.0")
#%
#%    Else if latest tag commit ID is the same as the HEAD then no change
#%    occurred, therefore same tag will be returned.
#%
#%    Else if the sem-versioned value derived is the SAME as latest existing
#%    tag, a minor incremented version will be returned. (i.e. "v2.0.0"
#%    => "v2.0.1")
#%
#%    Else for other/non-release branches, the derived version will be
#%    the current branch name suffixed with "-SNAPSHOT".
#%
#%    [1]:https://semver.org/
#%    [2]:https://docs.github.com/en/get-started/quickstart/github-flow
#================================================================
# END_OF_HEADER
#================================================================
function version_deriver() {
  local VERSION
  local CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
  local LATEST_TAG=$(git describe --tags --abbrev=0)

  if [ "$CURRENT_BRANCH" = "main" ]; then
    local LATEST_TAG_COMMIT_ID=$(git rev-list -n 1 "$LATEST_TAG")
    local LATEST_VERSIONED_MERGE_COMMIT=$(git log --merges -n 1 | grep request | grep -oh -P "(?<=\/)v?\d+\.\d+\.\d+$")

    if [ -z "$LATEST_TAG" ] && [ -z "$LATEST_VERSIONED_MERGE_COMMIT" ]; then
      VERSION="v1.0.0"
    elif [ "$LATEST_TAG_COMMIT_ID" = "$(git rev-parse HEAD)" ]; then
      VERSION=$LATEST_TAG
    elif [ "${LATEST_TAG/v/}" = "${LATEST_VERSIONED_MERGE_COMMIT/v/}" ] || [ -z "$LATEST_VERSIONED_MERGE_COMMIT" ]; then
      local MINOR_INCREMENTED_CURRENT_VERSION=$(echo "$LATEST_TAG" | awk -F. -v OFS=. '{$NF=$NF+1;print}')
      VERSION=$MINOR_INCREMENTED_CURRENT_VERSION
    else
      VERSION=$LATEST_VERSIONED_MERGE_COMMIT
    fi
  else
    VERSION="${CURRENT_BRANCH}-SNAPSHOT"
  fi

  echo "$VERSION"
}

version_deriver
