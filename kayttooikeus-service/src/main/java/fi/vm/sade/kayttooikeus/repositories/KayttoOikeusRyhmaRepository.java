package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;

import java.util.List;

public interface KayttoOikeusRyhmaRepository {
    List<KayttoOikeusRyhma> listAll();

    List<KayttoOikeusRyhma> findByIdList(List<Long> idList);
}
