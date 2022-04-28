#!/bin/sh

ENTRYPOINT_DEBUG=${ENTRYPOINT_DEBUG:-false}
JVM_DEBUG=${JVM_DEBUG:-false}
JVM_DEBUG_PORT=${JVM_DEBUG_PORT:-5000}
JVM_DEBUG_SUSPEND=${JVM_DEBUG_SUSPEND:-n}
JVM_MEM_OPTS=${JVM_MEM_OPTS:--Xms512m -Xmx4096M}
JVM_EXTRA_OPTS=${JVM_EXTRA_OPTS:--server -noverify -XX:+TieredCompilation -XX:TieredStopAtLevel=1}

if [ $JVM_DEBUG == "true" ]; then
  JVM_EXTRA_OPTS="${JVM_EXTRA_OPTS} -Xdebug -Xrunjdwp:transport=dt_socket,address=*:${JVM_DEBUG_PORT},server=y,suspend=${JVM_DEBUG_SUSPEND}"
fi

if [ $ENTRYPOINT_DEBUG == "true" ]; then
  JVM_EXTRA_OPTS="${JVM_EXTRA_OPTS} -Ddebug=true"
fi

echo -e "\nChecking java..."
java -version

echo -e "\nCreating CAS configuration directories..."
mkdir -p /etc/cas/config
mkdir -p /etc/cas/services

echo "Listing provided CAS docker artifacts..."
ls -R docker/cas

echo -e "\nMoving CAS configuration artifacts..."
mv docker/cas/thekeystore /etc/cas 2>/dev/null
mv docker/cas/config/*.* /etc/cas/config 2>/dev/null
mv docker/cas/services/*.* /etc/cas/services 2>/dev/null

if [ -d /etc/cas ]; then
  echo -e "\nListing CAS configuration under /etc/cas..."
  ls -R /etc/cas
fi
echo -e "\nRemote debugger configured on port ${JVM_DEBUG_PORT} with suspend=${JVM_DEBUG_SUSPEND}: ${JVM_DEBUG}"
echo -e "\nJava args: ${JVM_MEM_OPTS} ${JVM_EXTRA_OPTS}"

echo -e "\nRunning CAS @ cas.war"
# shellcheck disable=SC2086
exec java $JVM_EXTRA_OPTS $JVM_MEM_OPTS -jar cas.war "$@"
