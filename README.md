# Käyttöoikeuspalvelu

Käyttöoikeuspalvelu on henkilö-palvelusta eriytetty käyttöoikeusryhmien ja -anomusten hallintaan tarkoitettu palvelu.

## Teknologiat

### Palvelin
* Tomcat 7
* Java 8
* Spring 4
* QueryDSL
* PostgreSQL
* JPA / Hibernate 4

### Käännösautomaatio
* Maven 3

## Testien ajaminen

    mvn clean test
    
## Käynnistäminen

### kayttooikeus-service

Backend-palvelun voi käynnistää Tomcatilla:

    mvn tomcat7-run

## Kääntäminen

    mvn clean install
