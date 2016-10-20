package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.KayttoOikeus;

import java.util.List;

public interface KayttoOikeusRepository {
    boolean isHenkiloMyonnettyKayttoOikeusToPalveluInRole(String henkiloOid, String palvelu, String role);

    List<KayttoOikeus> findByKayttoOikeusRyhma(Long id);

    List<Long> findByKayttoOikeusRyhmaIds(Long id);
}
