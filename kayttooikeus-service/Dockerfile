FROM maven:3.9.16-amazoncorretto-21@sha256:141879c4499594c3d1181aeeccd5f6b563faed4ecc03a5635b4962657baefe19 AS build
WORKDIR /app

COPY . .
RUN mvn clean package -s ./codebuild-mvn-settings.xml -DskipTests

FROM amazoncorretto:21.0.11@sha256:cc5af6b7df0e0bf2f5952dd53fdc90e011d33c26e31c749357227808dca871d7

WORKDIR /app
COPY --from=build /app/target/kayttooikeus.jar .


CMD [ "java", "-XX:InitialRAMPercentage=80.0", "-XX:MaxRAMPercentage=80.0", "-Dspring.config.additional-location=classpath:/config/${ENV}.yml", "--add-opens", "java.base/java.lang=ALL-UNNAMED", "--add-opens", "java.base/java.util=ALL-UNNAMED", "-jar", "kayttooikeus.jar" ]
