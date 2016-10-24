# Käyttöoikeuspalvelu

Käyttöoikeuspalvelu on henkilö-palvelusta eriytetty käyttöoikeusryhmien ja -anomusten hallintaan tarkoitettu palvelu.

## Teknologiat

### Palvelin
* Tomcat 7
* Java 8
* Spring Boot
* QueryDSL
* PostgreSQL
* JPA / Hibernate 4

### Käännösautomaatio
* Maven 3

## Testien ajaminen

    mvn clean test
    
## Käynnistäminen

### kayttooikeus-service

dev-profiilia varten kopioi security-context-backend-local-testing.properties.template oph-configuraation hakemistoon ja nimeä tiedosto security-context.properties

Backend-palvelun käynnistäminen dev-profiililla:

    java -jar -Dspring.profiles.active=dev -Dspring.config.location=/<path>/<to>/oph-configuration/kayttooikeus.yml kayttooikeus-service/target/kayttooikeus-service-1.0.0-SNAPSHOT.jar

## Kääntäminen

    mvn clean install
