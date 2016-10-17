package fi.vm.sade.kayttooikeus.repositories;

import java.util.List;

/**
 * User: tommiratamaa
 * Date: 12/10/2016
 * Time: 14.44
 */
public interface OrganisaatioHenkiloRepository {
    List<String> findDistinctOrganisaatiosForHenkiloOid(String henkiloOid);
}
