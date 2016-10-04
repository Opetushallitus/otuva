package fi.vm.sade.kayttooikeus.dao;

import fi.vm.sade.kayttooikeus.domain.KayttoOikeusRyhma;

import java.util.List;

/**
 * Created by autio on 4.10.2016.
 */
public interface KayttoOikeusRyhmaDao{
    List<KayttoOikeusRyhma> listAll();
}
