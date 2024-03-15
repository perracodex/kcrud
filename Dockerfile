FROM openjdk:17
EXPOSE 8080
RUN mkdir /app

COPY build/libs/kcrud-1.0.0-all.jar /kcrud-1.0.0-all.jar
COPY build/libs/keystore.p12 /keystore.p12

ENTRYPOINT ["java","-jar","/kcrud-1.0.0-all.jar"]
