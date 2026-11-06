FROM maven:3.9.16-amazoncorretto-21@sha256:efdf660ec74911594e2a7f642e177ffba21b741c8e848223bf41a6d0fc367692 AS build
WORKDIR /app

COPY . .
RUN mvn clean package -s ./codebuild-mvn-settings.xml -DskipTests

FROM amazoncorretto:21.0.13@sha256:90100f194336ef2326f5efe1ee747395fd87d8210ad6d60463196aa0c4b0ad4e

WORKDIR /app
COPY --from=build /app/target/kayttooikeus.jar .


CMD [ "java", "-XX:InitialRAMPercentage=80.0", "-XX:MaxRAMPercentage=80.0", "-Dspring.config.additional-location=classpath:/config/${ENV}.yml", "--add-opens", "java.base/java.lang=ALL-UNNAMED", "--add-opens", "java.base/java.util=ALL-UNNAMED", "-jar", "kayttooikeus.jar" ]
