package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.dto.VahvaTunnistusRequestDto;
import fi.vm.sade.kayttooikeus.dto.VahvaTunnistusResponseDto;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.TunnistusToken;
import fi.vm.sade.kayttooikeus.service.IdentificationService;
import fi.vm.sade.kayttooikeus.service.KayttajatiedotService;
import fi.vm.sade.kayttooikeus.service.VahvaTunnistusService;
import fi.vm.sade.kayttooikeus.service.dto.HenkiloVahvaTunnistusDto;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.kayttooikeus.util.YhteystietoUtil;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

import static fi.vm.sade.kayttooikeus.model.Identification.STRONG_AUTHENTICATION_IDP;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class VahvaTunnistusServiceImpl implements VahvaTunnistusService {
    private final IdentificationService identificationService;
    private final KayttajatiedotService kayttajatiedotService;
    private final OppijanumerorekisteriClient oppijanumerorekisteriClient;

    @Value("${url-virkailija}")
    private String urlVirkailija;
    @Value("${virkailijan-tyopoyta}")
    private String virkailijanTyopoytaUrl;
    @Value("${cas.login}")
    private String casLogin;

    @Override
    public VahvaTunnistusResponseDto tunnistaudu(String loginToken, VahvaTunnistusRequestDto lisatiedotDto) {
        TunnistusToken tunnistusToken = identificationService.getByValidLoginToken(loginToken);
        return tunnistaudu(tunnistusToken, lisatiedotDto);
    }

    private VahvaTunnistusResponseDto tunnistaudu(TunnistusToken tunnistusToken, VahvaTunnistusRequestDto lisatiedotDto) {
        Henkilo henkiloByLoginToken = tunnistusToken.getHenkilo();
        String henkiloOid = henkiloByLoginToken.getOidHenkilo();

        HenkiloVahvaTunnistusDto vahvaTunnistusDto = new HenkiloVahvaTunnistusDto(tunnistusToken.getHetu());
        Optional.ofNullable(lisatiedotDto.getTyosahkopostiosoite())
                .filter(StringUtils::hasLength)
                .ifPresent(vahvaTunnistusDto::setTyosahkopostiosoite);
        oppijanumerorekisteriClient.setStrongIdentifiedHetu(henkiloOid, vahvaTunnistusDto);

        Optional.ofNullable(lisatiedotDto.getSalasana())
                .filter(StringUtils::hasLength)
                .ifPresent(salasana -> {
                    kayttajatiedotService.throwIfOldPassword(henkiloOid,    salasana);
                    kayttajatiedotService.changePasswordAsAdmin(henkiloOid, salasana);
                });

        String authToken = identificationService.consumeLoginToken(tunnistusToken.getLoginToken(), STRONG_AUTHENTICATION_IDP);
        henkiloByLoginToken.setVahvastiTunnistettu(true);

        return VahvaTunnistusResponseDto.builder()
                .authToken(authToken)
                .service(virkailijanTyopoytaUrl)
                .build();
    }

    @Override
    public String kasitteleKutsunTunnistus(String kutsuToken, String kielisyys, String hetu, String etunimet, String sukunimi) {
        return identificationService.updateKutsuAndGenerateTemporaryKutsuToken(kutsuToken, hetu, etunimet, sukunimi)
                .map(temporaryKutsuToken -> urlVirkailija + "/henkilo-ui/kayttaja/rekisteroidy?temporaryKutsuToken=" + temporaryKutsuToken)
                .orElseGet(() -> getVahvatunnistusVirheUrl(kielisyys, "vanhakutsu"));
    }

    @Override
    public String kirjaaVahvaTunnistus(String loginToken, String kielisyys, String hetu) {
        // otetaan hetu talteen jotta se on vielä tiedossa seuraavassa vaiheessa
        return identificationService.updateLoginToken(loginToken, hetu)
                .map(tunnistusToken -> kirjaaVahvaTunnistus(tunnistusToken, kielisyys, hetu))
                .orElseGet(() -> getVahvatunnistusVirheUrl(kielisyys, "vanha"));
    }

    private String kirjaaVahvaTunnistus(TunnistusToken tunnistusToken, String kielisyys, String hetu) {
        HenkiloDto henkiloByLoginToken = oppijanumerorekisteriClient.getHenkiloByOid(tunnistusToken.getHenkilo().getOidHenkilo());
        if (tunnistusToken.getHenkilo().isPalvelu()) {
            log.error("Palvelukäyttäjänä kirjautuminen on estetty");
            return getVahvatunnistusVirheUrl(kielisyys, "palvelukayttaja");
        }

        if (tunnistusToken.getHenkilo().getKayttajatiedot() == null) {
            log.warn("Käyttäjältä {} puuttuu käyttäjätunnus, uudelleenrekisteröinti estetty", tunnistusToken.getHenkilo().getOidHenkilo());
            return getVahvatunnistusVirheUrl(kielisyys, "eivirkailija");
        }

        // tarkistetaan että virkailijalla on tämä hetu käytössä
        if (StringUtils.hasLength(henkiloByLoginToken.getHetu()) && !henkiloByLoginToken.getHetu().equals(hetu)) {
            log.error(String.format("Vahvan tunnistuksen henkilötunnus %s on eri kuin virkailijan henkilötunnus %s", hetu, henkiloByLoginToken.getHetu()));
            return getVahvatunnistusVirheUrl(kielisyys, "vaara");
        }

        return getRedirectUrl(tunnistusToken.getLoginToken(), kielisyys, tunnistusToken.getSalasananVaihto(), henkiloByLoginToken);
    }

    private String getRedirectUrl(String loginToken, String kielisyys, Boolean salasananVaihto, HenkiloDto henkiloByLoginToken) {
        boolean sahkopostinAsetus = !YhteystietoUtil.getWorkEmail(henkiloByLoginToken.getYhteystiedotRyhma()).isPresent();
        boolean salasananVaihtoBool = Boolean.TRUE.equals(salasananVaihto);
        // pyydetään käyttäjää täydentämään tietoja ("uudelleenrekisteröinti")
        if (sahkopostinAsetus || salasananVaihtoBool) {
            return urlVirkailija + "/henkilo-ui/kayttaja/uudelleenrekisterointi/" + kielisyys + "/" + loginToken + "/" + sahkopostinAsetus + "/" + salasananVaihtoBool;
        }
        // jos mitään tietoja ei tarvitse täyttää, suoritetaan tunnistautuminen ilman rekisteröintisivua
        VahvaTunnistusRequestDto vahvaTunnistusRequestDto = new VahvaTunnistusRequestDto();
        VahvaTunnistusResponseDto vahvaTunnistusResponseDto = this.tunnistaudu(loginToken, vahvaTunnistusRequestDto);
        return UriComponentsBuilder.fromUriString(casLogin)
                .queryParam("service", vahvaTunnistusResponseDto.getService())
                .queryParam("authToken", vahvaTunnistusResponseDto.getAuthToken())
                .build()
                .toUriString();
    }

    private String getVahvatunnistusVirheUrl(String kielisyys, String reason) {
        return urlVirkailija + "/henkilo-ui/kayttaja/vahvatunnistusinfo/virhe/" + kielisyys + "/" + reason;
    }

}
