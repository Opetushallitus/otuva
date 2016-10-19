package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.MyonnettyKayttoOikeusRyhmaTapahtuma;

import java.util.List;

public interface MyonnettyKayttoOikeusRyhmaTapahtumaRepository {
    List<MyonnettyKayttoOikeusRyhmaTapahtuma> findValidByHenkiloOid(String henkiloOid);

    List<MyonnettyKayttoOikeusRyhmaTapahtuma> findByHenkiloInOrganisaatio(String henkiloOid, String organisaatioOid);
}
