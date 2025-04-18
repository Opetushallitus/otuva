package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioPerustieto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrganisaatioServiceImplTest {

    private OrganisaatioServiceImpl organisaatioServiceImpl;

    @Mock
    private OrganisaatioClient organisaatioClientMock;

    @BeforeEach
    public void setup() {
        CommonProperties commonProperties = new CommonProperties();
        commonProperties.setRootOrganizationOid("1.2.246.562.10.00000000001");
        organisaatioServiceImpl = new OrganisaatioServiceImpl(organisaatioClientMock);
    }

    @Test
    public void updateOrganisaatioCache() {
        when(organisaatioClientMock.refreshCache()).thenReturn(3L);

        organisaatioServiceImpl.updateOrganisaatioCache();

        verify(organisaatioClientMock).refreshCache();
    }

    @Test
    public void getOrganisaatioNames() {
        OrganisaatioPerustieto vaka1 = OrganisaatioPerustieto.builder()
                .oid("org.vaka1.oid")
                .organisaatiotyypit(List.of("organisaatiotyyppi_08"))
                .nimi(Collections.singletonMap("lang", "vaka1"))
                .build();
        OrganisaatioPerustieto vaka2 = OrganisaatioPerustieto.builder()
                .oid("org.vaka2.oid")
                .organisaatiotyypit(List.of("organisaatiotyyppi_08"))
                .nimi(Collections.singletonMap("lang", "vaka2"))
                .children(List.of())
                .build();
        OrganisaatioPerustieto org = OrganisaatioPerustieto.builder()
                .oid("org.10.oid")
                .organisaatiotyypit(List.of("organisaatiotyyppi_03"))
                .nimi(Collections.singletonMap("lang", "name"))
                .children(List.of(vaka1, vaka2))
                .build();
        OrganisaatioPerustieto group = OrganisaatioPerustieto.builder()
                .oid("group.28.oid")
                .organisaatiotyypit(List.of("Ryhma"))
                .nimi(Collections.singletonMap("groups", "are filtered from output"))
                .build();

        when(organisaatioClientMock.stream()).thenReturn(Stream.of(org, vaka1, vaka2, group));

        Map<String, Map<String, String>> result = organisaatioServiceImpl.getOrganisationNames();

        assertThat(result).hasSize(1).containsKey("org.10.oid");
        assertThat(result.get("org.10.oid")).containsEntry("lang", "name");
    }
}
