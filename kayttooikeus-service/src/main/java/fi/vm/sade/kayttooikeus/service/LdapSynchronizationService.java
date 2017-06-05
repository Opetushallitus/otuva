package fi.vm.sade.kayttooikeus.service;

/**
 * Palvelu henkilötietojen LDAP-synkronointiin.
 */
public interface LdapSynchronizationService {

    /**
     * Lisää kaikki henkilöt synkronointijonoon, joka tyhjennetään yöaikaan.
     */
    void updateAllAtNight();

    /**
     * Lisää käyttöoikeusryhmän henkilöt synkronointijonoon.
     *
     * @param kayttoOikeusRyhmaId käyttöoikeusryhmä id
     */
    void updateKayttoOikeusRyhma(Long kayttoOikeusRyhmaId);

    /**
     * Lisää henkilön synkronointijonoon.
     *
     * @param henkiloOid henkilö oid
     */
    void updateHenkilo(String henkiloOid);

    /**
     * Lisää henkilön synkronointijonon kärkeen.
     *
     * @param henkiloOid henkilö oid
     */
    void updateHenkiloAsap(String henkiloOid);

    /**
     * Synkronoi henkilön tiedot välittömästi.
     *
     * @param henkiloOid henkilö oid
     */
    void updateHenkiloNow(String henkiloOid);

    /**
     * Tyhjentää synkronointijonoa.
     */
    void runSynchronizer();

}
