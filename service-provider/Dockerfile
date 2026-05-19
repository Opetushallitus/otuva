FROM maven:3.9.8-amazoncorretto-21@sha256:12ced8e1a5ad060aed3f30a7b430093de5889cef7894dd49a0d67a1a0d0a5a3a AS build

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM tomcat:9-jre21@sha256:8591e677f12de5aea6533be1d2adbabc78905870b259c7bd2a47070b21cbf16a

WORKDIR /app
COPY config ./config
COPY --from=build /app/target/service-provider-app.war /usr/local/tomcat/webapps/

COPY --chmod=755 <<"EOF" /app/entrypoint.sh
#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail

cp /app/config/${ENV}.properties /app/config/service-provider.properties
sed -i -e 's/${ssm_keystore_password}/'"$ssm_keystore_password"'/g' /app/config/service-provider.properties
sed -i -e 's/${ssm_app_username_to_usermanagement}/'"$ssm_app_username_to_usermanagement"'/g' /app/config/service-provider.properties
sed -i -e 's/${ssm_app_password_to_usermanagement}/'"$ssm_app_password_to_usermanagement"'/g' /app/config/service-provider.properties
sed -i -e 's/${ssm_sp_keyalias}/'"$ssm_sp_keyalias"'/g' /app/config/service-provider.properties
sed -i -e 's/${ssm_sp_keyalias_secondary}/'"$ssm_sp_keyalias_secondary"'/g' /app/config/service-provider.properties
sed -i -e 's/${ssm_sp_keypassword}/'"$ssm_sp_keypassword"'/g' /app/config/service-provider.properties
sed -i -e 's/${ssm_mpassid_keyalias}/'"$ssm_mpassid_keyalias"'/g' /app/config/service-provider.properties
echo -n ${hakasp} > /app/config/hakasp.xml
echo -n ${keystore} | base64 --decode > /app/config/keystore.jks

cd $CATALINA_HOME/bin
./catalina.sh run
EOF

ENTRYPOINT ["/app/entrypoint.sh"]
