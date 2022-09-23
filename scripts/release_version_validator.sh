#!/bin/bash
# shellcheck disable=SC2155
#================================================================
# HEADER
#================================================================
#% SYNOPSIS
#+    release_version_validator arg
#%
#% DESCRIPTION
#%    This script will validate a given release version against
#%    the [Semantic Versioning 2.0.0][1] standard.
#%
#% EXAMPLES
#%    release_version_validator "1.0.1" # => FAIL
#%    release_version_validator "1.0.1" # => PASS
#%
#%    release_version_validator "2      # => FAIL
#%    release_version_validator "2.0    # => FAIL
#%    release_version_validator "2.0.0" # => PASS
#%
#%    [1]:https://semver.org/
#================================================================
# END_OF_HEADER
#================================================================
function release_version_validator()
{
  local GIVEN_VERSION=$1

  if echo "$GIVEN_VERSION" | grep -Pq "^\d+\.\d+\.\d+$"; then
    return 0
  else
    echo "The current derived version: '$GIVEN_VERSION' is not sem-versioned, and therefore will not be published..."
    exit 1
  fi
}

release_version_validator "$@"
