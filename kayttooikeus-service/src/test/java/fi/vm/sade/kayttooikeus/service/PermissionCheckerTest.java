package fi.vm.sade.kayttooikeus.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import fi.vm.sade.generic.rest.CachingRestClient;
import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.dto.permissioncheck.ExternalPermissionService;
import fi.vm.sade.kayttooikeus.dto.permissioncheck.PermissionCheckRequestDto;
import fi.vm.sade.kayttooikeus.dto.permissioncheck.PermissionCheckResponseDto;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.OrganisaatioHenkilo;
import fi.vm.sade.kayttooikeus.model.OrganisaatioViite;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRyhmaMyontoViiteRepository;
import fi.vm.sade.kayttooikeus.repositories.MyonnettyKayttoOikeusRyhmaTapahtumaDataRepository;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioPerustieto;
import fi.vm.sade.kayttooikeus.service.impl.PermissionCheckerServiceImpl;
import fi.vm.sade.kayttooikeus.util.CreateUtil;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloDto;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloTyyppi;
import fi.vm.sade.properties.OphProperties;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import org.springframework.test.util.ReflectionTestUtils;


@RunWith(SpringRunner.class)
public class PermissionCheckerTest {

    private PermissionCheckerService permissionChecker;

    @Mock
    private BasicHttpEntity entity;

    private HenkiloDataRepository henkiloDataRepositoryMock;

    private MyonnettyKayttoOikeusRyhmaTapahtumaDataRepository myonnettyKayttoOikeusRyhmaTapahtumaDataRepository;

    private KayttoOikeusRyhmaMyontoViiteRepository kayttoOikeusRyhmaMyontoViiteRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    private FakeRestClient fakeRestClient = new FakeRestClient();

    @Mock
    private OppijanumerorekisteriClient oppijanumerorekisteriClient;

    private OrganisaatioClient organisaatioClient;

    private Set<String> myRoles;

    private static final String ORG1 = "org1";
    private static final String ORG2 = "org2";

    @Before
    public void setup() {
        this.myRoles = createMockedRoles(new HashSet<>());

        this.henkiloDataRepositoryMock = Mockito.mock(HenkiloDataRepository.class);
        this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository = mock(MyonnettyKayttoOikeusRyhmaTapahtumaDataRepository.class);
        this.kayttoOikeusRyhmaMyontoViiteRepository = mock(KayttoOikeusRyhmaMyontoViiteRepository.class);

        CommonProperties commonProperties = new CommonProperties();

        this.organisaatioClient = Mockito.mock(OrganisaatioClient.class);
        OphProperties ophPropertiesMock = Mockito.mock(OphProperties.class);
        when(ophPropertiesMock.url(anyString())).thenReturn("fakeurl");

        this.permissionChecker = spy(new PermissionCheckerServiceImpl(ophPropertiesMock,
                this.henkiloDataRepositoryMock, organisaatioClient, this.oppijanumerorekisteriClient,
                this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository, this.kayttoOikeusRyhmaMyontoViiteRepository,
                commonProperties));
        ReflectionTestUtils.setField(permissionChecker, "restClient", this.fakeRestClient);
        when(this.oppijanumerorekisteriClient.getAllOidsForSamePerson(Matchers.anyString())).thenReturn(
                Sets.newHashSet("masterOid", "slaveOid1", "slaveOid2")
        );
    }

    private Set<String> createMockedRoles(Set<String> additionalRoles) {
        Set<String> auths = Sets.newHashSet(
                "ROLE_APP_HENKILONHALLINTA_CRUD",
                "ROLE_APP_HENKILONHALLINTA_CRUD_" + ORG1,
                "ROLE_APP_HENKILONHALLINTA_CRUD_" + ORG2,
                "ROLE_APP_ANOMUSTENHALLINTA_CRUD",
                "ROLE_APP_ANOMUSTENHALLINTA_CRUD_" + ORG1,
                "ROLE_APP_ANOMUSTENHALLINTA_CRUD_" + ORG2
        );

        auths.addAll(additionalRoles);

        return auths;
    }

    @Test
    public void testThatPermissionIsDeniedWhenUserIsNotFound() {
        when(this.henkiloDataRepositoryMock.findByOidHenkilo(anyString())).thenReturn(Optional.empty());
        assertThat(this.permissionChecker.isAllowedToAccessPerson("callingPerson", "testPerson", Lists.newArrayList("CRUD"),
                ExternalPermissionService.HAKU_APP, this.myRoles)).isFalse();
    }

    @Test
    public void testThatSuperuserIsAllowedAccess() {
        this.myRoles = createMockedRoles(Sets.newHashSet("ROLE_APP_HENKILONHALLINTA_OPHREKISTERI"));
        assertThat(this.permissionChecker.isAllowedToAccessPerson("callingPerson", "testPerson", Lists.newArrayList("CRUD"),
                ExternalPermissionService.HAKU_APP, this.myRoles)).isTrue();
    }

    @Test
    public void testThatPermissionIsAllowedWhenUserIsNotOppijaAndHasNoOrganization() {
        Optional<Henkilo> henkilo = Optional.of(new Henkilo());
        when(oppijanumerorekisteriClient.getHenkiloByOid(any())).thenReturn(HenkiloDto.builder()
                .henkiloTyyppi(HenkiloTyyppi.VIRKAILIJA)
                .build());
        when(henkiloDataRepositoryMock.findByOidHenkilo("testPerson")).thenReturn(henkilo);
        assertThat(this.permissionChecker.isAllowedToAccessPerson("callingPerson", "testPerson", Lists.newArrayList("CRUD"),
                ExternalPermissionService.HAKU_APP, this.myRoles)).isTrue();
    }

    @Test
    public void testThatPermissionIsAllowedWhenUserBelongsToOrganizationThatLoggedInUserHasAccessTo() {
        Optional<Henkilo> henkilo = Optional.of(new Henkilo(){{
            setOrganisaatioHenkilos(Collections.singleton(new OrganisaatioHenkilo(){{
                setOrganisaatioOid("org1");
            }}));
        }});
        given(this.organisaatioClient.getActiveParentOids(any())).willReturn(Lists.newArrayList("org1", "org2", "org3"));
        Mockito.when(henkiloDataRepositoryMock.findByOidHenkilo("testPerson")).thenReturn(henkilo);
        assertThat(this.permissionChecker.isAllowedToAccessPerson("callingPerson", "testPerson", Lists.newArrayList("CRUD", "READ"),
                null, this.myRoles)).isTrue();
    }

    @Test
    public void testThatPermissionIsDeniedWhenUserDoesNotBelongToOrganizationThatLoggedInUserHasAccessTo() {
        Optional<Henkilo> henkilo = Optional.of(new Henkilo(){{
            setOrganisaatioHenkilos(Collections.singleton(new OrganisaatioHenkilo(){{
                setOrganisaatioOid("notCommonOrg1");
            }}));
        }});
        given(this.organisaatioClient.getActiveParentOids(any()))
                .willReturn(Lists.newArrayList("notCommonOrg1", "notCommonOrg2", "notCommonOrg3"));
        Mockito.when(henkiloDataRepositoryMock.findByOidHenkilo("testPerson")).thenReturn(henkilo);
        assertThat(this.permissionChecker.isAllowedToAccessPerson("callingPerson", "testPerson",
                Lists.newArrayList("CRUD", "READ"), null, this.myRoles)).isFalse();
    }

    @Test
    public void testThatPermissionIsDeniedWhenExternalServiceDeniesAccess() throws IOException {
        doReturn(getDummyOrganisaatioHakutulos()).when(this.permissionChecker).listOrganisaatiosByHenkiloOid(anyString());
        this.fakeRestClient.setAllowAccess(false);
        when(this.henkiloDataRepositoryMock.findByOidHenkilo(anyString())).thenReturn(Optional.empty());
        assertThat(this.permissionChecker.isAllowedToAccessPerson("callingPerson", "testPerson", Lists.newArrayList("CRUD"),
                ExternalPermissionService.HAKU_APP, this.myRoles)).isFalse();
    }

    @Test
    public void testThatPermissionIsAllowedWhenExternalServiceAllowsAccess() throws IOException {
        doReturn(getDummyOrganisaatioHakutulos()).when(this.permissionChecker).listOrganisaatiosByHenkiloOid(anyString());
        this.fakeRestClient.setAllowAccess(false);
        when(this.henkiloDataRepositoryMock.findByOidHenkilo(anyString())).thenReturn(Optional.empty());
        this.fakeRestClient.setAllowAccess(true);
        assertThat(this.permissionChecker.isAllowedToAccessPerson("callingPerson", "testPerson",
                Lists.newArrayList("CRUD"), ExternalPermissionService.HAKU_APP, this.myRoles))
                .isTrue();
    }

    @Test
    public void testThatHasRoleForOrganizationReturnsFalseWhenUserNotAssociatedWithOrg() {
        Mockito.when(organisaatioClient.listActiveOrganisaatioPerustiedotByOidRestrictionList(Matchers.anyCollectionOf(String.class)))
                .thenReturn(new ArrayList<>());
        assertThat(permissionChecker.hasRoleForOrganization("orgThatLoggedInUserIsNotAssociatedWith",
                Lists.newArrayList("CRUD", "READ"), this.myRoles))
                .isFalse();
    }

    @Test
    public void testThatHasRoleForOrganizationReturnsTrueWhenUserIsAssociatedWithOrg() {
        Mockito.when(organisaatioClient.listActiveOrganisaatioPerustiedotByOidRestrictionList(Matchers.anyCollectionOf(String.class)))
                .thenReturn(getDummyOrganisaatioHakutulos());
        assertThat(permissionChecker.hasRoleForOrganization("org1", Lists.newArrayList("CRUD", "READ"), this.myRoles)).isTrue();
    }

    @Test
    public void kayttooikeusMyontoviiteLimitationCheckNoKayttajaryhmas() {
        doReturn("1.2.3.4.5").when(this.permissionChecker).getCurrentUserOid();
        doReturn(false).when(this.permissionChecker).isCurrentUserAdmin();

        assertThat(this.permissionChecker.kayttooikeusMyontoviiteLimitationCheck(1L)).isFalse();
    }

    @Test
    public void kayttooikeusMyontoviiteLimitationCheckIsAdmin() {
        doReturn("1.2.3.4.5").when(this.permissionChecker).getCurrentUserOid();
        doReturn(true).when(this.permissionChecker).isCurrentUserAdmin();

        assertThat(this.permissionChecker.kayttooikeusMyontoviiteLimitationCheck(1L)).isTrue();
    }

    @Test
    public void kayttooikeusMyontoviiteLimitationCheckCanNotGrantToSameKayttooikeusryhma() {
        doReturn("1.2.3.4.5").when(this.permissionChecker).getCurrentUserOid();
        doReturn(false).when(this.permissionChecker).isCurrentUserAdmin();
        when(this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository.findValidMyonnettyKayttooikeus("1.2.3.4.5"))
                .thenReturn(Lists.newArrayList(CreateUtil.createMyonnettyKayttoOikeusRyhmaTapahtuma(1001L, 2001L)));
        given(this.kayttoOikeusRyhmaMyontoViiteRepository.getSlaveIdsByMasterIds(anyListOf(Long.class))).willReturn(Lists.newArrayList(2002L));

        assertThat(this.permissionChecker.kayttooikeusMyontoviiteLimitationCheck(2001L)).isFalse();
    }

    @Test
    public void kayttooikeusMyontoviiteLimitationCheck() {
        doReturn("1.2.3.4.5").when(this.permissionChecker).getCurrentUserOid();
        doReturn(false).when(this.permissionChecker).isCurrentUserAdmin();
        when(this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository.findValidMyonnettyKayttooikeus("1.2.3.4.5"))
                .thenReturn(Lists.newArrayList(CreateUtil.createMyonnettyKayttoOikeusRyhmaTapahtuma(1001L, 2001L)));
        given(this.kayttoOikeusRyhmaMyontoViiteRepository.getSlaveIdsByMasterIds(anyListOf(Long.class))).willReturn(Lists.newArrayList(2002L));

        assertThat(this.permissionChecker.kayttooikeusMyontoviiteLimitationCheck(2002L)).isTrue();
    }

    @Test
    public void organisaatioLimitationCheckOrganisaatioNoChildrenWrongOid() {
        given(this.organisaatioClient.getOrganisaatioPerustiedotCached(eq("1.2.3.4.5")))
                .willReturn(Optional.of(CreateUtil.createOrganisaatioPerustietoNoChildren("1.2.3.4.5")));
        OrganisaatioViite organisaatioViite = OrganisaatioViite.builder().organisaatioTyyppi("1.2.3.4.1").build();
        boolean hasPermission = this.permissionChecker
                .organisaatioLimitationCheck("1.2.3.4.5", Sets.newHashSet());
        assertThat(hasPermission).isFalse();
    }

    @Test
    public void organisaatioLimitationCheckOrganisaatioNoChildrenCorrectOid() {
        given(this.organisaatioClient.getOrganisaatioPerustiedotCached(eq("1.2.3.4.5")))
                .willReturn(Optional.of(CreateUtil.createOrganisaatioPerustietoNoChildren("1.2.3.4.5")));
        OrganisaatioViite organisaatioViite = OrganisaatioViite.builder().organisaatioTyyppi("1.2.3.4.5").build();
        boolean hasPermission = this.permissionChecker
                .organisaatioLimitationCheck("1.2.3.4.5", Sets.newHashSet(organisaatioViite));
        assertThat(hasPermission).isTrue();
    }

    @Test
    public void organisaatioLimitationCheckOrganisaatioWithChildrenViiteMatchesChildTyyppi() {
        given(this.organisaatioClient.getOrganisaatioPerustiedotCached(eq("1.2.3.4.5")))
                .willReturn(Optional.of(CreateUtil.createOrganisaatioPerustietoWithChild("1.2.3.4.5", "1.2.3.4.6",
                        "oppilaitostyyppi_11#1")));
        OrganisaatioViite organisaatioViite = OrganisaatioViite.builder().organisaatioTyyppi("11").build();
        boolean hasPermission = this.permissionChecker
                .organisaatioLimitationCheck("1.2.3.4.5", Sets.newHashSet(organisaatioViite));
        assertThat(hasPermission).isTrue();
    }

    @Test
    public void organisaatioLimitationCheckOrganisaatioWithChildrenViiteMatchesParentOid() {
        given(this.organisaatioClient.getOrganisaatioPerustiedotCached(eq("1.2.3.4.5")))
                .willReturn(Optional.of(CreateUtil.createOrganisaatioPerustietoWithChild("1.2.3.4.5", "1.2.3.4.6",
                        "oppilaitostyyppi_11#1")));
        OrganisaatioViite organisaatioViite = OrganisaatioViite.builder().organisaatioTyyppi("1.2.3.4.5").build();
        boolean hasPermission = this.permissionChecker
                .organisaatioLimitationCheck("1.2.3.4.5", Sets.newHashSet(organisaatioViite));
        assertThat(hasPermission).isTrue();
    }

    @Test
    public void organisaatioLimitationCheckOrganisaatioWithChildrenViiteNoMatch() {
        given(this.organisaatioClient.getOrganisaatioPerustiedotCached(eq("1.2.3.4.5")))
                .willReturn(Optional.of(CreateUtil.createOrganisaatioPerustietoWithChild("1.2.3.4.5", "1.2.3.4.6",
                        "oppilaitostyyppi_11#1")));
        OrganisaatioViite organisaatioViite = OrganisaatioViite.builder().organisaatioTyyppi("1.2.3.4.1").build();
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

    private static List<OrganisaatioPerustieto> getDummyOrganisaatioHakutulos() {
        OrganisaatioPerustieto org2 = getOrg("org2");
        OrganisaatioPerustieto org2Child1 = getOrg("org2.child1");
        org2Child1.setChildren(Lists.newArrayList(getOrg("org2.child1.child1")));
        org2.setChildren(Lists.newArrayList(org2Child1, getOrg("org2.child2")));

        return Lists.newArrayList(getOrg("org1"), org2);
    }

    private static OrganisaatioPerustieto getOrg(String oid) {
        OrganisaatioPerustieto org = new OrganisaatioPerustieto();
        org.setOid(oid);
        org.setParentOidPath("parent1/parent2/parent3");
        return org;
    }

    class FakeRestClient extends CachingRestClient {

        private boolean allowAccess;

        @Override
        public HttpResponse post(String url, String charset, String json) throws IOException {
            PermissionCheckRequestDto request = objectMapper.readValue(json, PermissionCheckRequestDto.class);

            // Assert that the client request was correct
            assertThat(request.getPersonOidsForSamePerson().contains("masterOid")).isTrue();
            assertThat(request.getPersonOidsForSamePerson().contains("slaveOid1")).isTrue();
            assertThat(request.getPersonOidsForSamePerson().contains("slaveOid2")).isTrue();
            assertThat(5).isEqualTo(request.getOrganisationOids().size());
            assertThat(request.getOrganisationOids().contains("org1")).isTrue();
            assertThat(request.getOrganisationOids().contains("org2")).isTrue();
            assertThat(request.getOrganisationOids().contains("org2.child1")).isTrue();
            assertThat(request.getOrganisationOids().contains("org2.child2")).isTrue();
            assertThat(request.getOrganisationOids().contains("org2.child1.child1")).isTrue();

            HttpResponse httpResponse = new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "OK"));

            PermissionCheckResponseDto response = new PermissionCheckResponseDto();
            response.setAccessAllowed(this.allowAccess);

            when(entity.getContent()).thenReturn(IOUtils.toInputStream(objectMapper.writeValueAsString(response)));
            httpResponse.setEntity(entity);

            return httpResponse;
        }

        public void setAllowAccess(boolean allowAccess) {
            this.allowAccess = allowAccess;
        }

    }

}
