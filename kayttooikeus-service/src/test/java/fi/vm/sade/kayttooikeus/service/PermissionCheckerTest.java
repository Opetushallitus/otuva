package fi.vm.sade.kayttooikeus.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.dto.KayttajaTyyppi;
import fi.vm.sade.kayttooikeus.dto.permissioncheck.ExternalPermissionService;
import fi.vm.sade.kayttooikeus.dto.permissioncheck.PermissionCheckRequestDto;
import fi.vm.sade.kayttooikeus.dto.permissioncheck.PermissionCheckResponseDto;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.OrganisaatioHenkilo;
import fi.vm.sade.kayttooikeus.model.OrganisaatioViite;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRyhmaMyontoViiteRepository;
import fi.vm.sade.kayttooikeus.repositories.KayttooikeusryhmaDataRepository;
import fi.vm.sade.kayttooikeus.service.external.ExternalPermissionClient;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioPerustieto;
import fi.vm.sade.kayttooikeus.service.impl.MyontooikeusServiceImpl;
import fi.vm.sade.kayttooikeus.service.impl.PermissionCheckerServiceImpl;
import fi.vm.sade.kayttooikeus.util.CreateUtil;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.vm.sade.kayttooikeus.service.impl.PermissionCheckerServiceImpl.*;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(SpringExtension.class)
@WithMockUser
public class PermissionCheckerTest {

    private PermissionCheckerService permissionChecker;

    private HenkiloDataRepository henkiloDataRepositoryMock;

    private KayttoOikeusRyhmaMyontoViiteRepository kayttoOikeusRyhmaMyontoViiteRepository;

    private KayttooikeusryhmaDataRepository kayttooikeusryhmaDataRepository;

    @Mock
    private ExternalPermissionClient externalPermissionClient;
    @Mock
    private OppijanumerorekisteriClient oppijanumerorekisteriClient;

    private OrganisaatioClient organisaatioClient;

    @Mock
    private KayttajarooliProvider kayttajarooliProvider;

    private static final String ORG1 = "org1";
    private static final String ORG2 = "org2";
    private static final String ROOT_ORG = "1.2.246.562.10.00000000001";

    @BeforeEach
    public void setup() {

        this.henkiloDataRepositoryMock = Mockito.mock(HenkiloDataRepository.class);
        this.kayttoOikeusRyhmaMyontoViiteRepository = mock(KayttoOikeusRyhmaMyontoViiteRepository.class);
        this.kayttooikeusryhmaDataRepository = mock(KayttooikeusryhmaDataRepository.class);


        CommonProperties commonProperties = new CommonProperties();

        this.organisaatioClient = Mockito.mock(OrganisaatioClient.class);

        this.permissionChecker = spy(new PermissionCheckerServiceImpl(
                new MyontooikeusServiceImpl(kayttoOikeusRyhmaMyontoViiteRepository, organisaatioClient),
                this.henkiloDataRepositoryMock, this.kayttooikeusryhmaDataRepository,
                externalPermissionClient, this.oppijanumerorekisteriClient, organisaatioClient,
                kayttajarooliProvider,
                commonProperties));
        when(this.oppijanumerorekisteriClient.getAllOidsForSamePerson(anyString())).thenReturn(
                Sets.newHashSet("masterOid", "slaveOid1", "slaveOid2")
        );
    }

    @Test
    @WithMockUser(value = "callingPerson", authorities = {"ROLE_APP_KAYTTOOIKEUS_CRUD","ROLE_APP_KAYTTOOIKEUS_CRUD_" + ORG1,
            "ROLE_APP_KAYTTOOIKEUS_CRUD_CRUD_" + ORG2,})
    public void testThatPermissionIsDeniedWhenUserIsNotFound() {
        when(this.henkiloDataRepositoryMock.findByOidHenkilo(anyString())).thenReturn(Optional.empty());
        assertThat(this.permissionChecker.isAllowedToAccessPerson("testPerson", Collections.singletonMap(PALVELU_KAYTTOOIKEUS, Lists.newArrayList("CRUD")),
                ExternalPermissionService.HAKU_APP)).isFalse();
    }

    @Test
    @WithMockUser(value = "callingPerson", authorities = {
        "ROLE_APP_PALVELU1_OIKEUS1",
        "ROLE_APP_PALVELU1_OIKEUS1_" + ORG1,
        "ROLE_APP_PALVELU1_OIKEUS1_" + ORG2,
    })
    public void isAllowedToAccessPersonShouldReturnFalseWhenUserIsNotFound() {
        when(this.henkiloDataRepositoryMock.findByOidHenkilo(anyString())).thenReturn(Optional.empty());
        assertThat(this.permissionChecker.isAllowedToAccessPerson(
                "testPerson",
                singletonMap("PALVELU1", singletonList("OIKEUS1")),
                ExternalPermissionService.HAKU_APP)).isFalse();
    }

    @Test
    @WithMockUser(value = "callingPerson", authorities = PALVELU_KAYTTOOIKEUS_PREFIX + ROLE_REKISTERINPITAJA + "_" + ROOT_ORG)
    public void testThatSuperuserIsAllowedAccess() {
        assertThat(this.permissionChecker.isAllowedToAccessPerson("testPerson", Collections.singletonMap(PALVELU_KAYTTOOIKEUS, Lists.newArrayList("CRUD")),
                ExternalPermissionService.HAKU_APP)).isTrue();
    }

    @Test
    @WithMockUser(value = "callingPerson", authorities = PALVELU_KAYTTOOIKEUS_PREFIX + ROLE_REKISTERINPITAJA + "_" + ROOT_ORG)
    public void testThatRekisterinpitajaIsAllowedAccess() {
        assertThat(this.permissionChecker.isAllowedToAccessPerson("testPerson", Collections.singletonMap(PALVELU_KAYTTOOIKEUS, Lists.newArrayList("CRUD")),
                ExternalPermissionService.HAKU_APP)).isTrue();
    }

    @Test
    @WithMockUser(value = "callingPerson", authorities = PALVELU_KAYTTOOIKEUS_PREFIX + ROLE_REKISTERINPITAJA + "_" + ROOT_ORG)
    public void isAllowedToAccessPersonShouldReturnTrueWhenSuperUser() {
        assertThat(this.permissionChecker.isAllowedToAccessPerson(
                "testPerson",
                singletonMap("PALVELU1", singletonList("OIKEUS1")),
                ExternalPermissionService.HAKU_APP)).isTrue();
    }

    @Test
    @WithMockUser(value = "callingPerson", authorities = PALVELU_KAYTTOOIKEUS_PREFIX + ROLE_REKISTERINPITAJA + "_" + ROOT_ORG)
    public void isAllowedToAccessPersonShouldReturnTrueWhenRekisterinpitaja() {
        assertThat(this.permissionChecker.isAllowedToAccessPerson(
                "testPerson",
                singletonMap("PALVELU1", singletonList("OIKEUS1")),
                ExternalPermissionService.HAKU_APP)).isTrue();
    }

    @Test
    @WithMockUser(value = "callingPerson", authorities = {"ROLE_APP_KAYTTOOIKEUS_CRUD","ROLE_APP_KAYTTOOIKEUS_CRUD_" + ORG1,
            "ROLE_APP_KAYTTOOIKEUS_CRUD_" + ORG2,})
    public void testThatPermissionIsAllowedWhenUserIsNotOppijaAndHasNoOrganization() {
        Optional<Henkilo> henkilo = Optional.of(new Henkilo());
        henkilo.get().setKayttajaTyyppi(KayttajaTyyppi.VIRKAILIJA);
        when(oppijanumerorekisteriClient.getHenkiloByOid(any())).thenReturn(HenkiloDto.builder()
                .build());
        when(henkiloDataRepositoryMock.findByOidHenkilo("testPerson")).thenReturn(henkilo);
        assertThat(this.permissionChecker.isAllowedToAccessPerson("testPerson", Collections.singletonMap(PALVELU_KAYTTOOIKEUS, Lists.newArrayList("CRUD")),
                ExternalPermissionService.HAKU_APP)).isTrue();
    }

    @Test
    @WithMockUser(value = "callingPerson", authorities = {
        "ROLE_APP_PALVELU1_OIKEUS1",
        "ROLE_APP_PALVELU1_OIKEUS1_" + ORG1,
        "ROLE_APP_PALVELU1_OIKEUS1_" + ORG2,
    })
    public void isAllowedToAccessPersonShouldReturnTrueWhenUserIsNotOppijaAndHasNoOrganization() {
        Optional<Henkilo> henkilo = Optional.of(new Henkilo());
        henkilo.get().setKayttajaTyyppi(KayttajaTyyppi.VIRKAILIJA);
        when(oppijanumerorekisteriClient.getHenkiloByOid(any())).thenReturn(HenkiloDto.builder()
                .build());
        when(henkiloDataRepositoryMock.findByOidHenkilo("testPerson")).thenReturn(henkilo);
        assertThat(this.permissionChecker.isAllowedToAccessPerson(
                "testPerson",
                singletonMap("PALVELU1", singletonList("OIKEUS1")),
                ExternalPermissionService.HAKU_APP)).isTrue();
    }

    @Test
    @WithMockUser(value = "callingPerson", authorities = {"ROLE_APP_KAYTTOOIKEUS_CRUD","ROLE_APP_KAYTTOOIKEUS_CRUD_" + ORG1,
            "ROLE_APP_KAYTTOOIKEUS_CRUD_" + ORG2,})
    public void testThatPermissionIsAllowedWhenUserBelongsToOrganizationThatLoggedInUserHasAccessTo() {
        Optional<Henkilo> henkilo = Optional.of(new Henkilo(){{
            setOrganisaatioHenkilos(Collections.singleton(new OrganisaatioHenkilo(){{
                setOrganisaatioOid(ORG1);
            }}));
        }});
        given(this.organisaatioClient.getActiveParentOids(any())).willReturn(Lists.newArrayList(ORG1, ORG2, "org3"));
        Mockito.when(henkiloDataRepositoryMock.findByOidHenkilo("testPerson")).thenReturn(henkilo);
        assertThat(this.permissionChecker.isAllowedToAccessPerson("testPerson", Collections.singletonMap(PALVELU_KAYTTOOIKEUS, Lists.newArrayList("CRUD", "READ")),
                null)).isTrue();
    }

    @Test
    @WithMockUser(value = "callingPerson", authorities = {
        "ROLE_APP_PALVELU1_OIKEUS1",
        "ROLE_APP_PALVELU1_OIKEUS1_" + ORG1,
        "ROLE_APP_PALVELU1_OIKEUS1_" + ORG2,
    })
    public void isAllowedToAccessPersonShouldReturnTrueWhenUserBelongsToOrganizationThatLoggedInUserHasAccessTo() {
        Optional<Henkilo> henkilo = Optional.of(new Henkilo(){{
            setOrganisaatioHenkilos(Collections.singleton(new OrganisaatioHenkilo(){{
                setOrganisaatioOid(ORG1);
            }}));
        }});
        given(this.organisaatioClient.getActiveParentOids(any())).willReturn(Lists.newArrayList(ORG1, ORG2, "org3"));
        Mockito.when(henkiloDataRepositoryMock.findByOidHenkilo("testPerson")).thenReturn(henkilo);
        assertThat(this.permissionChecker.isAllowedToAccessPerson(
                "testPerson",
                singletonMap("PALVELU1", singletonList("OIKEUS1")),
                null)).isTrue();
    }

    @Test
    @WithMockUser(value = "callingPerson", authorities = {"ROLE_APP_KAYTTOOIKEUS_CRUD","ROLE_APP_KAYTTOOIKEUS_CRUD_" + ORG1,
            "ROLE_APP_KAYTTOOIKEUS_CRUD_" + ORG2,})
    public void testThatPermissionIsDeniedWhenUserDoesNotBelongToOrganizationThatLoggedInUserHasAccessTo() {
        Optional<Henkilo> henkilo = Optional.of(new Henkilo(){{
            setOrganisaatioHenkilos(Collections.singleton(new OrganisaatioHenkilo(){{
                setOrganisaatioOid("notCommonOrg1");
            }}));
        }});
        given(this.organisaatioClient.getActiveParentOids(any()))
                .willReturn(Lists.newArrayList("notCommonOrg1", "notCommonOrg2", "notCommonOrg3"));
        Mockito.when(henkiloDataRepositoryMock.findByOidHenkilo("testPerson")).thenReturn(henkilo);
        assertThat(this.permissionChecker.isAllowedToAccessPerson("testPerson",
                Collections.singletonMap(PALVELU_KAYTTOOIKEUS, Lists.newArrayList("CRUD", "READ")), null)).isFalse();
    }

    @Test
    @WithMockUser(value = "callingPerson", authorities = {
        "ROLE_APP_PALVELU1_OIKEUS1",
        "ROLE_APP_PALVELU1_OIKEUS1_" + ORG1,
        "ROLE_APP_PALVELU1_OIKEUS1_" + ORG2,
    })
    public void isAllowedToAccessPersonShouldReturnFalseWhenUserDoesNotBelongToOrganizationThatLoggedInUserHasAccessTo() {
        Optional<Henkilo> henkilo = Optional.of(new Henkilo(){{
            setOrganisaatioHenkilos(Collections.singleton(new OrganisaatioHenkilo(){{
                setOrganisaatioOid("notCommonOrg1");
            }}));
        }});
        given(this.organisaatioClient.getActiveParentOids(any()))
                .willReturn(Lists.newArrayList("notCommonOrg1", "notCommonOrg2", "notCommonOrg3"));
        Mockito.when(henkiloDataRepositoryMock.findByOidHenkilo("testPerson")).thenReturn(henkilo);
        assertThat(this.permissionChecker.isAllowedToAccessPerson(
                "testPerson",
                singletonMap("PALVELU1", singletonList("OIKEUS1")),
                null)).isFalse();
    }

    @Test
    @WithMockUser(value = "callingPerson", authorities = {
        "ROLE_APP_KAYTTOOIKEUS_PALVELUKAYTTAJA_CRUD",
        "ROLE_APP_KAYTTOOIKEUS_PALVELUKAYTTAJA_CRUD_" + ORG1,
        "ROLE_APP_KAYTTOOIKEUS_PALVELUKAYTTAJA_CRUD_" + ORG2,
    })
    public void isAllowedToAccessPersonShouldReturnTrueWhenPalvelukayttajaCrudAndPalvelu() {
        Optional<Henkilo> henkilo = Optional.of(new Henkilo(){{
            setKayttajaTyyppi(KayttajaTyyppi.PALVELU);
            setOrganisaatioHenkilos(Collections.singleton(new OrganisaatioHenkilo(){{
                setOrganisaatioOid(ORG1);
            }}));
        }});
        given(this.organisaatioClient.getActiveParentOids(any())).willReturn(Lists.newArrayList(ORG1, ORG2, "org3"));
        when(oppijanumerorekisteriClient.getHenkiloByOid(any())).thenReturn(HenkiloDto.builder()
                .build());
        when(henkiloDataRepositoryMock.findByOidHenkilo("testPerson")).thenReturn(henkilo);
        assertThat(this.permissionChecker.isAllowedToAccessPerson(
                "testPerson",
                singletonMap("KAYTTOOIKEUS", singletonList("PALVELUKAYTTAJA_CRUD")),
                ExternalPermissionService.HAKU_APP)).isTrue();
    }

    @Test
    @WithMockUser(value = "callingPerson", authorities = {
        "ROLE_APP_KAYTTOOIKEUS_PALVELUKAYTTAJA_CRUD",
        "ROLE_APP_KAYTTOOIKEUS_PALVELUKAYTTAJA_CRUD_" + ORG1,
        "ROLE_APP_KAYTTOOIKEUS_PALVELUKAYTTAJA_CRUD_" + ORG2,
    })
    public void isAllowedToAccessPersonShouldReturnFalseWhenPalvelukayttajaCrudAndVirkailija() {
        Optional<Henkilo> henkilo = Optional.of(new Henkilo(){{
            setKayttajaTyyppi(KayttajaTyyppi.VIRKAILIJA);
            setOrganisaatioHenkilos(Collections.singleton(new OrganisaatioHenkilo(){{
                setOrganisaatioOid(ORG1);
            }}));
        }});
        given(this.organisaatioClient.getActiveParentOids(any())).willReturn(Lists.newArrayList(ORG1, ORG2, "org3"));
        when(oppijanumerorekisteriClient.getHenkiloByOid(any())).thenReturn(HenkiloDto.builder()
                .build());
        when(henkiloDataRepositoryMock.findByOidHenkilo("testPerson")).thenReturn(henkilo);
        assertThat(this.permissionChecker.isAllowedToAccessPerson(
                "testPerson",
                singletonMap("KAYTTOOIKEUS", singletonList("PALVELUKAYTTAJA_CRUD")),
                ExternalPermissionService.HAKU_APP)).isFalse();
    }

    @Test
    @WithMockUser(value = "callingPerson", authorities = {"ROLE_APP_KAYTTOOIKEUS_CRUD","ROLE_APP_KAYTTOOIKEUS_CRUD_" + ORG1,
            "ROLE_APP_KAYTTOOIKEUS_CRUD_" + ORG2,})
    public void testThatPermissionIsDeniedWhenExternalServiceDeniesAccess() {
        Henkilo henkilo = new Henkilo();
        henkilo.setOrganisaatioHenkilos(singleton(OrganisaatioHenkilo.builder().organisaatioOid(ORG1).build()));
        when(henkiloDataRepositoryMock.findByOidHenkilo(eq("callingPerson"))).thenReturn(Optional.of(henkilo));

        Set<String> organisaatioOidsWithChildren = Stream.of(ORG1, ORG2, ORG2 + ".child1", ORG2 + ".child1.child1", ORG2 + ".child2").collect(Collectors.toSet());
        doReturn(organisaatioOidsWithChildren).when(this.organisaatioClient).listWithChildOids(any(), any());
        List<String> organisaatioOidsWithParent = Stream.of(ORG1).collect(Collectors.toList());
        doReturn(organisaatioOidsWithParent).when(this.organisaatioClient).getActiveParentOids(eq(ORG1));

        when(externalPermissionClient.getPermission(any(), any())).thenReturn(PermissionCheckResponseDto.denied());
        when(this.henkiloDataRepositoryMock.findByOidHenkilo(eq("testPerson"))).thenReturn(Optional.empty());
        assertThat(this.permissionChecker.isAllowedToAccessPerson("testPerson", Collections.singletonMap(PALVELU_KAYTTOOIKEUS, Lists.newArrayList("CRUD")),
                ExternalPermissionService.HAKU_APP)).isFalse();
        ArgumentCaptor<PermissionCheckRequestDto> permissionCheckRequestDtoArgumentCaptor = ArgumentCaptor.forClass(PermissionCheckRequestDto.class);
        verify(externalPermissionClient).getPermission(any(), permissionCheckRequestDtoArgumentCaptor.capture());
        PermissionCheckRequestDto permissionCheckRequestDto = permissionCheckRequestDtoArgumentCaptor.getValue();
        assertThat(permissionCheckRequestDto).isNotNull();
        assertThat(permissionCheckRequestDto.getPersonOidsForSamePerson())
                .containsExactlyInAnyOrder("masterOid", "slaveOid1", "slaveOid2");
        assertThat(permissionCheckRequestDto.getOrganisationOids())
                .containsExactlyInAnyOrder(ORG1, ORG2, ORG2 + ".child1", ORG2 + ".child2", ORG2 + ".child1.child1");
    }

    @Test
    @WithMockUser(value = "callingPerson", authorities = {"ROLE_APP_KAYTTOOIKEUS_CRUD","ROLE_APP_KAYTTOOIKEUS_CRUD_" + ORG1,
            "ROLE_APP_KAYTTOOIKEUS_CRUD_" + ORG2,})
    public void testThatPermissionIsAllowedWhenExternalServiceAllowsAccess() throws IOException {
        Henkilo henkilo = new Henkilo();
        henkilo.setOrganisaatioHenkilos(singleton(OrganisaatioHenkilo.builder().organisaatioOid(ORG1).build()));
        when(henkiloDataRepositoryMock.findByOidHenkilo(eq("callingPerson"))).thenReturn(Optional.of(henkilo));

        Set<String> organisaatioOids = Stream.of(ORG1, ORG2, ORG2 + ".child1", ORG2 + ".child1.child1", ORG2 + ".child2").collect(Collectors.toSet());
        doReturn(organisaatioOids).when(this.organisaatioClient).listWithChildOids(any(), any());
        List<String> organisaatioOidsWithParent = Stream.of(ORG1).collect(Collectors.toList());
        doReturn(organisaatioOidsWithParent).when(this.organisaatioClient).getActiveParentOids(eq(ORG1));

        when(externalPermissionClient.getPermission(any(), any())).thenReturn(PermissionCheckResponseDto.denied());
        when(this.henkiloDataRepositoryMock.findByOidHenkilo(eq("testPerson"))).thenReturn(Optional.empty());
        when(externalPermissionClient.getPermission(any(), any())).thenReturn(PermissionCheckResponseDto.allowed());
        assertThat(this.permissionChecker.isAllowedToAccessPerson("testPerson",
                Collections.singletonMap(PALVELU_KAYTTOOIKEUS, Lists.newArrayList("CRUD")), ExternalPermissionService.HAKU_APP))
                .isTrue();
        ArgumentCaptor<PermissionCheckRequestDto> permissionCheckRequestDtoArgumentCaptor = ArgumentCaptor.forClass(PermissionCheckRequestDto.class);
        verify(externalPermissionClient).getPermission(any(), permissionCheckRequestDtoArgumentCaptor.capture());
        PermissionCheckRequestDto permissionCheckRequestDto = permissionCheckRequestDtoArgumentCaptor.getValue();
        assertThat(permissionCheckRequestDto).isNotNull();
        assertThat(permissionCheckRequestDto.getPersonOidsForSamePerson())
                .containsExactlyInAnyOrder("masterOid", "slaveOid1", "slaveOid2");
        assertThat(permissionCheckRequestDto.getOrganisationOids())
                .containsExactlyInAnyOrder(ORG1, ORG2, ORG2 + ".child1", ORG2 + ".child2", ORG2 + ".child1.child1");
    }

    @Test
    @WithMockUser(value = "user", authorities = {
            "ROLE_APP_KAYTTOOIKEUS_READ",
            "ROLE_APP_KAYTTOOIKEUS_READ_" + ORG1,
            "ROLE_APP_KAYTTOOIKEUS_CRUD",
            "ROLE_APP_KAYTTOOIKEUS_CRUD_" + ORG2,
    })
    public void testThatExternalPermissionServiceHasCorrectOrganisaatioOids() throws IOException {
        Henkilo henkilo = new Henkilo();
        henkilo.setOrganisaatioHenkilos(Stream.of(ORG1, ORG2).map(oid -> OrganisaatioHenkilo.builder().organisaatioOid(oid).build()).collect(Collectors.toSet()));
        when(henkiloDataRepositoryMock.findByOidHenkilo(eq("user"))).thenReturn(Optional.of(henkilo));

        doAnswer(invocation -> singleton(invocation.getArgument(0))).when(this.organisaatioClient).listWithChildOids(any(), any());
        doAnswer(invocation -> singletonList(invocation.getArgument(0))).when(this.organisaatioClient).getActiveParentOids(any());

        when(externalPermissionClient.getPermission(any(), any())).thenReturn(PermissionCheckResponseDto.allowed());
        when(henkiloDataRepositoryMock.findByOidHenkilo(eq("person"))).thenReturn(Optional.empty());

        boolean allowedToAccessPerson = permissionChecker.isAllowedToAccessPerson("person", singletonMap(PALVELU_KAYTTOOIKEUS, singletonList("CRUD")), ExternalPermissionService.KOSKI);

        assertThat(allowedToAccessPerson).isTrue();
        ArgumentCaptor<PermissionCheckRequestDto> permissionCheckRequestDtoArgumentCaptor = ArgumentCaptor.forClass(PermissionCheckRequestDto.class);
        verify(externalPermissionClient).getPermission(any(), permissionCheckRequestDtoArgumentCaptor.capture());
        PermissionCheckRequestDto permissionCheckRequestDto = permissionCheckRequestDtoArgumentCaptor.getValue();
        assertThat(permissionCheckRequestDto)
                .returns(singletonList(ORG2), PermissionCheckRequestDto::getOrganisationOids)
                .returns(Stream.of(
                        "ROLE_APP_KAYTTOOIKEUS_READ",
                        "ROLE_APP_KAYTTOOIKEUS_READ_" + ORG1,
                        "ROLE_APP_KAYTTOOIKEUS_CRUD",
                        "ROLE_APP_KAYTTOOIKEUS_CRUD_" + ORG2)
                        .collect(Collectors.toSet()), PermissionCheckRequestDto::getLoggedInUserRoles);
    }

    @Test
    @WithMockUser(value = "callingPerson", authorities = {"ROLE_APP_KAYTTOOIKEUS_CRUD","ROLE_APP_KAYTTOOIKEUS_CRUD_" + ORG1,
            "ROLE_APP_KAYTTOOIKEUS_CRUD_" + ORG2,})
    public void testThatHasRoleForOrganizationReturnsFalseWhenUserNotAssociatedWithOrg() {
        assertThat(permissionChecker.checkRoleForOrganisation(singletonList("orgThatLoggedInUserIsNotAssociatedWith"),
                singletonMap(PALVELU_KAYTTOOIKEUS, Lists.newArrayList("CRUD", "READ"))))
                .isFalse();
    }

    @Test
    @WithMockUser(value = "callingPerson", authorities = {
        "ROLE_APP_PALVELU1_OIKEUS1",
        "ROLE_APP_PALVELU1_OIKEUS1_" + ORG1,
        "ROLE_APP_PALVELU1_OIKEUS1_" + ORG2,
    })
    public void hasRoleForOrganisationShouldReturnFalseWhenUserNotAssociatedWithOrg() {
        assertThat(permissionChecker.checkRoleForOrganisation(singletonList("orgThatLoggedInUserIsNotAssociatedWith"),
                singletonMap("PALVELU1", singletonList("OIKEUS1"))))
                .isFalse();
    }

    @Test
    @WithMockUser(value = "callingPerson", authorities = {"ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA","ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA_" + ORG1,
            "ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA_" + ORG2,})
    public void testThatHasRoleForOrganizationReturnsTrueWhenUserIsAssociatedWithOrg() {
        Mockito.when(organisaatioClient.getActiveParentOids(anyString()))
                .thenReturn(Lists.newArrayList(ORG2, "parent1", "parent2", "parent3"));
        assertThat(permissionChecker.checkRoleForOrganisation(singletonList(ORG1), singletonMap(PALVELU_KAYTTOOIKEUS, Lists.newArrayList("CRUD", "READ", "REKISTERINPITAJA")))).isTrue();
    }

    @Test
    @WithMockUser(value = "callingPerson", authorities = {
        "ROLE_APP_PALVELU1_OIKEUS1",
        "ROLE_APP_PALVELU1_OIKEUS1_" + ORG1,
        "ROLE_APP_PALVELU1_OIKEUS1_" + ORG2,
    })
    public void hasRoleForOrganisationShouldReturnTrueWhenUserIsAssociatedWithOrg() {
        Mockito.when(organisaatioClient.getActiveParentOids(anyString()))
                .thenReturn(Lists.newArrayList(ORG2, "parent1", "parent2", "parent3"));
        assertThat(permissionChecker.checkRoleForOrganisation(singletonList(ORG1), singletonMap("PALVELU1", singletonList("OIKEUS1")))).isTrue();
    }

    @Test
    public void getPrefixedRolesByPalveluRooli() {
        Map<String, List<String>> input1 = Maps.newHashMap();
        input1.put("OPPIJANUMEROREKISTERI", Lists.newArrayList("CRUD", "READ"));

        Set<String> expectedResult1 = Sets.newHashSet("ROLE_APP_OPPIJANUMEROREKISTERI_CRUD",
                "ROLE_APP_OPPIJANUMEROREKISTERI_READ");
        assertThat(PermissionCheckerServiceImpl.getPrefixedRolesByPalveluRooli(input1)).isEqualTo(expectedResult1);

        Map<String, List<String>> input2 = Maps.newHashMap();
        input2.put("TESTIPALVELU", Lists.newArrayList("ABC", "DEF", "ABCSD"));
        input2.put("OIKEAPALVELU", Lists.newArrayList("ABC", "FOOBAR"));

        Set<String> expectedResult2 = Sets.newHashSet("ROLE_APP_TESTIPALVELU_ABC", "ROLE_APP_TESTIPALVELU_DEF",
                "ROLE_APP_TESTIPALVELU_ABCSD", "ROLE_APP_OIKEAPALVELU_ABC", "ROLE_APP_OIKEAPALVELU_FOOBAR");
        assertThat(PermissionCheckerServiceImpl.getPrefixedRolesByPalveluRooli(input2)).isEqualTo(expectedResult2);
    }

    @Test
    public void kayttooikeusMyontoviiteLimitationCheckNoKayttajaryhmas() {
        doReturn("1.2.3.4.5").when(this.permissionChecker).getCurrentUserOid();
        doReturn(false).when(this.permissionChecker).isCurrentUserAdmin();
        assertThat(this.permissionChecker.kayttooikeusMyontoviiteLimitationCheck("organisaatioOid", 1L)).isFalse();
    }

    @Test
    public void kayttooikeusMyontoviiteLimitationCheckIsAdmin() {
        doReturn("1.2.3.4.5").when(this.permissionChecker).getCurrentUserOid();
        doReturn(true).when(this.permissionChecker).isCurrentUserAdmin();

        assertThat(this.permissionChecker.kayttooikeusMyontoviiteLimitationCheck("organisaatioOid", 1L)).isTrue();
    }

    @Test
    public void kayttooikeusMyontoviiteLimitationCheckCanNotGrantToSameKayttooikeusryhma() {
        doReturn("1.2.3.4.5").when(this.permissionChecker).getCurrentUserOid();
        doReturn(false).when(this.permissionChecker).isCurrentUserAdmin();
        Map<String, Set<Long>> myontooikeudet = singletonMap("organisaatioOid", singleton(2002L));
        when(kayttoOikeusRyhmaMyontoViiteRepository.getSlaveIdsByMasterHenkiloOid(any(), any())).thenReturn(myontooikeudet);

        assertThat(this.permissionChecker.kayttooikeusMyontoviiteLimitationCheck("organisaatioOid", 2001L)).isFalse();
    }

    @Test
    public void kayttooikeusMyontoviiteLimitationCheck() {
        doReturn("1.2.3.4.5").when(this.permissionChecker).getCurrentUserOid();
        doReturn(false).when(this.permissionChecker).isCurrentUserAdmin();
        Map<String, Set<Long>> myontooikeudet = singletonMap("organisaatioOid", singleton(2002L));
        when(kayttoOikeusRyhmaMyontoViiteRepository.getSlaveIdsByMasterHenkiloOid(any(), any())).thenReturn(myontooikeudet);

        assertThat(this.permissionChecker.kayttooikeusMyontoviiteLimitationCheck("organisaatioOid", 2002L)).isTrue();
    }

    @Test
    public void organisaatioLimitationCheckOrganisaatioNoChildrenWrongOid() {
        given(this.organisaatioClient.listWithParentsAndChildren(eq("1.2.3.4.5"), any()))
                .willReturn(asList(CreateUtil.createOrganisaatioPerustietoNoChildren("1.2.3.4.5")));
        boolean hasPermission = this.permissionChecker
                .organisaatioLimitationCheck("1.2.3.4.5", Sets.newHashSet());
        assertThat(hasPermission).isFalse();
    }

    @Test
    public void organisaatioLimitationCheckOrganisaatioNoChildrenCorrectOid() {
        given(this.organisaatioClient.listWithParentsAndChildren(eq("1.2.3.4.5"), any()))
                .willReturn(asList(CreateUtil.createOrganisaatioPerustietoNoChildren("1.2.3.4.5")));
        OrganisaatioViite organisaatioViite = OrganisaatioViite.builder().organisaatioTyyppi("1.2.3.4.5").build();
        boolean hasPermission = this.permissionChecker
                .organisaatioLimitationCheck("1.2.3.4.5", Sets.newHashSet(organisaatioViite));
        assertThat(hasPermission).isTrue();
    }

    @Test
    public void organisaatioLimitationCheckOrganisaatioWithChildrenViiteMatchesChildTyyppi() {
        OrganisaatioPerustieto organisaatioPerustietoWithChild = CreateUtil.createOrganisaatioPerustietoWithChild("1.2.3.4.5", "1.2.3.4.6", "oppilaitostyyppi_11#1");
        given(this.organisaatioClient.listWithParentsAndChildren(eq("1.2.3.4.5"), any()))
                .willReturn(Stream.concat(Stream.of(organisaatioPerustietoWithChild), organisaatioPerustietoWithChild.getChildren().stream()).collect(Collectors.toList()));
        OrganisaatioViite organisaatioViite = OrganisaatioViite.builder().organisaatioTyyppi("oppilaitostyyppi_11").build();
        boolean hasPermission = this.permissionChecker
                .organisaatioLimitationCheck("1.2.3.4.5", Sets.newHashSet(organisaatioViite));
        assertThat(hasPermission).isTrue();
    }

    @Test
    public void organisaatioLimitationCheckOrganisaatioWithChildrenViiteMatchesParentOid() {
        given(this.organisaatioClient.listWithParentsAndChildren(eq("1.2.3.4.5"), any()))
                .willReturn(asList(CreateUtil.createOrganisaatioPerustietoWithChild("1.2.3.4.5", "1.2.3.4.6",
                        "oppilaitostyyppi_11#1")));
        OrganisaatioViite organisaatioViite = OrganisaatioViite.builder().organisaatioTyyppi("1.2.3.4.5").build();
        boolean hasPermission = this.permissionChecker
                .organisaatioLimitationCheck("1.2.3.4.5", Sets.newHashSet(organisaatioViite));
        assertThat(hasPermission).isTrue();
    }

    @Test
    public void organisaatioLimitationCheckOrganisaatioWithChildrenViiteNoMatch() {
        given(this.organisaatioClient.listWithParentsAndChildren(eq("1.2.3.4.5"), any()))
                .willReturn(asList(CreateUtil.createOrganisaatioPerustietoWithChild("1.2.3.4.5", "1.2.3.4.6",
                        "oppilaitostyyppi_11#1")));
        OrganisaatioViite organisaatioViite = OrganisaatioViite.builder().organisaatioTyyppi("1.2.3.4.1").build();
        boolean hasPermission = this.permissionChecker
                .organisaatioLimitationCheck("1.2.3.4.5", Sets.newHashSet(organisaatioViite));
        assertThat(hasPermission).isFalse();
    }

    @Test
    public void organisaatioLimitationCheckOrganisaatioWithoutChildrenOppilaitosMatch() {
        OrganisaatioPerustieto organisaatio = CreateUtil.createOrganisaatioPerustietoNoChildren("1.2.3.4.5");
        organisaatio.setOppilaitostyyppi("oppilaitostyyppi_11#1");
        given(this.organisaatioClient.listWithParentsAndChildren(eq("1.2.3.4.5"), any()))
                .willReturn(asList(organisaatio));
        OrganisaatioViite organisaatioViite = OrganisaatioViite.builder().organisaatioTyyppi("oppilaitostyyppi_11").build();
        boolean hasPermission = this.permissionChecker
                .organisaatioLimitationCheck("1.2.3.4.5", Sets.newHashSet(organisaatioViite));
        assertThat(hasPermission).isTrue();
    }

    @Test
    public void organisaatioLimitationCheckOrganisaatioWithChildrenOppilaitosMatch() {
        OrganisaatioPerustieto organisaatio = CreateUtil.createOrganisaatioPerustietoWithChild("1.2.3.4.5", "1.2.3.4.6", "oppilaitostyyppi_12#1");
        organisaatio.setOppilaitostyyppi("oppilaitostyyppi_11#1");
        given(this.organisaatioClient.listWithParentsAndChildren(eq("1.2.3.4.5"), any()))
                .willReturn(asList(organisaatio));
        OrganisaatioViite organisaatioViite = OrganisaatioViite.builder().organisaatioTyyppi("oppilaitostyyppi_11").build();
        boolean hasPermission = this.permissionChecker
                .organisaatioLimitationCheck("1.2.3.4.5", Sets.newHashSet(organisaatioViite));
        assertThat(hasPermission).isTrue();
    }

    @Test
    public void organisaatioLimitationCheckOrganisaatioWithoutChildrenOppilaitosNoMatch() {
        OrganisaatioPerustieto organisaatio = CreateUtil.createOrganisaatioPerustietoNoChildren("1.2.3.4.5");
        organisaatio.setOppilaitostyyppi("oppilaitostyyppi_11#1");
        given(this.organisaatioClient.listWithParentsAndChildren(eq("1.2.3.4.5"), any()))
                .willReturn(asList(organisaatio));
        OrganisaatioViite organisaatioViite = OrganisaatioViite.builder().organisaatioTyyppi("oppilaitostyyppi_12").build();
        boolean hasPermission = this.permissionChecker
                .organisaatioLimitationCheck("1.2.3.4.5", Sets.newHashSet(organisaatioViite));
        assertThat(hasPermission).isFalse();
    }

    @Test
    public void organisaatioLimitationCheckOrganisaatioWithChildrenOppilaitosNoMatch() {
        OrganisaatioPerustieto organisaatio = CreateUtil.createOrganisaatioPerustietoWithChild("1.2.3.4.5", "1.2.3.4.6", "oppilaitostyyppi_12#1");
        organisaatio.setOppilaitostyyppi("oppilaitostyyppi_11#1");
        given(this.organisaatioClient.listWithParentsAndChildren(eq("1.2.3.4.5"), any()))
                .willReturn(asList(organisaatio));
        OrganisaatioViite organisaatioViite = OrganisaatioViite.builder().organisaatioTyyppi("oppilaitostyyppi_13").build();
        boolean hasPermission = this.permissionChecker
                .organisaatioLimitationCheck("1.2.3.4.5", Sets.newHashSet(organisaatioViite));
        assertThat(hasPermission).isFalse();
    }

    @Test
    public void organisaatioLimitationCheckOrganisaatioRyhmaCorrectOid() {
        OrganisaatioViite organisaatioViite = OrganisaatioViite.builder().organisaatioTyyppi("1.2.246.562.28").build();
        boolean hasPermission = this.permissionChecker
                .organisaatioLimitationCheck("1.2.246.562.28.0.0.1", Sets.newHashSet(organisaatioViite));
        assertThat(hasPermission).isTrue();
    }

    @Test
    public void organisaatioLimitationCheckOrganisaatioRyhmaWrongOid() {
        OrganisaatioViite organisaatioViite = OrganisaatioViite.builder().organisaatioTyyppi("1.2.3.4.5").build();
        boolean hasPermission = this.permissionChecker
                .organisaatioLimitationCheck("1.2.246.562.28.0.0.1", Sets.newHashSet(organisaatioViite));
        assertThat(hasPermission).isFalse();
    }

    @Test
    public void organisaatioLimitationCheckCorrentOrganisaatiotyyppi() {
        when(organisaatioClient.listWithParentsAndChildren(any(), any())).thenAnswer(invocation
                -> singletonList(OrganisaatioPerustieto.builder()
                .oid(invocation.getArgument(0))
                .children(emptyList())
                .organisaatiotyypit(asList("organisaatiotyyppi_01", "organisaatiotyyppi_02"))
                .build()));
        OrganisaatioViite organisaatioViite = OrganisaatioViite.builder().organisaatioTyyppi("organisaatiotyyppi_01").build();
        boolean hasPermission = this.permissionChecker
                .organisaatioLimitationCheck("1.2.3.4.5", Sets.newHashSet(organisaatioViite));
        assertThat(hasPermission).isTrue();
    }

    @Test
    public void organisaatioLimitationCheckWrongOrganisaatiotyyppi() {
        when(organisaatioClient.listWithParentsAndChildren(any(), any())).thenAnswer(invocation
                -> singletonList(OrganisaatioPerustieto.builder()
                .oid(invocation.getArgument(0))
                .children(emptyList())
                .organisaatiotyypit(asList("organisaatiotyyppi_01", "organisaatiotyyppi_02"))
                .build()));
        OrganisaatioViite organisaatioViite = OrganisaatioViite.builder().organisaatioTyyppi("organisaatiotyyppi_03").build();
        boolean hasPermission = this.permissionChecker
                .organisaatioLimitationCheck("1.2.3.4.5", Sets.newHashSet(organisaatioViite));
        assertThat(hasPermission).isFalse();
    }

    @Test
    public void organisaatioLimitationCheckChildOrganisaatiotyyppi() {
        OrganisaatioPerustieto aliorganisaatio = OrganisaatioPerustieto.builder()
                .oid("1.2.3.4.6")
                .children(emptyList())
                .organisaatiotyypit(asList("organisaatiotyyppi_01", "organisaatiotyyppi_02"))
                .build();
        when(organisaatioClient.listWithParentsAndChildren(any(), any())).thenAnswer(invocation
                -> asList(OrganisaatioPerustieto.builder()
                .oid(invocation.getArgument(0))
                .children(singletonList(aliorganisaatio))
                .build(), aliorganisaatio));
        OrganisaatioViite organisaatioViite = OrganisaatioViite.builder().organisaatioTyyppi("organisaatiotyyppi_01").build();
        boolean hasPermission = this.permissionChecker
                .organisaatioLimitationCheck("1.2.3.4.5", Sets.newHashSet(organisaatioViite));
        assertThat(hasPermission).isFalse();
    }

    @Test
    public void organisaatioLimitationCheckParentOrganisaatiotyyppi() {
        OrganisaatioPerustieto ylaorganisaatio = OrganisaatioPerustieto.builder()
                .oid("1.2.3.4.6")
                .children(emptyList())
                .organisaatiotyypit(asList("organisaatiotyyppi_01", "organisaatiotyyppi_02"))
                .build();
        when(organisaatioClient.listWithParentsAndChildren(any(), any())).thenAnswer(invocation
                -> asList(OrganisaatioPerustieto.builder()
                .oid(invocation.getArgument(0))
                .children(emptyList())
                .build(), ylaorganisaatio));
        OrganisaatioViite organisaatioViite = OrganisaatioViite.builder().organisaatioTyyppi("organisaatiotyyppi_01").build();
        boolean hasPermission = this.permissionChecker
                .organisaatioLimitationCheck("1.2.3.4.5", Sets.newHashSet(organisaatioViite));
        assertThat(hasPermission).isFalse();
    }

    @Test
    @WithMockUser(authorities = {"ROLE_APP_KAYTTOOIKEUS_READ_UPDATE", "ROLE_APP_KAYTTOOIKEUS_READ_UPDATE_" + ROOT_ORG})
    public void roleWithUnderscoreIsHandledProperly() {
        boolean hasReadUpdate = this.permissionChecker.isCurrentUserMiniAdmin(PALVELU_KAYTTOOIKEUS, "READ_UPDATE");
        boolean hasRead = this.permissionChecker.isCurrentUserMiniAdmin(PALVELU_KAYTTOOIKEUS, "READ");
        boolean hasReadOrUpdate = this.permissionChecker.isCurrentUserMiniAdmin(PALVELU_KAYTTOOIKEUS, "READ", "READ_UPDATE");
        assertThat(hasReadUpdate).isTrue();
        assertThat(hasRead).isFalse();
        assertThat(hasReadOrUpdate).isTrue();
    }

    @Test
    @WithMockUser(authorities = {
        "ROLE_APP_PALVELU1_OIKEUS1",
        "ROLE_APP_PALVELU1_OIKEUS1_" + ROOT_ORG,
    })
    public void getCurrentUserOrgnisationsWithPalveluRole() {
        Set<String> oids = this.permissionChecker.getCurrentUserOrgnisationsWithPalveluRole(singletonMap("PALVELU1", singletonList("OIKEUS1")));
        assertThat(oids).containsExactly(ROOT_ORG);
    }

    @Test
    @WithMockUser(authorities = {
        "ROLE_APP_PALVELU1_OIKEUS1",
        "ROLE_APP_PALVELU1_OIKEUS1_" + ORG1,
        "ROLE_APP_PALVELU1_OIKEUS2_" + ORG2,
        "ROLE_APP_PALVELU2_OIKEUS1_" + ORG2,
    })
    public void hasOrganisaatioInHierarchy() {
        when(organisaatioClient.getActiveParentOids(eq(ORG1))).thenReturn(asList(ROOT_ORG, ORG1));
        when(organisaatioClient.getActiveParentOids(eq(ORG2))).thenReturn(asList(ROOT_ORG, ORG2));

        Set<String> oids = this.permissionChecker.hasOrganisaatioInHierarchy(asList(ORG1, ORG2), singletonMap("PALVELU1", singletonList("OIKEUS1")));

        assertThat(oids).containsExactly(ORG1);
    }

    @Test
    public void hasInsternalAccess() {
        given(this.henkiloDataRepositoryMock.findByOidHenkilo("1.2.3.4.5"))
                .willReturn(Optional.of(Henkilo.builder().oidHenkilo("1.2.3.4.5")
                        .organisaatioHenkilos(Sets.newHashSet(
                                OrganisaatioHenkilo.builder().organisaatioOid("1.2.3.4.100").passivoitu(false).build(),
                                OrganisaatioHenkilo.builder().organisaatioOid("1.2.3.4.200").passivoitu(true).build()))
                        .build()));
        given(this.organisaatioClient.getActiveParentOids("1.2.3.4.100"))
                .willReturn(Lists.newArrayList("1.2.3.4.100"));
        given(this.organisaatioClient.getActiveParentOids("1.2.3.4.200"))
                .willReturn(Lists.newArrayList("1.2.3.4.200"));
        Boolean hasInternalAccess = ReflectionTestUtils.invokeMethod(this.permissionChecker, "hasInternalAccess", "1.2.3.4.5", singletonMap(PALVELU_KAYTTOOIKEUS, singletonList("READ")), new HashSet<>(Arrays.asList("ROLE_APP_KAYTTOOIKEUS_READ", "ROLE_APP_KAYTTOOIKEUS_READ_1.2.3.4.100")));
        assertThat(hasInternalAccess).isTrue();
        verify(this.organisaatioClient, Mockito.times(1)).getActiveParentOids(eq("1.2.3.4.100"));
        hasInternalAccess = ReflectionTestUtils.invokeMethod(this.permissionChecker, "hasInternalAccess", "1.2.3.4.5", singletonMap(PALVELU_KAYTTOOIKEUS, singletonList("READ")), new HashSet<>(Arrays.asList("ROLE_APP_KAYTTOOIKEUS_READ", "ROLE_APP_KAYTTOOIKEUS_READ_READ_1.2.3.4.200")));
        assertThat(hasInternalAccess).isFalse();
        verify(this.organisaatioClient, Mockito.times(2)).getActiveParentOids(eq("1.2.3.4.100"));
    }

}
