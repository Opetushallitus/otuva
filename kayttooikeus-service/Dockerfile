FROM maven:3.9.16-amazoncorretto-21@sha256:825646d3e530a9caec81a6a0e0cddea4d314bf5692b5861d52bf4e0d48277d08 AS build
WORKDIR /app

COPY . .
RUN mvn clean package -s ./codebuild-mvn-settings.xml -DskipTests

FROM amazoncorretto:21.0.11@sha256:9c3e09f59a5c2319a161cef94c7295ce28d6a5a98ed1773088a73778807676e6

WORKDIR /app
COPY --from=build /app/target/kayttooikeus.jar .


CMD [ "java", "-XX:InitialRAMPercentage=80.0", "-XX:MaxRAMPercentage=80.0", "-Dspring.config.additional-location=classpath:/config/${ENV}.yml", "--add-opens", "java.base/java.lang=ALL-UNNAMED", "--add-opens", "java.base/java.util=ALL-UNNAMED", "-jar", "kayttooikeus.jar" ]
