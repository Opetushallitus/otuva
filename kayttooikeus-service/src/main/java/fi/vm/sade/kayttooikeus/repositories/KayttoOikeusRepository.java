package fi.vm.sade.kayttooikeus.repositories;

/**
 * User: tommiratamaa
 * Date: 12/10/2016
 * Time: 15.47
 */
public interface KayttoOikeusRepository {
    boolean isHenkiloMyonnettyKayttoOikeusToPalveluInRole(String henkiloOid, String palvelu, String role);
}
