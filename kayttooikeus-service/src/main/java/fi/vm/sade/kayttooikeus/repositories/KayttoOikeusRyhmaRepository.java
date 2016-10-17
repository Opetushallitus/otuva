package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;

import java.util.List;

/**
 * Created by autio on 4.10.2016.
 */
public interface KayttoOikeusRyhmaRepository {
    List<KayttoOikeusRyhma> listAll();
}
