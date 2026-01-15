package fi.vm.sade.kayttooikeus.service.it;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import fi.vm.sade.kayttooikeus.controller.PalvelukayttajaController.Jarjestelmatunnus;
import fi.vm.sade.kayttooikeus.controller.PalvelukayttajaController.Oauth2ClientCredential;
import fi.vm.sade.kayttooikeus.dto.PalvelukayttajaCreateDto;
import fi.vm.sade.kayttooikeus.dto.PalvelukayttajaCriteriaDto;
import fi.vm.sade.kayttooikeus.dto.PalvelukayttajaReadDto;
import fi.vm.sade.kayttooikeus.model.Kayttajatiedot;
import fi.vm.sade.kayttooikeus.model.Oauth2Client;
import fi.vm.sade.kayttooikeus.repositories.Oauth2ClientRepository;
import fi.vm.sade.kayttooikeus.repositories.populate.HenkiloPopulator;
import fi.vm.sade.kayttooikeus.service.KayttajatiedotService;
import fi.vm.sade.kayttooikeus.service.PalvelukayttajaService;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static fi.vm.sade.kayttooikeus.repositories.populate.HenkiloPopulator.palvelukayttaja;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloPopulator.organisaatioHenkilo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class PalvelukayttajaServiceTest extends AbstractServiceIntegrationTest {
    @Autowired
    private PalvelukayttajaService palvelukayttajaService;
    @Autowired
    private KayttajatiedotService kayttajatiedotService;
    @Autowired
    private Oauth2ClientRepository oauth2ClientRepository;
    @MockitoBean
    private OrganisaatioClient organisaatioClient;
    @MockitoBean
    private OppijanumerorekisteriClient oppijanumerorekisteriClient;

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_KAYTTOOIKEUS_CRUD")
    public void listWithCriteria() throws Exception {
        when(this.organisaatioClient.getChildOids(eq("1.2.3.4.200")))
                .thenReturn(List.of("1.2.3.4.600"));

        populate(palvelukayttaja("1.2.3.4.500")
                .withNimet("_", "one")
                .withUsername("one"));
        populate(organisaatioHenkilo("1.2.3.4.500", "1.2.3.4.600"));

        populate(palvelukayttaja("1.2.3.4.100")
                .withNimet("_", "two")
                .withUsername("two"));
        populate(organisaatioHenkilo("1.2.3.4.100", "1.2.3.4.200"));

        var requestAll = new PalvelukayttajaCriteriaDto();
        List<PalvelukayttajaReadDto> responseAll = palvelukayttajaService.list(requestAll);
        assertThat(responseAll.size()).isEqualTo(2);

        var requestByName = new PalvelukayttajaCriteriaDto();
        requestByName.setNameQuery("two");
        List<PalvelukayttajaReadDto> responseByName = palvelukayttajaService.list(requestByName);
        assertThat(responseByName.size()).isEqualTo(1);
        assertThat(responseByName.get(0).getKayttajatunnus()).isEqualTo("two");

        var requestByOrganisaatioOid = new PalvelukayttajaCriteriaDto();
        requestByOrganisaatioOid.setOrganisaatioOid("1.2.3.4.200");
        List<PalvelukayttajaReadDto> responseByOrganisaatioOid = palvelukayttajaService.list(requestByOrganisaatioOid);
        assertThat(responseByOrganisaatioOid.size()).isEqualTo(1);
        assertThat(responseByOrganisaatioOid.get(0).getKayttajatunnus()).isEqualTo("two");

        var requestBySubOrganisations = new PalvelukayttajaCriteriaDto();
        requestBySubOrganisations.setOrganisaatioOid("1.2.3.4.200");
        requestBySubOrganisations.setSubOrganisation(true);
        List<PalvelukayttajaReadDto> responseBySubOrganisations = palvelukayttajaService.list(requestBySubOrganisations);
        assertThat(responseBySubOrganisations.size()).isEqualTo(2);
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_KAYTTOOIKEUS_CRUD")
    public void createCreates() throws Exception {
        var create = new PalvelukayttajaCreateDto();
        create.setNimi("Test service-1 ?_");

        when(oppijanumerorekisteriClient.createHenkilo(any()))
                .thenReturn("1.2.3.4.6");

        Jarjestelmatunnus response = palvelukayttajaService.create(create);
        assertThat(response)
                .extracting(Jarjestelmatunnus::oid, Jarjestelmatunnus::nimi, Jarjestelmatunnus::kayttajatunnus, Jarjestelmatunnus::oauth2Credentials)
                .containsExactly("1.2.3.4.6", create.getNimi(), "Testservice-1_", new ArrayList<>());

        var kayttaja = kayttajatiedotService.getKayttajatiedotByOidHenkilo("1.2.3.4.6");
        assertThat(kayttaja.get())
                .extracting(Kayttajatiedot::getUsername, Kayttajatiedot::getPassword)
                .containsExactly("Testservice-1_", null);
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_KAYTTOOIKEUS_CRUD")
    public void createCreatesWithDuplicateKayttajatunnus() throws Exception {
        var create = new PalvelukayttajaCreateDto();
        create.setNimi("Test service-1 ?");

        when(oppijanumerorekisteriClient.createHenkilo(any()))
                .thenReturn("1.2.3.4.6", "1.2.3.4.7");

        Jarjestelmatunnus response = palvelukayttajaService.create(create);
        assertThat(response)
                .extracting(Jarjestelmatunnus::oid, Jarjestelmatunnus::nimi, Jarjestelmatunnus::kayttajatunnus, Jarjestelmatunnus::oauth2Credentials)
                .containsExactly("1.2.3.4.6", create.getNimi(), "Testservice-1", new ArrayList<>());

        Jarjestelmatunnus duplicate = palvelukayttajaService.create(create);
        assertThat(duplicate.kayttajatunnus()).matches("Testservice-1[0-9]{4}");
        assertThat(duplicate)
                .extracting(Jarjestelmatunnus::oid, Jarjestelmatunnus::nimi, Jarjestelmatunnus::oauth2Credentials)
                .containsExactly("1.2.3.4.7", create.getNimi(), new ArrayList<>());

        var kayttaja = kayttajatiedotService.getKayttajatiedotByOidHenkilo("1.2.3.4.7");
        assertThat(kayttaja.get())
                .extracting(Kayttajatiedot::getUsername, Kayttajatiedot::getPassword)
                .containsExactly(duplicate.kayttajatunnus(), null);
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_KAYTTOOIKEUS_CRUD")
    public void createCasPasswordCreatesPassword() throws Exception {
        var create = new PalvelukayttajaCreateDto();
        create.setNimi("Test service-1 ?");
        when(oppijanumerorekisteriClient.createHenkilo(any()))
                .thenReturn("1.2.3.4.6", "1.2.3.4.7");
        Jarjestelmatunnus response = palvelukayttajaService.create(create);
        assertThat(response)
                .extracting(Jarjestelmatunnus::oid, Jarjestelmatunnus::nimi, Jarjestelmatunnus::kayttajatunnus, Jarjestelmatunnus::oauth2Credentials)
                .containsExactly("1.2.3.4.6", create.getNimi(), "Testservice-1", new ArrayList<>());

        String password = palvelukayttajaService.createCasPassword("1.2.3.4.6");
        assertThat(password).hasSizeGreaterThan(32);
        Kayttajatiedot kayttaja = kayttajatiedotService.getByUsernameAndPassword("Testservice-1", password);
        assertThat(kayttaja).isNotNull();
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_KAYTTOOIKEUS_CRUD")
    public void createOauth2ClientSecretCreatesSecret() throws Exception {
        populate(HenkiloPopulator.henkilo("1.2.3.4.5").withNimet("etu", "suku").withUsername("user"));
        var create = new PalvelukayttajaCreateDto();
        create.setNimi("Test service-1 ?");
        when(oppijanumerorekisteriClient.createHenkilo(any()))
                .thenReturn("1.2.3.4.6");
        Jarjestelmatunnus response = palvelukayttajaService.create(create);
        assertThat(response)
                .extracting(Jarjestelmatunnus::oid, Jarjestelmatunnus::nimi, Jarjestelmatunnus::kayttajatunnus, Jarjestelmatunnus::oauth2Credentials)
                .containsExactly("1.2.3.4.6", create.getNimi(), "Testservice-1", new ArrayList<>());

        String secret = palvelukayttajaService.createOauth2ClientSecret("1.2.3.4.6");
        assertThat(secret).hasSizeGreaterThan(32);
        Oauth2Client client = oauth2ClientRepository.findById("Testservice-1").get();
        assertThat(client.getKasittelija().getOidHenkilo()).isEqualTo("1.2.3.4.5");
        assertThat(client.getCreated()).isAfter(LocalDateTime.now().minusMinutes(1));
        assertThat(client.getUpdated()).isAfter(LocalDateTime.now().minusMinutes(1));
        assertThat(client.getUuid()).isNotNull();
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = "ROLE_APP_KAYTTOOIKEUS_CRUD")
    public void getJarjestelmatunnusGetsJarjestelmatunnus() throws Exception {
        populate(HenkiloPopulator.henkilo("1.2.3.4.5").withNimet("etu", "suku").withUsername("user"));
        var create = new PalvelukayttajaCreateDto();
        create.setNimi("Test service-1 ?");
        when(oppijanumerorekisteriClient.createHenkilo(any()))
                .thenReturn("1.2.3.4.6");
        palvelukayttajaService.create(create);
        palvelukayttajaService.createOauth2ClientSecret("1.2.3.4.6");

        Jarjestelmatunnus updated = palvelukayttajaService.getJarjestelmatunnus("1.2.3.4.6");
        assertThat(updated)
                .extracting(Jarjestelmatunnus::oid, Jarjestelmatunnus::kayttajatunnus)
                .containsExactly("1.2.3.4.6", "Testservice-1");
        assertThat(updated.oauth2Credentials()).hasSize(1);
        Oauth2ClientCredential client = updated.oauth2Credentials().get(0);
        assertThat(client.clientId()).isEqualTo("Testservice-1");
        assertThat(client.kasittelija().sukunimi()).isEqualTo("suku");
        assertThat(client.kasittelija().etunimet()).isEqualTo("etu");
        assertThat(client.kasittelija().oid()).isEqualTo("1.2.3.4.5");
        assertThat(client.created()).isAfter(LocalDateTime.now().minusMinutes(1));
        assertThat(client.updated()).isAfter(LocalDateTime.now().minusMinutes(1));
    }
}
