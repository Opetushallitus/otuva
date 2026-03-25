package fi.vm.sade.kayttooikeus.service.it;

import static fi.vm.sade.kayttooikeus.repositories.populate.HenkiloPopulator.henkilo;
import static fi.vm.sade.kayttooikeus.repositories.populate.PalveluPopulator.palvelu;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.kayttooikeus.config.security.OpintopolkuUserDetailsService;
import fi.vm.sade.kayttooikeus.dto.GrantKayttooikeusryhmaDto;
import fi.vm.sade.kayttooikeus.dto.KayttoOikeusRyhmaModifyDto;
import fi.vm.sade.kayttooikeus.dto.PalveluRooliModifyDto;
import fi.vm.sade.kayttooikeus.dto.TextGroupDto;
import fi.vm.sade.kayttooikeus.dto.enumeration.OrganisaatioStatus;
import fi.vm.sade.kayttooikeus.model.KayttoOikeus;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioPerustieto;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@Sql("/truncate_tables.sql")
@AutoConfigureMockMvc
public class PermissionCheckerRyhmaRestrictionTest extends AbstractServiceIntegrationTest {
  @Test
  void oikeuksienMyontaminenKunKayttooikeusryhmanMyontaminenRyhmalleOnSallittu() throws Exception {
    List<GrantKayttooikeusryhmaDto> body =
        List.of(
            GrantKayttooikeusryhmaDto.builder()
                .id(valintojenKatselijaId)
                .alkupvm(LocalDate.of(2026, 1, 1))
                .loppupvm(LocalDate.of(2027, 1, 1))
                .build());

    mockMvc
        .perform(
            put("/kayttooikeusanomus/{oid}/{orgOid}", VALINTOJEN_KATSELIJA_OID, RYHMA_OID)
                .with(user(VIRKAILIJA_PAAKAYTTAJA_KK))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
        .andExpect(status().isOk());
  }

  @Test
  void oikeuksienMyontaminenKunKayttooikeusryhmanMyontaminenRyhmalleEiOleSallittu() throws Exception {
    updateKayttoOikeusRyhma(
        valintojenKatselijaId, "Valintojen katselija (kk)", List.of(), false, List.of());

    List<GrantKayttooikeusryhmaDto> body =
        List.of(
            GrantKayttooikeusryhmaDto.builder()
                .id(valintojenKatselijaId)
                .alkupvm(LocalDate.now())
                .loppupvm(LocalDate.now().plusYears(1))
                .build());

    mockMvc
        .perform(
            put("/kayttooikeusanomus/{oid}/{orgOid}", VALINTOJEN_KATSELIJA_OID, RYHMA_OID)
                .with(user(VIRKAILIJA_PAAKAYTTAJA_KK))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
        .andExpect(status().isForbidden());
  }

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private OrganisaatioClient organisaatioClient;

  private Long valintojenKatselijaId;
  private Long kehittajaId;

  @BeforeEach
  void setupTestData() throws Exception {
    populate(henkilo(OPH_REKISTERINPITAJA_OID));
    populate(henkilo(PAAKAYTTAJA_OID));
    populate(henkilo(VALINTOJEN_KATSELIJA_OID));
    populate(palvelu("KAYTTOOIKEUS"));

    when(organisaatioClient.listWithParentsAndChildren(eq(RYHMA_OID), any()))
        .thenReturn(Collections.emptyList());
    when(organisaatioClient.listWithChildOids(eq(RYHMA_OID), any())).thenReturn(Set.of(RYHMA_OID));
    when(organisaatioClient.getOrganisaatioPerustiedotCached(eq(RYHMA_OID)))
        .thenReturn(
            Optional.of(
                OrganisaatioPerustieto.builder()
                    .oid(RYHMA_OID)
                    .status(OrganisaatioStatus.AKTIIVINEN)
                    .build()));

    valintojenKatselijaId = createKayttoOikeusRyhma("Valintojen katselija (kk)", List.of());
    kehittajaId = createKayttoOikeusRyhma("Yleiskäyttöisten Palveluiden kehittäjä", List.of());

    var paakayttajaId =
        createKayttoOikeusRyhma(
            "Pääkäyttäjä (kk)",
            List.of(
                PalveluRooliModifyDto.builder().palveluName("KAYTTOOIKEUS").rooli("CRUD").build()));

    updateKayttoOikeusRyhma(
        valintojenKatselijaId, "Valintojen katselija (kk)", List.of(), true, List.of());

    updateKayttoOikeusRyhma(
        paakayttajaId,
        "Pääkäyttäjä (kk)",
        List.of(PalveluRooliModifyDto.builder().palveluName("KAYTTOOIKEUS").rooli("CRUD").build()),
        false,
        List.of(valintojenKatselijaId, kehittajaId));

    grantKayttoOikeusRyhma(PAAKAYTTAJA_OID, RYHMA_OID, paakayttajaId);
  }

  private Long createKayttoOikeusRyhma(String nameFi, List<PalveluRooliModifyDto> palvelutRoolit)
      throws Exception {
    var dto =
        KayttoOikeusRyhmaModifyDto.builder()
            .nimi(new TextGroupDto().put("FI", nameFi).put("SV", nameFi).put("EN", nameFi))
            .palvelutRoolit(palvelutRoolit)
            .ryhmaRestriction(false)
            .build();
    var result =
        mockMvc
            .perform(
                post("/kayttooikeusryhma")
                    .with(user(OPH_REKISTERINPITAJA))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andReturn();
    return Long.parseLong(result.getResponse().getContentAsString());
  }

  private void updateKayttoOikeusRyhma(
      Long id,
      String nameFi,
      List<PalveluRooliModifyDto> palvelutRoolit,
      boolean ryhmaRestriction,
      List<Long> slaveIds)
      throws Exception {
    var dto =
        KayttoOikeusRyhmaModifyDto.builder()
            .nimi(new TextGroupDto().put("FI", nameFi).put("SV", nameFi).put("EN", nameFi))
            .palvelutRoolit(palvelutRoolit)
            .ryhmaRestriction(ryhmaRestriction)
            .slaveIds(slaveIds)
            .build();
    mockMvc
        .perform(
            put("/kayttooikeusryhma/{id}", id)
                .with(user(OPH_REKISTERINPITAJA))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isOk());
  }

  private void grantKayttoOikeusRyhma(
      String henkiloOid, String organisaatioOid, Long kayttoOikeusRyhmaId) throws Exception {
    var body =
        List.of(
            GrantKayttooikeusryhmaDto.builder()
                .id(kayttoOikeusRyhmaId)
                .alkupvm(LocalDate.of(2026, 1, 1))
                .loppupvm(LocalDate.of(2099, 12, 31))
                .build());
    mockMvc
        .perform(
            put("/kayttooikeusanomus/{oid}/{orgOid}", henkiloOid, organisaatioOid)
                .with(user(OPH_REKISTERINPITAJA))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
        .andExpect(status().isOk());
  }

  private static final String OPH_REKISTERINPITAJA_OID = "1.2.246.562.24.43581349552";
  private static final String PAAKAYTTAJA_OID = "1.2.246.562.24.82664122759";
  private static final String VALINTOJEN_KATSELIJA_OID = "1.2.246.562.24.77272107242";
  private static final String RYHMA_OID = "1.2.246.562.28.30003624829";

  private final OpintopolkuUserDetailsService.OpintopolkuUserDetailsl VIRKAILIJA_PAAKAYTTAJA_KK =
      new OpintopolkuUserDetailsService.OpintopolkuUserDetailsl(
          PAAKAYTTAJA_OID,
          List.of("ROLE_APP_KAYTTOOIKEUS_CRUD", "ROLE_APP_KAYTTOOIKEUS_CRUD_" + RYHMA_OID));

  private final OpintopolkuUserDetailsService.OpintopolkuUserDetailsl OPH_REKISTERINPITAJA =
      new OpintopolkuUserDetailsService.OpintopolkuUserDetailsl(
          OPH_REKISTERINPITAJA_OID,
          List.of(
              "ROLE_APP_KAYTTOOIKEUS_CRUD",
              "ROLE_APP_KAYTTOOIKEUS_CRUD_1.2.246.562.10.00000000001",
              "ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA",
              "ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA_1.2.246.562.10.00000000001"));
}
