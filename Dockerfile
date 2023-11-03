# Gradle's official Ubuntu JDK17 image.
#   See:
#     https://github.com/keeganwitt/docker-gradle/blob/master/jdk17/Dockerfile
#   NOTE:
#     We use a Ubuntu image instead of the slim Alpine image as official Alpine OS uses musl libc
#     instead of the more common glibc used by most other Linux distributions. Many pre-built
#     binaries, including many JDK distributions, expect glibc, so running Alpine risks us
#     encounting unexpected errors and wasting time debugging them for no functional benefit.
FROM gradle:8.4.0-jdk17-jammy

# Image Metadata
LABEL app="runner" \
      description="Docker image to build, publish, and run the propactive framework." \
      maintainer="U-ways (work@u-ways.info)" \
      url="https://github.com/propactive/propactive"

# Installing required packages.
RUN apt-get update -q && \
    apt-get install -yq --no-install-recommends gnupg gosu && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

USER root

# Define $HOME directory to the gradle user's home directory so
# our subsequent definitions have the correct $HOME as we will
# run commands in the contianer using the gradle user.
ARG HOME=/home/gradle

# Ensure appropriate environment variables set.
ENV GRADLE_USER_HOME=$HOME/.gradle \
    MAVEN_HOME=$HOME/.m2 \
    GPG_HOME=$HOME/.gnupg \
    WORKING_DIR=$HOME/propactive

# Add and set permissions for the entrypoint script
COPY ./scripts/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
# We use the exec form of ENTRYPOINT to allows us to gracefully
# handle SIGTERM and SIGINT signals sent to the container.
# See: https://stackoverflow.com/a/77202288/5037430
ENTRYPOINT ["/entrypoint.sh"]

# Create necessary directories (if they don't exist)
# and set their permissions to the gradle user.
RUN mkdir -p \
      $GRADLE_USER_HOME \
      $MAVEN_HOME \
      $GPG_HOME \
      $WORKING_DIR && \
    chown -R gradle:gradle $HOME

# Specify volume mount points to avoid accidental data persistence in anonymous volumes.
VOLUME ["$MAVEN_HOME", "$GRADLE_USER_HOME", "$GPG_HOME", "$WORKING_DIR"]

# Set the working directory to the project's directory.
WORKDIR $WORKING_DIR

# Default command to run when the container starts.
CMD ["./gradlew", "tasks"]
