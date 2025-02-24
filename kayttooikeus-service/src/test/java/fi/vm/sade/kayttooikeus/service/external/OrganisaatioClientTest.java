package fi.vm.sade.kayttooikeus.service.external;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.Optional;
import java.util.Set;

import static java.util.Collections.singletonList;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrganisaatioClientTest extends AbstractClientTest {
    @Autowired
    private OrganisaatioClient client;

    @Test
    public void getLakkautetutOidsTest() {
        stubFor(get(urlEqualTo("/organisaatio-service/rest/organisaatio/v4/1.2.246.562.10.00000000001/jalkelaiset"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON.getType())
                        .withBody(jsonResource("/organisaatio/organisaatioServiceHaeResponse.json"))));
        stubFor(get(urlEqualTo("/organisaatio-service/rest/organisaatio/v4/1.2.246.562.10.00000000001"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON.getType())
                        .withBody(jsonResource("/organisaatio/organisaatioServiceRootOrganisation.json"))));
        stubFor(get(urlEqualTo("/organisaatio-service/rest/organisaatio/v2/ryhmat?lakkautetut=true&aktiiviset=true"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON.getType())
                        .withBody(jsonResource("/organisaatio/ryhmat.json"))));
        this.client.refreshCache();
        Set<String> lakkautetutOids = this.client.getLakkautetutOids();

        // lakkautetut organisaatiot sisältää sekä ryhmien että organisaatioiden passivoidut
        assertTrue(lakkautetutOids.contains("1.2.246.562.28.32497911273"));
        assertTrue(lakkautetutOids.contains("1.2.246.562.10.123456789"));

        assertFalse(lakkautetutOids.contains("1.2.246.562.10.234567890"));
        assertFalse(lakkautetutOids.contains("1.2.246.562.28.36046890756"));
        assertFalse(lakkautetutOids.contains(("1.2.246.562.10.14175756379")));
        assertTrue(lakkautetutOids.size() == 2);
    }

    @Test
    public void getOrganisaatioPerustiedotCachedRoot() {
        stubFor(get(urlEqualTo("/organisaatio-service/rest/organisaatio/v4/1.2.246.562.10.00000000001/jalkelaiset"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON.getType())
                        .withBody(jsonResource("/organisaatio/organisaatioServiceHaeResponse.json"))));
        stubFor(get(urlEqualTo("/organisaatio-service/rest/organisaatio/v4/1.2.246.562.10.00000000001"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON.getType())
                        .withBody(jsonResource("/organisaatio/organisaatioServiceRootOrganisation.json"))));
        stubFor(get(urlEqualTo("/organisaatio-service/rest/organisaatio/v2/ryhmat?lakkautetut=true&aktiiviset=true"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON.getType())
                        .withBody(jsonResource("/organisaatio/ryhmat.json"))));
        client.refreshCache();

        Optional<OrganisaatioPerustieto> organisaatio = client.getOrganisaatioPerustiedotCached("1.2.246.562.10.00000000001");

        assertThat(organisaatio).hasValueSatisfying(org ->
            assertThat(org)
                    .returns(singletonList("MUU_ORGANISAATIO"), OrganisaatioPerustieto::getOrganisaatiotyypit)
                    .returns(singletonList("MUU_ORGANISAATIO"), OrganisaatioPerustieto::getTyypit));
    }

    @Test
    public void getOrganisaatioPerustiedotCachedNotRoot() {
        stubFor(get(urlEqualTo("/organisaatio-service/rest/organisaatio/v4/1.2.246.562.10.00000000001/jalkelaiset"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON.getType())
                        .withBody(jsonResource("/organisaatio/organisaatioServiceHaeResponse.json"))));
        stubFor(get(urlEqualTo("/organisaatio-service/rest/organisaatio/v4/1.2.246.562.10.00000000001"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON.getType())
                        .withBody(jsonResource("/organisaatio/organisaatioServiceRootOrganisation.json"))));
        stubFor(get(urlEqualTo("/organisaatio-service/rest/organisaatio/v2/ryhmat?lakkautetut=true&aktiiviset=true"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON.getType())
                        .withBody(jsonResource("/organisaatio/ryhmat.json"))));
        client.refreshCache();

        Optional<OrganisaatioPerustieto> organisaatio = client.getOrganisaatioPerustiedotCached("1.2.246.562.10.14175756379");

        assertThat(organisaatio).hasValueSatisfying(org ->
            assertThat(org)
                    .returns(singletonList("KOULUTUSTOIMIJA"), OrganisaatioPerustieto::getOrganisaatiotyypit)
                    .returns(singletonList("KOULUTUSTOIMIJA"), OrganisaatioPerustieto::getTyypit));
    }

}
