FROM amazoncorretto:21 AS build
WORKDIR /app

COPY gradle ./gradle
COPY gradlew .
COPY gradle.properties .
COPY settings.gradle .
COPY build.gradle .

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

mkdir -p /app/etc
echo -n ${haka_keystore_base64} | base64 --decode > /app/etc/hakakeystore.jks
echo -n ${haka_sp_metadata} > /app/etc/hakasp.xml

exec java \
  -XX:InitialRAMPercentage=80.0 \
  -XX:MaxRAMPercentage=80.0 \
  -Dcas.standalone.configurationFile=/app/config/${ENV}.yml \
  -Dfi.vm.sade.javautils.http.HttpServletRequestUtils.HARMLESS_URLS=/cas/actuator/health \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/tmp/heap_dump.hprof \
  -XX:OnOutOfMemoryError="yum install -y aws-cli > /dev/null && aws s3 mv /tmp/heap_dump.hprof s3://${HEAP_DUMP_BUCKET}/'date+%F_%T'.hprof ; kill -9 %p" \
  -jar \
  cas.war
EOF

ENTRYPOINT [ "/app/entrypoint.sh" ]
