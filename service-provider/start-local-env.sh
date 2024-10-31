#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
readonly repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

function main {
  cd "$repo"
  use_correct_jvm_version
  ./mvnw clean verify org.codehaus.cargo:cargo-maven3-plugin:run \
    -Dcargo.maven.containerId=tomcat9x \
    -Dcargo.maven.containerUrl=https://repo.maven.apache.org/maven2/org/apache/tomcat/tomcat/9.0.96/tomcat-9.0.96.zip
}

function use_correct_jvm_version {
  JAVA_HOME="$( /usr/libexec/java_home -v "21" )"
  export JAVA_HOME
}

main "$@"
