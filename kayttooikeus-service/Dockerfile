FROM maven:3.9.12-amazoncorretto-21@sha256:22cd2d8f18db7e8932beab8c2870d23d157d2472ebec0aa93d4f531a3f0e52a1 AS build
WORKDIR /app

COPY . .
RUN mvn clean package -s ./codebuild-mvn-settings.xml -DskipTests

FROM amazoncorretto:21.0.10@sha256:704f62cb32e0850ef3bc8d589eb926780bd3a51611df49cc0a0d432f90615372

WORKDIR /app
COPY --from=build /app/target/kayttooikeus.jar .


CMD [ "java", "-XX:InitialRAMPercentage=80.0", "-XX:MaxRAMPercentage=80.0", "-Dspring.config.additional-location=classpath:/config/${ENV}.yml", "--add-opens", "java.base/java.lang=ALL-UNNAMED", "--add-opens", "java.base/java.util=ALL-UNNAMED", "-jar", "kayttooikeus.jar" ]
