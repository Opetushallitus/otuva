FROM maven:3.9.16-amazoncorretto-21@sha256:8eba4f3c7158e760a9e2ffec33d91cf361ad6daa26639c339b66b54f99fcf38b AS build
WORKDIR /app

COPY . .
RUN mvn clean package -s ./codebuild-mvn-settings.xml -DskipTests

FROM amazoncorretto:21.0.11@sha256:0bee3cd54b789bb54b9f6bfbf2d89615e225e587e6eada2c7ca0a191d4689332

WORKDIR /app
COPY --from=build /app/target/kayttooikeus.jar .


CMD [ "java", "-XX:InitialRAMPercentage=80.0", "-XX:MaxRAMPercentage=80.0", "-Dspring.config.additional-location=classpath:/config/${ENV}.yml", "--add-opens", "java.base/java.lang=ALL-UNNAMED", "--add-opens", "java.base/java.util=ALL-UNNAMED", "-jar", "kayttooikeus.jar" ]
