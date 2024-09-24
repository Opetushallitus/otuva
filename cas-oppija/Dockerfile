FROM amazoncorretto:11 AS build
WORKDIR /app

COPY gradle ./gradle
COPY gradlew .
COPY gradle.properties .
COPY settings.gradle .
COPY build.gradle .

COPY lombok.config .
COPY src ./src
RUN ./gradlew clean build -x test

FROM amazoncorretto:11

WORKDIR /app
COPY config ./config
COPY --from=build /app/build/libs/cas.war .
ADD https://static.apro.tunnistus.fi/static/metadata/idp-metadata.xml /app/config/

CMD [ "echo -n ${keystore_base64} | base64 --decode > /app/config/keystore.jks &&", \
      "java", \
      "-Dcas.standalone.configurationFile=/app/config/${ENV}.yml", \
      "-Dlog4j.configurationFile=file:///app/config/log4j2.xml", \
      "-Dfi.vm.sade.javautils.http.HttpServletRequestUtils.HARMLESS_URLS=/cas-oppija/actuator/health", \
      "-jar", \
      "cas.war" ]

