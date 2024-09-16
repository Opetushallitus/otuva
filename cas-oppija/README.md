# CAS-oppija

This project is based on [cas-overlay-template (branch 6.5)](https://github.com/apereo/cas-overlay-template/tree/6.5).

## Requirements

Java 11
Redis

Project includes gradle wrapper so it doesn't have to be installed. Just use `./gradlew` (unix) and `gradlew.bat` (win).

## Redis

Requires a Redis instance running on port 6379. Sessions and ticketRegistry are saved and updated in Redis. Easiest way is to run some redis docker image.

##Spring Webflow
There are a lot of custom Webflow configuration. So learning some is essential to manage this service
###Flow basics:
####State:
There are at least 3 types of states that we use in configuration:
    
- ActionState(runs actions and has transitions to other states), 
    
- DecicionState(Transfers to another state based on some kind of evaluate statement),
    
- EndState(ends current flow and has actions and a possible redirect parameter)
####Transition:
Transitions are events that signal flow state transformation to some other state.
####Action
Each State, when entered, excecutes related actions. Actions result in an event that causes some transition in flow.
###Existing flow json endpoint
Cas provides an actuator endpoint that lists the current login and logout flow configurations.
This is very helpful in debugging so use it! it is available at path /cas-oppija/actuator/springWebflow

it can be toggled on using the
``management.endpoint.springWebflow.enabled: true``
property.

## build

    ./gradlew clean build

## Running the application

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