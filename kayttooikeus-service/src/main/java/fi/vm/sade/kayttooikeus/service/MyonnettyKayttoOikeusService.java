package fi.vm.sade.kayttooikeus.service;

public interface MyonnettyKayttoOikeusService {

    /**
     * Poistaa vanhentuneet käyttöoikeudet henkilöiltä.
     */
    void poistaVanhentuneet();

    /**
     * Poistaa vanhentuneet käyttöoikeudet henkilöiltä. Tämä metodi on tehty
     * vain sisäistä ajastusta varten. Jos muutetaan käyttämään ulkoista
     * ajastusta, voidaan tämä metodi poistaa ja käyttää
     * {@link #poistaVanhentuneet()} -metodia.
     *
     * @param kasittelijaOid käsittelijä oid
     */
    void poistaVanhentuneet(String kasittelijaOid);

}
