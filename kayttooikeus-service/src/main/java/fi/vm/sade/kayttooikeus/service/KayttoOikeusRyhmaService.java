package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.service.dto.KayttoOikeusRyhmaDto;

import java.util.List;

/**
 * Created by autio on 4.10.2016.
 */
public interface KayttoOikeusRyhmaService {
    List<KayttoOikeusRyhmaDto> listAllKayttoOikeusRyhmas();
}
