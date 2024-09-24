FROM maven:3.9.8-amazoncorretto-21 AS build
WORKDIR /app

COPY . .
RUN echo $MVN_SETTINGSXML
RUN echo $ssm_mvn_settingsxml
RUN echo $MVN_SETTINGSXML > ./settings.xml
RUN mvn clean package -s ./settings.xml -DskipTests

FROM amazoncorretto:21

WORKDIR /app
COPY --from=build /app/target/kayttooikeus.jar .


CMD [ "java", "-Dspring.config.additional-location=classpath:/config/${ENV}.yml", "--add-opens", "java.base/java.lang=ALL-UNNAMED", "--add-opens", "java.base/java.util=ALL-UNNAMED", "-jar", "kayttooikeus.jar" ]
