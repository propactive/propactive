#!/bin/bash
# shellcheck disable=SC2086
# Some CI runners do not have the standard UID and GID values (i.e. 1000:1000)
# This causes permission issues when mounting volumes and running commands as
# a standard user.
#
# To work around this, we allow the user to specify custom UID and GID values via:
# - Change UID:  `-e SET_DOCKER_USER_UID=$(shell id -u)`
# - Change GID:  `-e SET_DOCKER_USER_GID=$(shell id -g)`
#
# And we also let them provide a custom username to run the commands via:
# - Change user: `-e SET_DOCKER_USER=$(shell whoami)`
#
# Usecase example:
#   - The GitHub Actions runner uses a non-standard UID/GID to run
#     docker containers, this causes permission issues when mounting volumes.

# In our context, the default user is the one built by the official
# Gradle Docker image, see: https://hub.docker.com/_/gradle
DEFAULT_USER="gradle"
DEFAULT_USER_UID="$(id -u $DEFAULT_USER)"
DEFAULT_USER_GID="$(id -g $DEFAULT_USER)"

USER="${SET_DOCKER_USER:-$DEFAULT_USER}"
GPG_HOME_DIR="/home/$USER/.gnupg"

# Check if the user exists, if not, create the user
if ! id "$USER" &>/dev/null; then
    echo "User $USER does not exist, and not the default ($DEFAULT_USER) user, creating user..."
    useradd -m "$USER";
fi

# Update the UID and GID if specified and they're not matching
if [ -n "$SET_DOCKER_USER_UID" ] && [ "$DEFAULT_USER_UID" != "$SET_DOCKER_USER_UID" ]; then
    echo "Given UID: $SET_DOCKER_USER_UID, does not match the default UID: $DEFAULT_USER_UID"
    echo "Changing UID from $DEFAULT_USER_UID to $SET_DOCKER_USER_UID"
    usermod  -u "$SET_DOCKER_USER_UID" $USER;
    echo "Also changing user ownership of /home/$USER to UID $SET_DOCKER_USER_UID"
    chown -R "$SET_DOCKER_USER_UID" "/home/$USER"
fi
if [ -n "$SET_DOCKER_USER_GID"  ] && [ "$DEFAULT_USER_GID" != "$SET_DOCKER_USER_GID" ]; then
    echo "Given GID: $SET_DOCKER_USER_GID, does not match the default GID: $DEFAULT_USER_GID"
    echo "Changing GID from $DEFAULT_USER_GID to $SET_DOCKER_USER_GID"
    groupmod -g "$SET_DOCKER_USER_GID" $USER;
    echo "Also changing group ownership of /home/$USER to GID $SET_DOCKER_USER_GID"
    chgrp -R "$SET_DOCKER_USER_GID" "/home/$USER"
fi

# If a GPG private key is provided, proceed with the GPG setup
if [[ -n $GPG_PRIVATE_KEY ]]; then
    if [[ -z $GPG_PRIVATE_KEY_PASSPHRASE ]]; then
        echo "Error: GPG_PRIVATE_KEY_PASSPHRASE is not set. Unable to import the private key."
        exit 1
    fi

    if ! command -v gpg &> /dev/null; then
        echo "Error: gpg is not installed. Unable to import the private key."
        exit 1
    fi

    if [[ ! -d $GPG_HOME_DIR ]]; then
        echo "Creating GPG home directory: $GPG_HOME_DIR..."
        mkdir -p $GPG_HOME_DIR
    fi

    echo "Importing GPG private key..."
    # NOTE: We use `echo -e` to allow for `\n` to be interpreted as newlines
    #       as we escape the newlines in the GPG_PRIVATE_KEY variable to prevent
    #       Make from interpreting them as newlines and breaking the build.
    echo -e "$GPG_PRIVATE_KEY" | gpg --homedir $GPG_HOME_DIR --pinentry-mode loopback --passphrase "$GPG_PRIVATE_KEY_PASSPHRASE" --import

    echo "Setting safe permissions on $GPG_HOME_DIR..."
    chown -R $USER:$USER $GPG_HOME_DIR
    chmod 600 $GPG_HOME_DIR/*
    chmod 700 $GPG_HOME_DIR
else
    echo "No GPG private key provided. Skipping GPG setup."
fi

# Run the provided command as the specified (or default) user
# see: https://github.com/tianon/gosu
# shellcheck disable=SC2068 # NOTE: We allow the user to pass in arguments to the entrypoint, just be careful with quotation marks.
exec gosu $USER $@
