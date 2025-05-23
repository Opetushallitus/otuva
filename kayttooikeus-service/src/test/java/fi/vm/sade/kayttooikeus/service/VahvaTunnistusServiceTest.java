package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.KayttajaTyyppi;
import fi.vm.sade.kayttooikeus.dto.VahvaTunnistusRequestDto;
import fi.vm.sade.kayttooikeus.dto.VahvaTunnistusResponseDto;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.Identification;
import fi.vm.sade.kayttooikeus.model.Kayttajatiedot;
import fi.vm.sade.kayttooikeus.model.TunnistusToken;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.IdentificationRepository;
import fi.vm.sade.kayttooikeus.repositories.TunnistusTokenDataRepository;
import fi.vm.sade.kayttooikeus.service.dto.HenkiloVahvaTunnistusDto;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.kayttooikeus.service.it.AbstractServiceIntegrationTest;
import fi.vm.sade.kayttooikeus.util.YhteystietoUtil;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloDto;
import fi.vm.sade.oppijanumerorekisteri.dto.YhteystiedotRyhmaDto;
import fi.vm.sade.oppijanumerorekisteri.dto.YhteystietoDto;
import fi.vm.sade.oppijanumerorekisteri.dto.YhteystietoTyyppi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static fi.vm.sade.kayttooikeus.repositories.populate.HenkiloPopulator.henkilo;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloPopulator.organisaatioHenkilo;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class VahvaTunnistusServiceTest extends AbstractServiceIntegrationTest {

    private static final String TEST_PASWORD = "This_is_example_of_strong_password";

    @Autowired
    private VahvaTunnistusService vahvaTunnistusService;
    @Autowired
    private IdentificationService identificationService;
    @Autowired
    private HenkiloDataRepository henkiloRepository;
    @Autowired
    private TunnistusTokenDataRepository tunnistusTokenRepository;
    @Autowired
    private IdentificationRepository identificationRepository;

    @MockitoBean
    private OppijanumerorekisteriClient oppijanumerorekisteriClient;

    @BeforeEach
    public void setup() {
        when(oppijanumerorekisteriClient.getHenkiloByOid(any()))
                .thenAnswer(invocation -> newHenkiloDto(invocation.getArgument(0)));
    }

    private HenkiloDto newHenkiloDto(String oid) {
        return HenkiloDto.builder()
                .oidHenkilo(oid)
                .kutsumanimi("kutsumanimi")
                .etunimet("etunimet")
                .sukunimi("suknimi")
                .build();
    }

    private Henkilo saveHenkilo(String oid, String kayttajatunnus) {
        Henkilo henkilo = new Henkilo(oid);
        Kayttajatiedot kayttajatiedot = new Kayttajatiedot();
        kayttajatiedot.setUsername(kayttajatunnus);
        kayttajatiedot.setHenkilo(henkilo);
        henkilo.setKayttajatiedot(kayttajatiedot);
        return henkiloRepository.save(henkilo);
    }

    @Test
    public void tunnistaudu() {
        String oid = "oid123";
        String kayttajatunnus = "kayttajatunnus123";
        Henkilo henkilo = saveHenkilo(oid, kayttajatunnus);
        String hetu = "hetu123";
        String tyosahkopostiosoite = "etu.suku@example.com";
        String loginToken = identificationService.createLoginToken(oid, false, null);
        identificationService.updateLoginToken(loginToken, hetu);

        VahvaTunnistusRequestDto requestDto = new VahvaTunnistusRequestDto();
        requestDto.setSalasana(TEST_PASWORD);
        requestDto.setTyosahkopostiosoite(tyosahkopostiosoite);
        VahvaTunnistusResponseDto responseDto = vahvaTunnistusService.tunnistaudu(loginToken, requestDto);

        assertThat(responseDto)
                .extracting(VahvaTunnistusResponseDto::getAuthToken)
                .isNotNull();
        assertThat(tunnistusTokenRepository.findByLoginToken(loginToken))
                .map(TunnistusToken::getKaytetty)
                .isNotEmpty();
        assertThat(identificationRepository.findAll())
                .extracting(Identification::getHenkilo, Identification::getAuthtoken)
                .containsExactly(tuple(henkilo, responseDto.getAuthToken()));
        ArgumentCaptor<HenkiloVahvaTunnistusDto> henkiloVahvaTunnistusDtoCaptor = ArgumentCaptor.forClass(HenkiloVahvaTunnistusDto.class);
        verify(oppijanumerorekisteriClient).setStrongIdentifiedHetu(eq(oid), henkiloVahvaTunnistusDtoCaptor.capture());
        HenkiloVahvaTunnistusDto henkiloVahvaTunnistusDto = henkiloVahvaTunnistusDtoCaptor.getValue();
        assertThat(henkiloVahvaTunnistusDto)
                .extracting(HenkiloVahvaTunnistusDto::getHetu, HenkiloVahvaTunnistusDto::getTyosahkopostiosoite)
                .containsExactly(hetu, tyosahkopostiosoite);
    }

    @Test
    public void tunnistauduIlmanTyosahkopostia() {
        String oid = "oid123";
        String kayttajatunnus = "kayttajatunnus123";
        Henkilo henkilo = saveHenkilo(oid, kayttajatunnus);
        String hetu = "hetu123";
        String tyosahkopostiosoite = "";
        String loginToken = identificationService.createLoginToken(oid, false, null);
        identificationService.updateLoginToken(loginToken, hetu);

        VahvaTunnistusRequestDto requestDto = new VahvaTunnistusRequestDto();
        requestDto.setSalasana(TEST_PASWORD);
        requestDto.setTyosahkopostiosoite(tyosahkopostiosoite);
        VahvaTunnistusResponseDto responseDto = vahvaTunnistusService.tunnistaudu(loginToken, requestDto);

        assertThat(responseDto)
                .extracting(VahvaTunnistusResponseDto::getAuthToken)
                .isNotNull();
        assertThat(tunnistusTokenRepository.findByLoginToken(loginToken))
                .map(TunnistusToken::getKaytetty)
                .isNotEmpty();
        assertThat(identificationRepository.findAll())
                .extracting(Identification::getHenkilo, Identification::getAuthtoken)
                .containsExactly(tuple(henkilo, responseDto.getAuthToken()));
        ArgumentCaptor<HenkiloVahvaTunnistusDto> henkiloVahvaTunnistusDtoCaptor = ArgumentCaptor.forClass(HenkiloVahvaTunnistusDto.class);
        verify(oppijanumerorekisteriClient).setStrongIdentifiedHetu(eq(oid), henkiloVahvaTunnistusDtoCaptor.capture());
        HenkiloVahvaTunnistusDto henkiloVahvaTunnistusDto = henkiloVahvaTunnistusDtoCaptor.getValue();
        assertThat(henkiloVahvaTunnistusDto)
                .extracting(HenkiloVahvaTunnistusDto::getHetu, HenkiloVahvaTunnistusDto::getTyosahkopostiosoite)
                .containsExactly(hetu, null);
    }

    @Test
    public void kirjaaVahvaTunnistusIlmanKayttajatunnusta() {
        String oid = "oid123";
        String hetu = "hetu123";
        populate(henkilo(oid));
        String loginToken = identificationService.createLoginToken(oid, false, null);
        identificationService.updateLoginToken(loginToken, hetu);

        String redirectUrl = vahvaTunnistusService.kirjaaVahvaTunnistus(loginToken, "fi", hetu);

        assertThat(redirectUrl).contains("/henkilo-ui/", "eivirkailija");
    }

    @Test
    public void kirjaaKayttajaVahvallaTunnistuksellaIlmanKayttajatunnusta() {
        String oid = "oid123";
        String hetu = "hetu123";
        populate(organisaatioHenkilo(henkilo(oid).withTyyppi(KayttajaTyyppi.VIRKAILIJA), "organisaatio123"));
        HenkiloDto henkiloDto = new HenkiloDto();
        henkiloDto.setOidHenkilo(oid);
        henkiloDto.setHetu(hetu);
        henkiloDto.setYhteystiedotRyhma(singleton(YhteystiedotRyhmaDto.builder()
                .ryhmaKuvaus(YhteystietoUtil.TYOOSOITE)
                .yhteystieto(YhteystietoDto.builder()
                        .yhteystietoTyyppi(YhteystietoTyyppi.YHTEYSTIETO_SAHKOPOSTI)
                        .yhteystietoArvo("oid123@example.com")
                        .build())
                .build()));
        when(oppijanumerorekisteriClient.getHenkiloByHetu(any())).thenReturn(Optional.ofNullable(henkiloDto));

        String redirectUrl = vahvaTunnistusService.kirjaaKayttajaVahvallaTunnistuksella(hetu, "fi");

        assertThat(redirectUrl).contains("/henkilo-ui/", "eivirkailija");
    }

}
