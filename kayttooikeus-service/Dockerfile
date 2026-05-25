FROM maven:3.9.16-amazoncorretto-21@sha256:5b37fc0d5ecb552ec57dfa215e25a3bee9475f9cbf4eab2737e4fa9d9730df75 AS build
WORKDIR /app

COPY . .
RUN mvn clean package -s ./codebuild-mvn-settings.xml -DskipTests

FROM amazoncorretto:21.0.11@sha256:e5c1419310bfcdf5a176c9b3297bc1abadf469ebffdefbe66fe3ffc91f236fe9

WORKDIR /app
COPY --from=build /app/target/kayttooikeus.jar .


CMD [ "java", "-XX:InitialRAMPercentage=80.0", "-XX:MaxRAMPercentage=80.0", "-Dspring.config.additional-location=classpath:/config/${ENV}.yml", "--add-opens", "java.base/java.lang=ALL-UNNAMED", "--add-opens", "java.base/java.util=ALL-UNNAMED", "-jar", "kayttooikeus.jar" ]
