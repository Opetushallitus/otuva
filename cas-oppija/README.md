# CAS-oppija

This project is based on [cas-overlay-template (branch 6.0)](https://github.com/apereo/cas-overlay-template/tree/6.0).

## Requirements

Java 11

Project includes gradle wrapper so it doesn't have to be installed. Just use `./gradlew` (unix) and `gradlew.bat` (win).

## Database

    docker run --name cas-oppija-db -p 5432:5432 -e POSTGRES_USER=cas-oppija -e POSTGRES_PASSWORD=cas-oppija -e POSTGRES_DB=cas-oppija -d postgres:11.5

## Build

    gradle build

## Run

    java -jar build/libs/cas.war

## Configuration

Default configuration is in [application.yml](src/main/resources/application.yml).
To extend or override default configuration add system variables to run command:

    -Dserver.port=8081

or create yml file

    server.port: 8081

... and add following to run command:

    -Dcas.standalone.configurationFile=<path_to_file>

To utilize other OPH services from test environment, add following to run command:

    -DbaseUrl=https://<domain_to_test_environment>
    -Dservice-user.username=<username_to_test_environment>
    -Dservice-user.password=<password_to_test_environment>

Note that `baseUrl` parameter cannot be used with yml file!
(See [java-utils/java-properties](https://github.com/Opetushallitus/java-utils/tree/master/java-properties)).
