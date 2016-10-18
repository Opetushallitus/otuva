package fi.vm.sade.kayttooikeus.repositories;

import java.util.List;

public interface OrganisaatioHenkiloRepository {
    List<String> findDistinctOrganisaatiosForHenkiloOid(String henkiloOid);
}
