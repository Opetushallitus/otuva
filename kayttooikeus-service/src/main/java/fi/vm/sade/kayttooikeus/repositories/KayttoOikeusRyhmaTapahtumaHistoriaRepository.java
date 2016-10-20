package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhmaTapahtumaHistoria;

import java.util.List;

public interface KayttoOikeusRyhmaTapahtumaHistoriaRepository {
    List<KayttoOikeusRyhmaTapahtumaHistoria> findByHenkiloInOrganisaatio(String henkiloOid, String organisaatioOid);
}
