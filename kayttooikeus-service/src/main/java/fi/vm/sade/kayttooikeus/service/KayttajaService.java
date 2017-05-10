package fi.vm.sade.kayttooikeus.service;

import java.util.Collection;

public interface KayttajaService {

    /**
     * Palauttaa nykyisen käyttäjän OID:n.
     *
     * @return käyttäjä oid
     */
    String getOid();

    /**
     * Palauttaa nykyiset käyttäjän roolit.
     *
     * @return käyttäjäroolit
     */
    Collection<String> getRoolit();

}
