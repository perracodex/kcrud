# https://ktor.io/docs/docker.html
# https://ktor.io/docs/docker-compose.html

# This Dockerfile variant does not include the fat-JAR build stage, only the final image stage.
# It is intended to be used with a pre-built fat JAR.
# If the image is not found, use the next command: docker image prune -a

#-------------------------------------------------------------------------------------------------
# Final image stage.
FROM amazoncorretto:17
LABEL authors="perraco"
LABEL image.tag="kcrud"
LABEL name="kcrud-final-image"
EXPOSE 8080

RUN mkdir -p /app

COPY build/libs/kcrud-1.0.0-all.jar /app/kcrud-1.0.0-all.jar
COPY build/libs/keystore.p12 /app/keystore.p12

ENTRYPOINT ["java", "-jar", "/app/kcrud-1.0.0-all.jar"]

#-------------------------------------------------------------------------------------------------
# ENVOIRMENT VARIABLES.

# Set host environment varianle to 0.0.0.0, so the server listens on all interfaces.
ENV KCRUD_KTOR_DEPLOYMENT_HOST="0.0.0.0"

# Set the SSL key store environment variable.
ENV KCRUD_KTOR_SECURITY_SSL_KEY_STORE="/app/keystore.p12"

# To override more configuration settings at image level add them here.
# For more settings see the existing 'conf' files in the base project, under the resources folder.
# Alternatively, the projct '.env' file can be used to override settings at runtime.