package fi.vm.sade.kayttooikeus.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import fi.vm.sade.generic.rest.CachingRestClient;
import fi.vm.sade.kayttooikeus.dto.HenkiloTyyppi;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloCreateDto;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloUpdateDto;
import fi.vm.sade.kayttooikeus.dto.permissioncheck.ExternalPermissionService;
import fi.vm.sade.kayttooikeus.dto.permissioncheck.PermissionCheckRequestDto;
import fi.vm.sade.kayttooikeus.dto.permissioncheck.PermissionCheckResponseDto;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.OrganisaatioCache;
import fi.vm.sade.kayttooikeus.model.OrganisaatioHenkilo;
import fi.vm.sade.kayttooikeus.repositories.HenkiloRepository;
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

    private HenkiloRepository henkiloRepository;
    private OppijanumerorekisteriClient oppijanumerorekisteriClient;

    private OrganisaatioClient organisaatioClient;

    private static Map<ExternalPermissionService, String> SERVICE_URIS = new HashMap<>();

    @Autowired
    public PermissionCheckerServiceImpl(OphProperties ophProperties,
                                        HenkiloRepository henkiloRepository,
                                        OrganisaatioClient organisaatioClient,
                                        OppijanumerorekisteriClient oppijanumerorekisteriClient) {
        SERVICE_URIS.put(ExternalPermissionService.HAKU_APP, ophProperties.url("haku-app.external-permission-check"));
        SERVICE_URIS.put(ExternalPermissionService.SURE, ophProperties.url("suoritusrekisteri.external-permission-check"));
        this.henkiloRepository = henkiloRepository;
        this.organisaatioClient = organisaatioClient;
        this.oppijanumerorekisteriClient = oppijanumerorekisteriClient;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAllowedToAccessPerson(String personOid, List<String> allowedRoles, ExternalPermissionService permissionService) {
        return isAllowedToAccessPerson(getCurrentUserOid(), personOid, allowedRoles, permissionService, getCasRoles());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAllowedToAccessPersonOrSelf(String personOid, List<String> allowedRoles, ExternalPermissionService permissionService) {
        String currentUserOid = getCurrentUserOid();
        if (personOid.equals(currentUserOid)) {
            return true;
        }
        return isAllowedToAccessPerson(currentUserOid, personOid, allowedRoles, permissionService, getCasRoles());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAllowedToAccessPerson(String callingUserOid, String personOidToAccess, List<String> allowedRoles,
                                           ExternalPermissionService permissionCheckService, Set<String> callingUserRoles) {

        if (hasInternalAccess(personOidToAccess, allowedRoles, callingUserRoles)) {
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

        Optional<Henkilo> henkilo = henkiloRepository.findByOidHenkilo(personOid);
        if (!henkilo.isPresent()) {
            return false;
        }

        // If person doesn't have any organisation (and is not of type "OPPIJA") -> access is granted
        // Otherwise creating persons wouldn't work, as first the person is created and only after that
        // the person is attached to an organisation
        if (henkilo.get().getOrganisaatioHenkilos().isEmpty()
                && !HenkiloTyyppi.OPPIJA.equals(henkilo.get().getHenkiloTyyppi())
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
            throw new IllegalArgumentException("Permissionchecker received empty input.");
        }
        else if (organisaatioHenkiloDtoList.get(0) instanceof OrganisaatioHenkiloCreateDto) {
            orgOidList = organisaatioHenkiloDtoList.stream()
                    .map(o -> ((OrganisaatioHenkiloCreateDto)o).getOrganisaatioOid()).collect(Collectors.toList());
        }
        else if (organisaatioHenkiloDtoList.get(0) instanceof OrganisaatioHenkiloUpdateDto) {
            orgOidList = organisaatioHenkiloDtoList.stream()
                    .map(o -> ((OrganisaatioHenkiloUpdateDto)o).getOrganisaatioOid()).collect(Collectors.toList());
        }
        else {
            throw new NotImplementedException("Unsupported input type.");
        }
        return checkRoleForOrganisation(allowedRolesWithoutPrefix, orgOidList);
    }

    private boolean checkRoleForOrganisation(List<String> allowedRolesWithoutPrefix, List<String> orgOidList) {
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
        return FluentIterable.from(rolesWithoutPrefix).transform(role -> prefix.concat(role)).toSet();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganisaatioPerustieto> listOrganisaatiosByHenkiloOid(String oid) {
        List<OrganisaatioPerustieto> organisaatios = new ArrayList<>();
        Optional<Henkilo> tempHenkilo = henkiloRepository.findByOidHenkilo(oid);
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
}
