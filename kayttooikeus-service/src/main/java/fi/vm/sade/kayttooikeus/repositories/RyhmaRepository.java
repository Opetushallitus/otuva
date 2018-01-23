package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.Ryhma;
import java.util.List;
import java.util.Optional;
import org.springframework.data.ldap.repository.LdapRepository;

public interface RyhmaRepository extends LdapRepository<Ryhma>, RyhmaRepositoryCustom {

    Optional<Ryhma> findByNimi(String nimi);

    /**
     * Palauttaa käyttäjän ryhmät.
     *
     * @param kayttajaDn käyttäjä DN
     * @return ryhmät
     */
    List<Ryhma> findByKayttajat(String kayttajaDn);

}
