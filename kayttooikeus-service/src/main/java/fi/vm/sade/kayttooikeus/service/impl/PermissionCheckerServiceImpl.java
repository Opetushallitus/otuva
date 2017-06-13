package fi.vm.sade.kayttooikeus.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import fi.vm.sade.generic.rest.CachingRestClient;
import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.dto.*;
import fi.vm.sade.kayttooikeus.dto.permissioncheck.ExternalPermissionService;
import fi.vm.sade.kayttooikeus.dto.permissioncheck.PermissionCheckRequestDto;
import fi.vm.sade.kayttooikeus.dto.permissioncheck.PermissionCheckResponseDto;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRyhmaMyontoViiteRepository;
import fi.vm.sade.kayttooikeus.repositories.MyonnettyKayttoOikeusRyhmaTapahtumaDataRepository;
import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioPerustieto;
import fi.vm.sade.properties.OphProperties;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PermissionCheckerServiceImpl extends AbstractService implements PermissionCheckerService {
    private static final Logger LOG = LoggerFactory.getLogger(PermissionCheckerService.class);
    private static CachingRestClient restClient = new CachingRestClient().setClientSubSystemCode("henkilo.authentication-service");
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static final String ROLE_HENKILONHALLINTA_PREFIX = "ROLE_APP_HENKILONHALLINTA_";
    private static final String ROLE_ANOMUSTENHALLINTA_PREFIX = "ROLE_APP_ANOMUSTENHALLINTA_";

    private HenkiloDataRepository henkiloDataRepository;
    private MyonnettyKayttoOikeusRyhmaTapahtumaDataRepository myonnettyKayttoOikeusRyhmaTapahtumaDataRepository;
    private KayttoOikeusRyhmaMyontoViiteRepository kayttoOikeusRyhmaMyontoViiteRepository;

    private OppijanumerorekisteriClient oppijanumerorekisteriClient;
    private OrganisaatioClient organisaatioClient;

    private CommonProperties commonProperties;

    private static Map<ExternalPermissionService, String> SERVICE_URIS = new HashMap<>();

    @Autowired
    public PermissionCheckerServiceImpl(OphProperties ophProperties,
                                        HenkiloDataRepository henkiloDataRepository,
                                        OrganisaatioClient organisaatioClient,
                                        OppijanumerorekisteriClient oppijanumerorekisteriClient,
                                        MyonnettyKayttoOikeusRyhmaTapahtumaDataRepository myonnettyKayttoOikeusRyhmaTapahtumaDataRepository,
                                        KayttoOikeusRyhmaMyontoViiteRepository kayttoOikeusRyhmaMyontoViiteRepository,
                                        CommonProperties commonProperties) {
        SERVICE_URIS.put(ExternalPermissionService.HAKU_APP, ophProperties.url("haku-app.external-permission-check"));
        SERVICE_URIS.put(ExternalPermissionService.SURE, ophProperties.url("suoritusrekisteri.external-permission-check"));
        this.henkiloDataRepository = henkiloDataRepository;
        this.organisaatioClient = organisaatioClient;
        this.oppijanumerorekisteriClient = oppijanumerorekisteriClient;
        this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository = myonnettyKayttoOikeusRyhmaTapahtumaDataRepository;
        this.kayttoOikeusRyhmaMyontoViiteRepository = kayttoOikeusRyhmaMyontoViiteRepository;
        this.commonProperties = commonProperties;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAllowedToAccessPerson(String personOid, List<String> allowedRoles, ExternalPermissionService permissionService) {
        return isAllowedToAccessPerson(getCurrentUserOid(), personOid, allowedRoles, permissionService, this.getCasRoles());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAllowedToAccessPersonOrSelf(String personOid, List<String> allowedRoles, ExternalPermissionService permissionService) {
        String currentUserOid = getCurrentUserOid();
        return personOid.equals(currentUserOid) || isAllowedToAccessPerson(currentUserOid, personOid, allowedRoles, permissionService, this.getCasRoles());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAllowedToAccessPerson(String callingUserOid, String personOidToAccess, List<String> allowedRoles,
                                           ExternalPermissionService permissionCheckService, Set<String> callingUserRoles) {

        if (this.hasInternalAccess(personOidToAccess, allowedRoles, callingUserRoles)) {
            return true;
        }

        // If no internal access -> try to check permission from external service

        String serviceUri = SERVICE_URIS.get(permissionCheckService);

        if (StringUtils.isBlank(personOidToAccess) || StringUtils.isBlank(serviceUri)) {
            LOG.error("isAllowedToAccess() called with empty personOid or invalid permissionCheckService");
            return false;
        }

        // Get orgs for logged in user
        if (callingUserOid == null) {
            LOG.error("isAllowedToAccess(): no logged in user found -> return no permission");
            return false;
        }

        List<OrganisaatioPerustieto> orgs = this.listOrganisaatiosByHenkiloOid(callingUserOid);
        Set<String> flattedOrgs = Sets.newHashSet();

        if (!orgs.isEmpty()) {
            for (OrganisaatioPerustieto org : orgs) {
                flattedOrgs.addAll(getOidsRecursive(org));
            }
        }

        if (flattedOrgs.isEmpty()) {
            LOG.error("No organisations found for logged in user with oid: " + callingUserOid);
            return false;
        }

        Set<String> personOidsForSamePerson = oppijanumerorekisteriClient.getAllOidsForSamePerson(personOidToAccess);

        PermissionCheckResponseDto response = checkPermissionFromExternalService(serviceUri, personOidsForSamePerson, flattedOrgs, callingUserRoles);


//        SURElla ei kaikissa tapauksissa (esim. jos YO tutkinto ennen 90-lukua) ole tietoa
//        henkilösta, joten pitää kysyä varmuuden vuoksi myös haku-appilta
        if (!response.isAccessAllowed() && ExternalPermissionService.SURE.equals(permissionCheckService)) {
            return checkPermissionFromExternalService(
                    SERVICE_URIS.get(ExternalPermissionService.HAKU_APP), personOidsForSamePerson, flattedOrgs, callingUserRoles
            ).isAccessAllowed();
        }

        if(!response.isAccessAllowed()) {
            LOG.error("Insufficient roles. permission check done from external service:"+ permissionCheckService + " Logged in user:" + callingUserOid + " accessed personId:" + personOidToAccess + " loginuser orgs:" + flattedOrgs.stream().collect(Collectors.joining(",")) + " roles needed:" + allowedRoles.stream().collect(Collectors.joining(",")), " user cas roles:" + callingUserRoles.stream().collect(Collectors.joining(",")) + " personOidsForSamePerson:" + personOidsForSamePerson.stream().collect(Collectors.joining(",")) + " external service error message:" + response.getErrorMessage());
        }

        return response.isAccessAllowed();
    }

    /**
     * Checks if the logged in user has HENKILONHALLINTA roles that
     * grants access to the wanted person (personOid)
    */
    @Override
    @Transactional(readOnly = true)
    public boolean hasInternalAccess(String personOid, List<String> allowedRolesWithoutPrefix, Set<String> callingUserRoles) {
        if (isSuperUser(callingUserRoles)) {
            return true;
        }

        Set<String> allowedRoles = getPrefixedRoles(ROLE_HENKILONHALLINTA_PREFIX, allowedRolesWithoutPrefix);

        Optional<Henkilo> henkilo = henkiloDataRepository.findByOidHenkilo(personOid);
        if (!henkilo.isPresent()) {
            return false;
        }

        // If person doesn't have any organisation -> access is granted
        // Otherwise creating persons wouldn't work, as first the person is created and only after that
        // the person is attached to an organisation
        if (henkilo.get().getOrganisaatioHenkilos().isEmpty()
                && CollectionUtils.containsAny(callingUserRoles, allowedRoles)) {
            return true;
        }

        Set<String> candidateRoles = new HashSet<>();
        for (OrganisaatioHenkilo orgHenkilo : henkilo.get().getOrganisaatioHenkilos()) {
            OrganisaatioCache organisaatioCache = orgHenkilo.getOrganisaatioCache();
            if (organisaatioCache != null) {
                String orgWithParents[] = organisaatioCache.getOrganisaatioOidPath().split("/");
                for (String allowedRole : allowedRoles) {
                    candidateRoles.addAll(getPrefixedRoles(allowedRole + "_", Lists.newArrayList(orgWithParents)));
                }
            }
        }

        return CollectionUtils.containsAny(callingUserRoles, candidateRoles);
    }

    @Override
    public boolean hasRoleForOrganisations(@NotNull List<Object> organisaatioHenkiloDtoList,
                                           List<String> allowedRolesWithoutPrefix) {
        List<String> orgOidList;
        if (organisaatioHenkiloDtoList == null || organisaatioHenkiloDtoList.isEmpty()) {
            logger.warn(this.getCurrentUserOid() + " called permission checker with empty input");
            return true;
        }
        else if (organisaatioHenkiloDtoList.get(0) instanceof OrganisaatioHenkiloCreateDto) {
            orgOidList = organisaatioHenkiloDtoList.stream().map(OrganisaatioHenkiloCreateDto.class::cast)
                    .map(OrganisaatioHenkiloCreateDto::getOrganisaatioOid).collect(Collectors.toList());
        }
        else if (organisaatioHenkiloDtoList.get(0) instanceof OrganisaatioHenkiloUpdateDto) {
            orgOidList = organisaatioHenkiloDtoList.stream().map(OrganisaatioHenkiloUpdateDto.class::cast)
                    .map(OrganisaatioHenkiloUpdateDto::getOrganisaatioOid).collect(Collectors.toList());
        }
        else if(organisaatioHenkiloDtoList.get(0) instanceof HaettuKayttooikeusryhmaDto) {
            orgOidList = organisaatioHenkiloDtoList.stream().map(HaettuKayttooikeusryhmaDto.class::cast)
                    .map(HaettuKayttooikeusryhmaDto::getAnomus).map(AnomusDto::getOrganisaatioOid).collect(Collectors.toList());
        }
        else {
            throw new NotImplementedException("Unsupported input type.");
        }
        return checkRoleForOrganisation(orgOidList, allowedRolesWithoutPrefix);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkRoleForOrganisation(@NotNull List<String> orgOidList, List<String> allowedRolesWithoutPrefix) {
        for(String oid : orgOidList) {
            if(!this.hasRoleForOrganization(oid, allowedRolesWithoutPrefix, this.getCasRoles())) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSuperUser(Set<String> roles) {
        return roles.contains(ROLE_HENKILONHALLINTA_PREFIX + "OPHREKISTERI");
    }

    private static PermissionCheckResponseDto checkPermissionFromExternalService(String serviceUrl,
                                                                                 Set<String> personOidsForSamePerson,
                                                                                 Set<String> organisationOids,
                                                                                 Set<String> loggedInUserRoles) {
        PermissionCheckRequestDto requestDTO = new PermissionCheckRequestDto();
        requestDTO.setPersonOidsForSamePerson(Lists.newArrayList(personOidsForSamePerson));
        requestDTO.setOrganisationOids(Lists.newArrayList(organisationOids));
        requestDTO.setLoggedInUserRoles(loggedInUserRoles);

        try {
            HttpResponse httpResponse = restClient.post(
                    serviceUrl, "application/json; charset=UTF-8", objectMapper.writeValueAsString(requestDTO)
            );
            return objectMapper.readValue(httpResponse.getEntity().getContent(), PermissionCheckResponseDto.class);
        }
        catch (Exception e) {
            LOG.error("External permission check failed: " + e.toString());
            throw new RuntimeException("Failed: " + e);
        }
    }

    private static Set<String> getOidsRecursive(OrganisaatioPerustieto org) {
        Preconditions.checkArgument(!StringUtils.isBlank(org.getOid()), "Organisation oid cannot be blank!");

        Set<String> oids = Sets.newHashSet(org.getOid());

        for (OrganisaatioPerustieto child : org.getChildren()) {
            oids.addAll(getOidsRecursive(child));
        }

        return oids;
    }

    private static Set<String> getPrefixedRoles(final String prefix, final List<String> rolesWithoutPrefix) {
        return rolesWithoutPrefix.stream().map(prefix::concat).collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganisaatioPerustieto> listOrganisaatiosByHenkiloOid(String oid) {
        List<OrganisaatioPerustieto> organisaatios = new ArrayList<>();
        Optional<Henkilo> tempHenkilo = henkiloDataRepository.findByOidHenkilo(oid);
        if (tempHenkilo.isPresent()) {
            Set<OrganisaatioHenkilo> orgHenkilos = tempHenkilo.get().getOrganisaatioHenkilos();
            List<String> organisaatioOids = orgHenkilos.stream().map(OrganisaatioHenkilo::getOrganisaatioOid).collect(Collectors.toList());
            organisaatios = organisaatioClient.listActiveOrganisaatioPerustiedotByOidRestrictionList(organisaatioOids);
        }
        return organisaatios;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasRoleForOrganization(String orgOid, final List<String> allowedRolesWithoutPrefix, Set<String> callingUserRoles) {
        if (isSuperUser(callingUserRoles)) {
            return true;
        }

        final Set<String> allowedRoles = getPrefixedRoles(ROLE_ANOMUSTENHALLINTA_PREFIX, allowedRolesWithoutPrefix);

        Optional<OrganisaatioPerustieto> oh = this.organisaatioClient.listActiveOrganisaatioPerustiedotByOidRestrictionList(Collections.singleton(orgOid))
                .stream().findFirst();
        if (!oh.isPresent()) {
            LOG.warn("Organization " + orgOid + " not found!");
            return false;
        }
        final OrganisaatioPerustieto org = oh.get();
        Set<String> orgAndParentOids = Sets.newHashSet(org.getParentOidPath().split("/"));
        orgAndParentOids.add(org.getOid());

        Set<Set<String>> candidateRolesByOrg = orgAndParentOids.stream().map(orgOrParentOid ->
                allowedRoles.stream().map(role -> role.concat("_" + orgOrParentOid)).collect(Collectors.toCollection(HashSet::new)))
                .collect(Collectors.toCollection(HashSet::new));

        Set<String> flattenedCandidateRolesByOrg = Sets.newHashSet(Iterables.concat(candidateRolesByOrg));

        return CollectionUtils.containsAny(flattenedCandidateRolesByOrg, callingUserRoles);
    }

    @Override
    public boolean notOwnData(String dataOwnderOid) {
        return !Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new NullPointerException("No user name available from SecurityContext!")).equals(dataOwnderOid);
    }

    @Override
    public String getCurrentUserOid() {
        String oid = SecurityContextHolder.getContext().getAuthentication().getName();
        if (oid == null) {
            throw new NullPointerException("No user name available from SecurityContext!");
        }
        return oid;
    }

    @Override
    public boolean isCurrentUserAdmin() {
        return isSuperUser(this.getCasRoles());
    }

    @Override
    public boolean kayttooikeusMyontoviiteLimitationCheck(Long kayttooikeusryhmaId) {
        List<Long> masterIdList = this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository
                .findValidMyonnettyKayttooikeus(this.getCurrentUserOid()).stream()
                .map(MyonnettyKayttoOikeusRyhmaTapahtuma::getKayttoOikeusRyhma)
                .map(KayttoOikeusRyhma::getId).collect(Collectors.toList());
        List<Long> slaveIds = this.kayttoOikeusRyhmaMyontoViiteRepository.getSlaveIdsByMasterIds(masterIdList);
        return this.isCurrentUserAdmin() || (!slaveIds.isEmpty() && slaveIds.contains(kayttooikeusryhmaId));
    }

    @Override
    public boolean organisaatioLimitationCheck(String organisaatioOid, Set<OrganisaatioViite> viiteSet) {
        // Group organizations have to match only as a general set since they're not separated by type or by individual groups
        if(organisaatioOid.startsWith(this.commonProperties.getOrganisaatioRyhmaPrefix())) {
            return viiteSet.stream().map(OrganisaatioViite::getOrganisaatioTyyppi).collect(Collectors.toList())
                    .contains(this.commonProperties.getOrganisaatioRyhmaPrefix());
        }
        OrganisaatioPerustieto organisaatioPerustieto = this.organisaatioClient.getOrganisaatioPerustiedotCached(organisaatioOid, OrganisaatioClient.Mode.requireCache());
        // Organization must have child items in it, so that the institution type can be fetched and verified
        if(!org.springframework.util.CollectionUtils.isEmpty(organisaatioPerustieto.getChildren())) {
            return organisaatioPerustieto.getChildren().stream().anyMatch(childOrganisation ->
                    viiteSet.stream().anyMatch(organisaatioViite ->
                            organisaatioViite.getOrganisaatioTyyppi()
                                    .equals(!org.springframework.util.StringUtils.isEmpty(childOrganisation.getOppilaitostyyppi())
                                            ? childOrganisation.getOppilaitostyyppi().substring(17, 19) // getOppilaitostyyppi() = "oppilaitostyyppi_11#1"
                                            : null)
                                    || organisaatioViite.getOrganisaatioTyyppi().equals(organisaatioOid)));
        }
        // if the organization doesn't have child items, then it must be a top level organization or some other type
        // organization in which case the target organization OID must match the allowed-to-organization OID
        return viiteSet.stream().map(OrganisaatioViite::getOrganisaatioTyyppi).collect(Collectors.toList())
                .contains(organisaatioOid);
    }
}
