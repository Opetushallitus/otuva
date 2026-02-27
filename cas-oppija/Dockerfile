FROM amazoncorretto:21 AS build
WORKDIR /app

COPY gradle ./gradle
COPY gradlew .
COPY gradle.properties .
COPY settings.gradle .
COPY build.gradle .
COPY gradle.lockfile .

COPY lombok.config .
COPY src ./src
RUN ./gradlew clean build -x test

FROM amazoncorretto:21

WORKDIR /app
COPY services ./services
COPY config ./config
COPY --from=build /app/build/libs/cas.war .

COPY --chmod=755 <<"EOF" /app/entrypoint.sh
#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail

echo -n ${keystore_base64} | base64 --decode > /app/config/keystore.jks
echo -n ${sp_metadata} > /app/config/sp-metadata.xml

echo -n ${registered_service_1} > /app/config/service-1.json
echo -n ${registered_service_2} > /app/config/service-2.json
echo -n ${registered_service_tiedotuspalvelu} > /app/config/tiedotuspalvelu-3.json

exec java \
  -XX:InitialRAMPercentage=80.0 \
  -XX:MaxRAMPercentage=80.0 \
  -Dcas.standalone.configurationFile=/app/config/${ENV}.yml \
  -jar \
  cas.war
EOF

ENTRYPOINT [ "/app/entrypoint.sh" ]
