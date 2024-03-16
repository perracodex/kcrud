# https://ktor.io/docs/docker.html
# https://ktor.io/docs/docker-compose.html

# This Dockerfile variant include 2 stages, a full fat JAR build stage, and a final image stage.

# NOTE: See '.dockerignore' file for the list of files and directories that are included/excluded in the Docker image.
# If a new sub-project is added, make sure to include the new sub-project's files in the '.dockerignore' file.

#-------------------------------------------------------------------------------------------------
# Build stage.
# If the fat JAR is built locally, comment out the following 'build' stage.
FROM gradle:8.2-jdk17 AS build
LABEL authors="perracolabs"
LABEL image.tag="kcrud-build"
LABEL name="kcrud-build-image"

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon --info

# Final image stage.
FROM amazoncorretto:17
LABEL authors="perracolabs"
LABEL image.tag="kcrud"
LABEL name="kcrud-final-image"
EXPOSE 8080

# Uncomment the following COPY commands if the fat JAR is built locally without the above build stage.
# COPY build/libs/kcrud-1.0.0-all.jar /kcrud-1.0.0-all.jar
# COPY build/libs/keystore.p12 /keystore.p12

# Comment out the following COPY commands if skipping the above build stage.
COPY --from=build /home/gradle/src/build/libs/kcrud-1.0.0-all.jar /kcrud-1.0.0-all.jar
COPY --from=build /home/gradle/src/build/libs/keystore.p12 /keystore.p12
#COPY --from=build /home/gradle/src/documentation/documentation.yaml /documentation/documentation.yaml

ENTRYPOINT ["java","-jar","kcrud-1.0.0-all.jar"]

#-------------------------------------------------------------------------------------------------
# ENVOIRMENT VARIABLES.

# Set host environment varianle to 0.0.0.0, so the server listens on all interfaces.
ENV KCRUD_KTOR_DEPLOYMENT_HOST="0.0.0.0"

# To override more configuration settings at image level add them here.
# For more settings see the existing 'conf' files in the base project, under the resources folder.
# Alternatively, the projct '.env' file can be used to override settings at runtime.
