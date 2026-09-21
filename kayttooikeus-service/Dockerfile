FROM maven:3.9.16-amazoncorretto-21@sha256:cb0791f5fbbb01bec717ed7bcf217b2356fe46e25de16115566c96bcbb6fe343 AS build
WORKDIR /app

COPY . .
RUN mvn clean package -s ./codebuild-mvn-settings.xml -DskipTests

FROM amazoncorretto:25.0.4@sha256:ec395950366b60da545925171be5e8f457f7b33d034ee12b3a0110657da285fc

WORKDIR /app
COPY --from=build /app/target/kayttooikeus.jar .


CMD [ "java", "-XX:InitialRAMPercentage=80.0", "-XX:MaxRAMPercentage=80.0", "-Dspring.config.additional-location=classpath:/config/${ENV}.yml", "--add-opens", "java.base/java.lang=ALL-UNNAMED", "--add-opens", "java.base/java.util=ALL-UNNAMED", "-jar", "kayttooikeus.jar" ]
