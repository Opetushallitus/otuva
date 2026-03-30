package fi.vm.sade.kayttooikeus.controller;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.vm.sade.kayttooikeus.CasUserAttributes;
import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.dto.Asiointikieli;
import fi.vm.sade.kayttooikeus.dto.KutsuCreateDto;
import fi.vm.sade.kayttooikeus.dto.KutsunTila;
import fi.vm.sade.kayttooikeus.dto.VirkailijaRegistration;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.Kutsu;
import fi.vm.sade.kayttooikeus.dto.KutsuCreateDto.KutsuKayttoOikeusRyhmaCreateDto;
import fi.vm.sade.kayttooikeus.dto.KutsuCreateDto.KutsuOrganisaatioCreateDto;
import fi.vm.sade.kayttooikeus.dto.enumeration.OrganisaatioStatus;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.KayttajatiedotRepository;
import fi.vm.sade.kayttooikeus.repositories.KayttooikeusryhmaDataRepository;
import fi.vm.sade.kayttooikeus.repositories.KutsuRepository;
import fi.vm.sade.kayttooikeus.service.KayttajatiedotService;
import fi.vm.sade.kayttooikeus.service.KutsuService;
import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioPerustieto;
import fi.vm.sade.kayttooikeus.util.YhteystietoUtil;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloCreateDto;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloDto;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloUpdateDto;
import fi.vm.sade.oppijanumerorekisteri.dto.YhteystiedotRyhmaDto;
import fi.vm.sade.oppijanumerorekisteri.dto.YhteystietoTyyppi;
import jakarta.transaction.Transactional;

import static fi.vm.sade.kayttooikeus.dto.KayttajaTyyppi.VIRKAILIJA;
import static fi.vm.sade.kayttooikeus.dto.KutsunTila.AVOIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Sql("/truncate_tables.sql")
@Sql("/test-data.sql")
@SpringBootTest
@AutoConfigureMockMvc
public class CasControllerUnmockedTest {
    @Autowired HenkiloDataRepository henkiloDataRepository;
    @Autowired KutsuService kutsuService;
    @Autowired KutsuRepository kutsuRepository;
    @Autowired KayttajatiedotRepository kayttajatiedotRepository;
    @Autowired KayttajatiedotService kayttajatiedotService;
    @Autowired KayttooikeusryhmaDataRepository kayttooikeusryhmaDataRepository;
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired OrikaBeanMapper orikaMapper;

    @MockitoBean OppijanumerorekisteriClient oppijanumerorekisteriClient;
    @MockitoBean OrganisaatioClient organisaatioClient;
    @MockitoBean PermissionCheckerService permissionCheckerService;

    String oid = "1.2.3.4.4444";
    String hetu = "121212-121A";

    @Test
    @WithMockUser(username = "1.2.246.562.24.37535704268")
    public void registerRequiresRekisterinpitaja() throws Exception {
        mvc.perform(post("/cas/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "token":"asdf",
                            "etunimet":"Etu Nimi",
                            "sukunimi":"Suku",
                            "hetu":"121212-121A"
                        }""")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "1.2.246.562.24.37535704268", authorities = "ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA")
    public void registerRequiresKutsu() throws Exception {
        mvc.perform(post("/cas/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "token":"asdf",
                            "etunimet":"Etu Nimi",
                            "sukunimi":"Suku",
                            "hetu":"121212-121A"
                        }""")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @WithMockUser(username = "1.2.246.562.24.37535704268", authorities = "ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA")
    public void registerWithExistingOnrHenkiloAndExistingVirkailija() throws Exception {
        var token = createKutsu();
        createVirkailija(oid, "111111-111A");

        when(oppijanumerorekisteriClient.getHenkiloByHetu(eq(hetu))).thenReturn(Optional.of(
            HenkiloDto.builder()
                .oidHenkilo(oid)
                .build()
        ));
        var response = mvc.perform(post("/cas/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(VirkailijaRegistration.builder()
                            .token(token)
                            .hetu(hetu)
                            .etunimet("Etu Nimi")
                            .sukunimi("Suku")
                            .build()))
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        CasUserAttributes result = objectMapper.readValue(response, new TypeReference<CasUserAttributes>(){});
        assertEquals("1.2.3.4.4444", result.username());
        assertEquals(VIRKAILIJA, result.kayttajaTyyppi());

        var captor = ArgumentCaptor.forClass(HenkiloUpdateDto.class);
        verify(oppijanumerorekisteriClient).updateHenkilo(captor.capture());
        var henkiloUpdate = captor.getValue();
        assertEquals(oid, henkiloUpdate.getOidHenkilo());
        assertEquals(false, henkiloUpdate.getPassivoitu());
        assertEmail(henkiloUpdate.getYhteystiedotRyhma(), "alkupera6");

        assertKutsu(oid);
    }

    @Test
    @Transactional
    @WithMockUser(username = "1.2.246.562.24.37535704268", authorities = "ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA")
    public void registerWithExistingOnrHenkiloAndNewVirkailija() throws Exception {
        var token = createKutsu();

        when(oppijanumerorekisteriClient.getHenkiloByHetu(eq(hetu))).thenReturn(Optional.of(
            HenkiloDto.builder()
                .oidHenkilo(oid)
                .build()
        ));
        var response = mvc.perform(post("/cas/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(VirkailijaRegistration.builder()
                            .token(token)
                            .hetu(hetu)
                            .etunimet("Etu Nimi")
                            .sukunimi("Suku")
                            .build()))
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        CasUserAttributes result = objectMapper.readValue(response, new TypeReference<CasUserAttributes>(){});
        assertThat(result.username()).matches("user-[a-z0-9]{10}");
        assertEquals(VIRKAILIJA, result.kayttajaTyyppi());

        var captor = ArgumentCaptor.forClass(HenkiloUpdateDto.class);
        verify(oppijanumerorekisteriClient).updateHenkilo(captor.capture());
        var henkiloUpdate = captor.getValue();
        assertEquals(oid, henkiloUpdate.getOidHenkilo());
        assertEquals(false, henkiloUpdate.getPassivoitu());
        assertEmail(henkiloUpdate.getYhteystiedotRyhma(), "alkupera6");

        assertKutsu(oid);
    }

    @Test
    @Transactional
    @WithMockUser(username = "1.2.246.562.24.37535704268", authorities = "ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA")
    public void registerWithNewOnrHenkiloAndNewVirkailija() throws Exception {
        var newOid = "1.2.3.4.123456";
        var token = createKutsu();

        when(oppijanumerorekisteriClient.getHenkiloByHetu(eq(hetu))).thenReturn(Optional.empty());
        when(oppijanumerorekisteriClient.createHenkilo(any())).thenReturn(newOid);
        var response = mvc.perform(post("/cas/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(VirkailijaRegistration.builder()
                            .token(token)
                            .hetu(hetu)
                            .etunimet("Etu Nimi")
                            .sukunimi("Suku")
                            .build()))
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        CasUserAttributes result = objectMapper.readValue(response, new TypeReference<CasUserAttributes>(){});
        assertThat(result.username()).matches("user-[a-z0-9]{10}");
        assertEquals(VIRKAILIJA, result.kayttajaTyyppi());

        var captor = ArgumentCaptor.forClass(HenkiloCreateDto.class);
        verify(oppijanumerorekisteriClient).createHenkilo(captor.capture());
        var henkiloCreate = captor.getValue();
        assertEquals("Etu Nimi", henkiloCreate.getEtunimet());
        assertEquals("Etu", henkiloCreate.getKutsumanimi());
        assertEquals("Suku", henkiloCreate.getSukunimi());
        assertEquals("fi", henkiloCreate.getAsiointiKieli().getKieliKoodi());
        assertEmail(henkiloCreate.getYhteystiedotRyhma(), "alkupera2");

        assertKutsu(newOid);
    }

    private void assertEmail(Set<YhteystiedotRyhmaDto> yhteystiedot, String alkupera) {
        var yhteystietoryhma = yhteystiedot.stream().findFirst().get();
        var sahkoposti = yhteystietoryhma.getYhteystieto().stream().findFirst().get();
        assertEquals("sahko@post.fi", sahkoposti.getYhteystietoArvo());
        assertEquals(YhteystietoTyyppi.YHTEYSTIETO_SAHKOPOSTI, sahkoposti.getYhteystietoTyyppi());
        assertEquals(YhteystietoUtil.TYOOSOITE, yhteystietoryhma.getRyhmaKuvaus());
        assertEquals(alkupera, yhteystietoryhma.getRyhmaAlkuperaTieto());
    }

    private void assertKutsu(String oid) {
        var kutsu = kutsuRepository.findAll().iterator().next();
        assertThat(kutsu.getKaytetty()).isNotNull();
        assertEquals(oid, kutsu.getLuotuHenkiloOid());
        assertEquals(KutsunTila.KAYTETTY, kutsu.getTila());
    }

    private String createKutsu() {
        when(organisaatioClient.getOrganisaatioPerustiedotCached(eq("1.2.3.4.77"))).thenReturn(Optional.of(
            OrganisaatioPerustieto.builder()
                .oid("1.2.3.4.77")
                .status(OrganisaatioStatus.AKTIIVINEN)
                .build()
        ));
        var kayttooikeusryhma = new KutsuKayttoOikeusRyhmaCreateDto();
        kayttooikeusryhma.setId(1l);
        var ryhma = new KutsuOrganisaatioCreateDto();
        ryhma.setOrganisaatioOid("1.2.3.4.77");
        ryhma.setVoimassaLoppuPvm(LocalDate.now().plusYears(1));
        ryhma.setKayttoOikeusRyhmat(Set.of(kayttooikeusryhma));
        var kutsuCreate = KutsuCreateDto.builder()
                .etunimi("Etunimi")
                .sukunimi("Sukunimi")
                .organisaatiot(Set.of(ryhma))
                .asiointikieli(Asiointikieli.fi)
                .kutsujaOid("1.2.3.4.6")
                .sahkoposti("sahko@post.fi")
                .saate("Terve")
                .build();
        var kutsu = orikaMapper.map(kutsuCreate, Kutsu.class);
        var salaisuus = UUID.randomUUID().toString();
        kutsu.setId(null);
        kutsu.setAikaleima(LocalDateTime.now());
        kutsu.setKutsuja("1.2.246.562.24.67357428459");
        kutsu.setSalaisuus(salaisuus);
        kutsu.setTila(AVOIN);
        kutsu.getOrganisaatiot().forEach(kutsuOrganisaatio -> kutsuOrganisaatio.setKutsu(kutsu));
        kutsuRepository.save(kutsu);
        return salaisuus;
    }

    private void createVirkailija(String oid, String hetu) {
        henkiloDataRepository.save(
                    Henkilo.builder()
                        .oidHenkilo(oid)
                        .etunimetCached("Ettu Nimmi")
                        .sukunimiCached("Sukku")
                        .hetuCached(hetu)
                        .kayttajaTyyppi(VIRKAILIJA)
                        .build());
        kayttajatiedotService.createOrUpdateUsername(oid, oid);
    }
}
