FROM maven:3.9.16-amazoncorretto-21@sha256:efdf660ec74911594e2a7f642e177ffba21b741c8e848223bf41a6d0fc367692 AS build
WORKDIR /app

COPY . .
RUN mvn clean package -s ./codebuild-mvn-settings.xml -DskipTests

FROM amazoncorretto:21.0.13@sha256:f9a740a8c4b33f8356490a533618dbd0f2a3e72442de309c73572d9b1e4b40fa

WORKDIR /app
COPY --from=build /app/target/kayttooikeus.jar .


CMD [ "java", "-XX:InitialRAMPercentage=80.0", "-XX:MaxRAMPercentage=80.0", "-Dspring.config.additional-location=classpath:/config/${ENV}.yml", "--add-opens", "java.base/java.lang=ALL-UNNAMED", "--add-opens", "java.base/java.util=ALL-UNNAMED", "-jar", "kayttooikeus.jar" ]
