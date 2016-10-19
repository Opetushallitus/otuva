package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.OrganisaatioViite;

import java.util.List;

public interface OrganisaatioViiteRepository {
    List<OrganisaatioViite> findByKayttoOikeusRyhmaId(Long id);
}
