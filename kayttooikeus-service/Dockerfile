FROM maven:3.9.12-amazoncorretto-21@sha256:fbb3d8b98a6564f5fa6779a465920e2f1dec15ac052b8df30a83be8e61811641 AS build
WORKDIR /app

COPY . .
RUN mvn clean package -s ./settings.xml -DskipTests

FROM amazoncorretto:21@sha256:9a236fde34eb07f427a89e7a73f19256af188e3476ed7210101b3ed4daa4de1b

WORKDIR /app
COPY --from=build /app/target/kayttooikeus.jar .


CMD [ "java", "-XX:InitialRAMPercentage=80.0", "-XX:MaxRAMPercentage=80.0", "-Dspring.config.additional-location=classpath:/config/${ENV}.yml", "--add-opens", "java.base/java.lang=ALL-UNNAMED", "--add-opens", "java.base/java.util=ALL-UNNAMED", "-jar", "kayttooikeus.jar" ]
