package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.dto.KayttoOikeudenTila;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhmaTapahtumaHistoria;
import fi.vm.sade.kayttooikeus.model.MyonnettyKayttoOikeusRyhmaTapahtuma;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRyhmaTapahtumaHistoriaDataRepository;
import fi.vm.sade.kayttooikeus.repositories.MyonnettyKayttoOikeusRyhmaTapahtumaDataRepository;
import fi.vm.sade.kayttooikeus.repositories.MyonnettyKayttoOikeusRyhmaTapahtumaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import fi.vm.sade.kayttooikeus.service.MyonnettyKayttoOikeusService;
import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;
import fi.vm.sade.kayttooikeus.service.exception.DataInconsistencyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fi.vm.sade.kayttooikeus.service.LdapSynchronizationService;

@Service
@Transactional
@RequiredArgsConstructor
public class MyonnettyKayttoOikeusServiceImpl implements MyonnettyKayttoOikeusService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyonnettyKayttoOikeusServiceImpl.class);

    private final PermissionCheckerService permissionCheckerService;
    private final HenkiloDataRepository henkiloDataRepository;
    private final MyonnettyKayttoOikeusRyhmaTapahtumaRepository myonnettyKayttoOikeusRyhmaTapahtumaRepository;
    private final MyonnettyKayttoOikeusRyhmaTapahtumaDataRepository myonnettyKayttoOikeusRyhmaTapahtumaDataRepository;
    private final KayttoOikeusRyhmaTapahtumaHistoriaDataRepository kayttoOikeusRyhmaTapahtumaHistoriaDataRepository;
    private final LdapSynchronizationService ldapSynchronizationService;

    @Override
    public void poistaVanhentuneet() {
        poistaVanhentuneet(permissionCheckerService.getCurrentUserOid());
    }

    @Override
    public void poistaVanhentuneet(String kasittelijaOid) {
        LOGGER.info("Vanhentuneiden käyttöoikeuksien poisto aloitetaan");
        Henkilo kasittelija = henkiloDataRepository.findByOidHenkilo(kasittelijaOid)
                .orElseThrow(() -> new DataInconsistencyException("Henkilöä ei löydy käyttäjän OID:lla " + kasittelijaOid));
        List<MyonnettyKayttoOikeusRyhmaTapahtuma> kayttoOikeudet = myonnettyKayttoOikeusRyhmaTapahtumaRepository.findByVoimassaLoppuPvmBefore(LocalDate.now());

        for (MyonnettyKayttoOikeusRyhmaTapahtuma kayttoOikeus : kayttoOikeudet) {
            String henkiloOid = kayttoOikeus.getOrganisaatioHenkilo().getHenkilo().getOidHenkilo();

            KayttoOikeusRyhmaTapahtumaHistoria historia = kayttoOikeus.toHistoria(
                    kasittelija, KayttoOikeudenTila.SULJETTU,
                    LocalDateTime.now(), "Oikeuksien poisto, vanhentunut");
            kayttoOikeusRyhmaTapahtumaHistoriaDataRepository.save(historia);

            myonnettyKayttoOikeusRyhmaTapahtumaDataRepository.delete(kayttoOikeus);
            ldapSynchronizationService.updateHenkilo(henkiloOid);
        }
        LOGGER.info("Vanhentuneiden käyttöoikeuksien poisto päättyy: poistettiin {} käyttöoikeutta", kayttoOikeudet.size());
    }

}
