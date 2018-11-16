package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.TunnistusToken;
import fi.vm.sade.kayttooikeus.repositories.TunnistusTokenDataRepository;
import fi.vm.sade.kayttooikeus.service.EmailVerificationService;
import fi.vm.sade.kayttooikeus.service.IdentificationService;
import fi.vm.sade.kayttooikeus.service.exception.LoginTokenException;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloDto;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloUpdateDto;
import fi.vm.sade.properties.OphProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static fi.vm.sade.kayttooikeus.model.Identification.CAS_AUTHENTICATION_IDP;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final TunnistusTokenDataRepository tunnistusTokenDataRepository;
    private final OphProperties ophProperties;
    private final OppijanumerorekisteriClient oppijanumerorekisteriClient;
    private final IdentificationService identificationService;

    @Override
    @Transactional
    public String emailVerification(HenkiloUpdateDto henkiloUpdateDto, String kielisyys, String loginToken) {
        TunnistusToken tunnistusToken = tunnistusTokenDataRepository.findByValidLoginToken(loginToken)
                .orElse(null);

        if(tunnistusToken == null) {
            log.error(String.format("Logintoken %s on vanhentunut tai sitä ei löydy", loginToken));
            return this.ophProperties.url("henkilo-ui.sahkopostivarmistus.virhe", kielisyys, "TOKEN_VANHENTUNUT_EI_LOYDY");
        }

        if(tunnistusToken.getKaytetty() != null) {
            log.error(String.format("Logintoken %s on jo käytetty", loginToken));
            return this.ophProperties.url("henkilo-ui.sahkopostivarmistus.virhe", kielisyys, "TOKEN_KAYTETTY");
        }

        Henkilo henkilo = tunnistusToken.getHenkilo();

        oppijanumerorekisteriClient.updateHenkilo(henkiloUpdateDto);
        henkilo.setSahkopostivarmennusAikaleima(LocalDateTime.now());

        String authToken = identificationService.consumeLoginToken(tunnistusToken.getLoginToken(), CAS_AUTHENTICATION_IDP);
        Map<String, Object> redirectMapping = this.redirectMapping(authToken, ophProperties.url("virkailijan-tyopoyta"));
        return ophProperties.url("cas.login", redirectMapping);
    }

    private Map<String, Object> redirectMapping(String authToken, String service) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("authToken", authToken);
        map.put("service", service);
        return map;
    }

    @Override
    @Transactional
    public String redirectUrlByLoginToken(String loginToken, String kielisyys) {
        TunnistusToken tunnistusToken = tunnistusTokenDataRepository.findByValidLoginToken(loginToken)
                .orElseThrow(() -> new NotFoundException(String.format("Login tokenia %s ei löydy tai se on vanhentunut", loginToken)));
        if(tunnistusToken.getKaytetty() != null){
            throw new LoginTokenException(String.format("Login token %s on jo käytetty", loginToken));
        }
        String authToken = identificationService.consumeLoginToken(tunnistusToken.getLoginToken(), CAS_AUTHENTICATION_IDP);
        Map<String, Object> redirectMapping = this.redirectMapping(authToken, ophProperties.url("virkailijan-tyopoyta"));
        return ophProperties.url("cas.login", redirectMapping);
    }

    @Override
    @Transactional(readOnly = true)
    public HenkiloDto getHenkiloByLoginToken(String loginToken) {
        TunnistusToken tunnistusToken = tunnistusTokenDataRepository.findByLoginToken(loginToken)
                .orElseThrow(() -> new NotFoundException(String.format("Login tokenia %s ei löytynyt", loginToken)));
        String oid = tunnistusToken.getHenkilo().getOidHenkilo();
        return this.oppijanumerorekisteriClient.getHenkiloByOid(oid);
    }


}
