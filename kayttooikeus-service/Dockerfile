FROM maven:3.9.12-amazoncorretto-21@sha256:35184f2aace9ae2fe06d0c66525a0039f4cf8a226f89e0af8ea80a80549bd27e AS build
WORKDIR /app

COPY . .
RUN mvn clean package -s ./codebuild-mvn-settings.xml -DskipTests

FROM amazoncorretto:21.0.10@sha256:cbae4fac2af2df50b411c532f5a7af9ce56f00768298854a24c76057c6817106

WORKDIR /app
COPY --from=build /app/target/kayttooikeus.jar .


CMD [ "java", "-XX:InitialRAMPercentage=80.0", "-XX:MaxRAMPercentage=80.0", "-Dspring.config.additional-location=classpath:/config/${ENV}.yml", "--add-opens", "java.base/java.lang=ALL-UNNAMED", "--add-opens", "java.base/java.util=ALL-UNNAMED", "-jar", "kayttooikeus.jar" ]
