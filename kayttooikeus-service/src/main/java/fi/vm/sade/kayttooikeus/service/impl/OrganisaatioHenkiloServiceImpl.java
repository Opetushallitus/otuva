package fi.vm.sade.kayttooikeus.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.dto.*;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloWithOrganisaatioDto.OrganisaatioDto;
import fi.vm.sade.kayttooikeus.repositories.*;
import fi.vm.sade.kayttooikeus.service.OrganisaatioHenkiloService;
import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient.Mode;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioPerustieto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static fi.vm.sade.kayttooikeus.dto.HenkiloTyyppi.PALVELU;
import static fi.vm.sade.kayttooikeus.dto.HenkiloTyyppi.VIRKAILIJA;

import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.OrganisaatioHenkilo;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import static fi.vm.sade.kayttooikeus.dto.Localizable.comparingPrimarlyBy;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Service
public class OrganisaatioHenkiloServiceImpl extends AbstractService implements OrganisaatioHenkiloService {
    private final String HENKILOHALLINTA_PALVELUNAME = "HENKILONHALLINTA";
    private final String ROOLI_OPH_REKISTERINPITAJA = "OPHREKISTERI";
    private final String ROOLI_CRUD = "CRUD";
    private final String FALLBACK_LANGUAGE = "fi";

    private final OrganisaatioHenkiloRepository organisaatioHenkiloRepository;
    private final OrganisaatioHenkiloDataRepository organisaatioHenkiloDataRepository;
    private final KayttoOikeusRepository kayttoOikeusRepository;
    private final HenkiloDataRepository henkiloDataRepository;
    private final OrikaBeanMapper mapper;
    private final OrganisaatioClient organisaatioClient;
    private final PermissionCheckerService permissionCheckerService;
    private final MyonnettyKayttoOikeusRyhmaTapahtumaDataRepository myonnettyKayttoOikeusRyhmaTapahtumaDataRepository;

    @Autowired
    public OrganisaatioHenkiloServiceImpl(OrganisaatioHenkiloRepository organisaatioHenkiloRepository,
                                          OrganisaatioHenkiloDataRepository organisaatioHenkiloDataRepository,
                                          KayttoOikeusRepository kayttoOikeusRepository,
                                          HenkiloDataRepository henkiloDataRepository,
                                          OrikaBeanMapper mapper,
                                          OrganisaatioClient organisaatioClient,
                                          PermissionCheckerService permissionCheckerService,
                                          MyonnettyKayttoOikeusRyhmaTapahtumaDataRepository myonnettyKayttoOikeusRyhmaTapahtumaDataRepository) {
        this.organisaatioHenkiloRepository = organisaatioHenkiloRepository;
        this.organisaatioHenkiloDataRepository = organisaatioHenkiloDataRepository;
        this.kayttoOikeusRepository = kayttoOikeusRepository;
        this.henkiloDataRepository = henkiloDataRepository;
        this.mapper = mapper;
        this.organisaatioClient = organisaatioClient;
        this.permissionCheckerService = permissionCheckerService;
        this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository = myonnettyKayttoOikeusRyhmaTapahtumaDataRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganisaatioHenkiloWithOrganisaatioDto> listOrganisaatioHenkilos(String henkiloOid, String compareByLang) {
        Mode organisaatioClientMode = Mode.requireCache();
        return organisaatioHenkiloRepository.findActiveOrganisaatioHenkiloListDtos(henkiloOid)
                .stream().peek(organisaatioHenkilo ->
                    organisaatioHenkilo.setOrganisaatio(
                        mapOrganisaatioDtoRecursive(
                            this.organisaatioClient.getOrganisaatioPerustiedotCached(
                                    organisaatioHenkilo.getOrganisaatio().getOid(),
                                    organisaatioClientMode),
                            compareByLang))
                ).sorted(Comparator.comparing(dto -> dto.getOrganisaatio().getNimi(),
                        comparingPrimarlyBy(ofNullable(compareByLang).orElse(FALLBACK_LANGUAGE)))).collect(toList());
    }

    protected OrganisaatioDto mapOrganisaatioDtoRecursive(OrganisaatioPerustieto perustiedot, String compareByLang) {
        OrganisaatioDto dto = new OrganisaatioDto();
        dto.setOid(perustiedot.getOid());
        dto.setNimi(new TextGroupMapDto(null, perustiedot.getNimi()));
        dto.setParentOidPath(perustiedot.getParentOidPath());
        dto.setTyypit(perustiedot.getTyypit());
        dto.setChildren(perustiedot.getChildren().stream().map(child -> mapOrganisaatioDtoRecursive(child, compareByLang))
            .sorted(Comparator.comparing(OrganisaatioDto::getNimi, comparingPrimarlyBy(ofNullable(compareByLang).orElse(FALLBACK_LANGUAGE))))
            .collect(toList()));
        return dto;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<OrganisaatioPerustieto> listOrganisaatioPerustiedotForCurrentUser() {
        return organisaatioClient.listActiveOrganisaatioPerustiedotByOidRestrictionList(
                organisaatioHenkiloRepository.findDistinctOrganisaatiosForHenkiloOid(getCurrentUserOid()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HenkiloTyyppi> listPossibleHenkiloTypesAccessibleForCurrentUser() {
        if (kayttoOikeusRepository.isHenkiloMyonnettyKayttoOikeusToPalveluInRole(getCurrentUserOid(),
                HENKILOHALLINTA_PALVELUNAME, ROOLI_OPH_REKISTERINPITAJA)) {
            return asList(VIRKAILIJA, PALVELU);
        }
        if (kayttoOikeusRepository.isHenkiloMyonnettyKayttoOikeusToPalveluInRole(getCurrentUserOid(),
                HENKILOHALLINTA_PALVELUNAME, ROOLI_CRUD)) {
            return singletonList(VIRKAILIJA);
        }
        return emptyList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrganisaatioHenkiloDto findOrganisaatioHenkiloByHenkiloAndOrganisaatio(String henkiloOid, String organisaatioOid) {
        return organisaatioHenkiloRepository.findByHenkiloOidAndOrganisaatioOid(henkiloOid, organisaatioOid)
                .orElseThrow(() -> new NotFoundException("Could not find organisaatiohenkilo"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganisaatioHenkiloDto> findOrganisaatioByHenkilo(String henkiloOid) {
        return organisaatioHenkiloRepository.findOrganisaatioHenkilosForHenkilo(henkiloOid);
    }

    @Override
    @Transactional(readOnly = false)
    public List<OrganisaatioHenkiloDto> addOrganisaatioHenkilot(String henkiloOid, List<OrganisaatioHenkiloCreateDto> organisaatioHenkilot) {
        Henkilo henkilo = henkiloDataRepository.findByOidHenkilo(henkiloOid)
                .orElseThrow(() -> new NotFoundException("Henkilöä ei löytynyt OID:lla " + henkiloOid));
        return findOrCreateOrganisaatioHenkilos(organisaatioHenkilot, henkilo);
    }

    private List<OrganisaatioHenkiloDto> findOrCreateOrganisaatioHenkilos(List<OrganisaatioHenkiloCreateDto> organisaatioHenkilot, Henkilo henkilo) {
        final Mode clientMode = Mode.requireCache();

        organisaatioHenkilot.stream()
                .filter((OrganisaatioHenkiloCreateDto t) ->
                    henkilo.getOrganisaatioHenkilos().stream()
                            .noneMatch((OrganisaatioHenkilo u) -> u.getOrganisaatioOid().equals(t.getOrganisaatioOid()))
                )
                .forEach((OrganisaatioHenkiloCreateDto t) -> {
                    this.organisaatioClient.getOrganisaatioPerustiedotCached(t.getOrganisaatioOid(), clientMode);
                    OrganisaatioHenkilo organisaatioHenkilo = mapper.map(t, OrganisaatioHenkilo.class);
                    organisaatioHenkilo.setHenkilo(henkilo);
                    henkilo.getOrganisaatioHenkilos().add(organisaatioHenkiloRepository.persist(organisaatioHenkilo));
                });

        return mapper.mapAsList(henkilo.getOrganisaatioHenkilos(), OrganisaatioHenkiloDto.class);
    }

    @Override
    @Transactional
    public List<OrganisaatioHenkiloDto> createOrUpdateOrganisaatioHenkilos(String henkiloOid,
                                                                           List<OrganisaatioHenkiloUpdateDto> organisaatioHenkiloDtoList) {
        final Mode clientMode = Mode.requireCache();
        Henkilo henkilo = this.henkiloDataRepository.findByOidHenkilo(henkiloOid)
                .orElseThrow(() -> new NotFoundException("Henkilöä ei löytynyt OID:lla " + henkiloOid));
        this.findOrCreateOrganisaatioHenkilos(this.mapper.mapAsList(organisaatioHenkiloDtoList, OrganisaatioHenkiloCreateDto.class),
                henkilo);

        organisaatioHenkiloDtoList.stream()
                .filter((OrganisaatioHenkiloUpdateDto t) ->
                        henkilo.getOrganisaatioHenkilos().stream()
                                .anyMatch((OrganisaatioHenkilo u) -> u.getOrganisaatioOid().equals(t.getOrganisaatioOid()))
                )
                .forEach(organisaatioHenkiloUpdateDto -> {
                    if(!this.getCurrentUserOid().equals(henkiloOid)) {
                        this.permissionCheckerService.hasRoleForOrganisations(Collections.singletonList(organisaatioHenkiloUpdateDto),
                                Lists.newArrayList("CRUD", "READ_UPDATE"));
                    }
                    this.organisaatioClient.getOrganisaatioPerustiedotCached(organisaatioHenkiloUpdateDto.getOrganisaatioOid(),
                            clientMode);
                    OrganisaatioHenkilo savedOrgHenkilo = this.findFirstMatching(organisaatioHenkiloUpdateDto,
                            henkilo.getOrganisaatioHenkilos());
                    // Do not allow updating organisation oid (should never happen since organisaatiohenkilo is found by this value)
                    if (!savedOrgHenkilo.getOrganisaatioOid().equals(organisaatioHenkiloUpdateDto.getOrganisaatioOid())) {
                        throw new InternalError("Trying to update organisaatio henkilo organisation oid");
                    }
                    this.mapper.map(organisaatioHenkiloUpdateDto, savedOrgHenkilo);
                });
        return mapper.mapAsList(henkilo.getOrganisaatioHenkilos(), OrganisaatioHenkiloDto.class);
    }

    @Transactional
    @Override
    public void passivoiHenkiloOrganisation(String oidHenkilo, String henkiloOrganisationOid) {
        OrganisaatioHenkilo organisaatioHenkilo = this.organisaatioHenkiloDataRepository
                .findByHenkiloOidHenkiloAndOrganisaatioOid(oidHenkilo, henkiloOrganisationOid)
                .orElseThrow(() -> new NotFoundException("Unknown organisation" + henkiloOrganisationOid + "for henkilo" + oidHenkilo));
        organisaatioHenkilo.setPassivoitu(true);

        organisaatioHenkilo.getMyonnettyKayttoOikeusRyhmas().forEach(myonnettyKayttoOikeusRyhmaTapahtuma ->
                myonnettyKayttoOikeusRyhmaTapahtuma.setTila(KayttoOikeudenTila.SULJETTU));
    }

    private OrganisaatioHenkilo findFirstMatching(OrganisaatioHenkiloUpdateDto organisaatioHenkilo,
                                                           Set<OrganisaatioHenkilo> organisaatioHenkiloCreateDtoList) {
        return organisaatioHenkiloCreateDtoList.stream().filter((OrganisaatioHenkilo t) ->
                organisaatioHenkilo.getOrganisaatioOid().equals(t.getOrganisaatioOid())
        ).findFirst().orElseThrow(() -> new NotFoundException("Could not update organisaatiohenkilo with oid "
                + organisaatioHenkilo.getOrganisaatioOid()));
    }

}
