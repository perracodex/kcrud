# https://ktor.io/docs/docker.html
# https://ktor.io/docs/docker-compose.html

# Full-build Dockerfile.

#-------------------------------------------------------------------------------------------------
# Build stage.
FROM gradle:8.8-jdk17 AS build
LABEL authors="perracodex"
LABEL image.tag="kcrud-build"
LABEL name="kcrud-build-image"

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon --info

# Final image stage.
FROM amazoncorretto:17
LABEL authors="perracodex"
LABEL image.tag="kcrud"
LABEL name="kcrud-final-image"

# Expose the ports the container listens on during runtime.
EXPOSE 8080
EXPOSE 8443

# Create the final output directory.
RUN mkdir -p /app

# Copy the newly built jar file from the build stage to the final image.
COPY --from=build /home/gradle/src/build/libs/kcrud-1.0.0-all.jar /app/kcrud-1.0.0-all.jar
# Copy the keystore file from the source directory to the final image.
COPY keystore.p12 /app/keystore.p12

#-------------------------------------------------------------------------------------------------
# Environment variables.

# Set host to 0.0.0.0 to listens on all interfaces.
ENV KCRUD_KTOR_DEPLOYMENT_HOST="0.0.0.0"

# Set the SSL key location.
ENV KCRUD_KTOR_SECURITY_SSL_KEY_STORE="/app/keystore.p12"

# To override more configuration settings at image level add them here.
# For more settings see the existing 'conf' files in the Core module, under the resources folder.

#-------------------------------------------------------------------------------------------------
# Execution entrypoint.
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/kcrud-1.0.0-all.jar"]
