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

COPY --chmod=755 <<"EOF" /app/entrypoint.sh
#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail

echo -n ${keystore_base64} | base64 --decode > /app/config/keystore.jks
echo -n ${sp_metadata} > /app/config/sp-metadata.xml
echo -n ${idp_metadata} > /app/config/idp-metadata.xml

echo -n ${registered_service_1} > /app/config/service-1.json
echo -n ${registered_service_2} > /app/config/service-2.json

exec java \
  -Dcas.standalone.configurationFile=/app/config/${ENV}.yml \
  -Dlog4j.configurationFile=file:///app/config/log4j2.xml \
  -Dfi.vm.sade.javautils.http.HttpServletRequestUtils.HARMLESS_URLS=/cas-oppija/actuator/health \
  -jar \
  cas.war
EOF

ENTRYPOINT [ "/app/entrypoint.sh" ]
