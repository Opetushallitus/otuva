# Käyttöoikeusanomuksien ja kutsujen virkailijan listaus -frontend

## Description

React/BaconJS SPA

## Tech

See [package.json](./package.json).

## Development

`npm start`

To enable calls to kayttooikeus backend, start it as well. See [README](../README.md). Proxy configuration will forward the API requests. 

Alternatively without need to install node/npm locally, just run:
mvn -Pdev-server install

To change logged in user authenticated with basic authentication, change "proxy"."/kayttooikeus-service/*"."auth" property to user:password in package.json

## Tests

`npm run test` (starts watcher)

`npm run testci` (runs all tests once)

## Build

To produce optimized bundle in _target_ dir:

`npm run build`

To produce a JAR:

`mvn clean install`

## TODO

- [ ] laod oph/urls props client (when supplied as npm package) 
- [ ] load common styles (when supplied as npm package)
- [ ] remove initial ajax request pendingP's as they seem unneeded 
- [ ] remove favicon
- [ ] set up proxying to ONR and other backends
- [ ] Enhance backend mocks (koodisto, lokalisaatio, etc.)
- [ ] improve test coverage
- [x] Package as JAR and serve with Tomcat?
- [x] Impl confirmation modal
- [x] Impl basic layout positioning with flexbox
- [x] Eject out of crete-react-app
