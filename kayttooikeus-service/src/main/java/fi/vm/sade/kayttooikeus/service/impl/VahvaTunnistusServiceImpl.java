package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.dto.VahvaTunnistusLisatiedotDto;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import static fi.vm.sade.kayttooikeus.model.Identification.STRONG_AUTHENTICATION_IDP;
import fi.vm.sade.kayttooikeus.model.TunnistusToken;
import fi.vm.sade.kayttooikeus.service.IdentificationService;
import fi.vm.sade.kayttooikeus.service.KayttajatiedotService;
import fi.vm.sade.kayttooikeus.service.LdapSynchronizationService;
import fi.vm.sade.kayttooikeus.service.VahvaTunnistusService;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloVahvaTunnistusDto;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
@RequiredArgsConstructor
public class VahvaTunnistusServiceImpl implements VahvaTunnistusService {

    private final IdentificationService identificationService;
    private final KayttajatiedotService kayttajatiedotService;

    private final OppijanumerorekisteriClient oppijanumerorekisteriClient;

    @Override
    // TODO: muuta metodi transaktionaaliseksi kun palvelut käyttävät eri kantaa
    @Transactional(propagation = Propagation.NEVER)
    public String tunnistaudu(String loginToken, VahvaTunnistusLisatiedotDto lisatiedotDto) {
        TunnistusToken tunnistusToken = identificationService.consumeLoginToken(loginToken);
        Henkilo henkiloByLoginToken = tunnistusToken.getHenkilo();
        String henkiloOid = henkiloByLoginToken.getOidHenkilo();

        HenkiloVahvaTunnistusDto vahvaTunnistusDto = new HenkiloVahvaTunnistusDto(tunnistusToken.getHetu());
        Optional.ofNullable(lisatiedotDto.getTyosahkopostiosoite())
                .filter(StringUtils::hasLength)
                .ifPresent(vahvaTunnistusDto::setTyosahkopostiosoite);
        oppijanumerorekisteriClient.setStrongIdentifiedHetu(henkiloOid, vahvaTunnistusDto);

        Optional.ofNullable(lisatiedotDto.getSalasana())
                .filter(StringUtils::hasLength)
                .ifPresent(salasana -> kayttajatiedotService.changePasswordAsAdmin(henkiloOid, salasana, LdapSynchronizationService.LdapSynchronizationType.NOW));

        return identificationService.generateAuthTokenForHenkilo(henkiloOid, STRONG_AUTHENTICATION_IDP,
                tunnistusToken.getHenkilo().getKayttajatiedot().getUsername());
    }

}
