package fi.vm.sade.kayttooikeus.service.impl;

import com.google.common.collect.Lists;
import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.dto.*;
import fi.vm.sade.kayttooikeus.repositories.criteria.HenkiloCriteria;
import fi.vm.sade.kayttooikeus.repositories.criteria.OrganisaatioHenkiloCriteria;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.*;
import fi.vm.sade.kayttooikeus.repositories.dto.HenkilohakuResultDto;
import fi.vm.sade.kayttooikeus.service.HenkiloService;
import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.kayttooikeus.util.HenkilohakuBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HenkiloServiceImpl extends AbstractService implements HenkiloService {


    private PermissionCheckerService permissionCheckerService;

    private HenkiloHibernateRepository henkiloHibernateRepository;
    private final OrganisaatioHenkiloRepository organisaatioHenkiloRepository;
    private final OrganisaatioHenkiloDataRepository organisaatioHenkiloDataRepository;
    private final MyonnettyKayttoOikeusRyhmaTapahtumaDataRepository myonnettyKayttoOikeusRyhmaTapahtumaDataRepository;
    private final KayttoOikeusRyhmaTapahtumaHistoriaDataRepository kayttoOikeusRyhmaTapahtumaHistoriaDataRepository;
    private final HenkiloDataRepository henkiloDataRepository;

    private final CommonProperties commonProperties;

    private final OppijanumerorekisteriClient oppijanumerorekisteriClient;

    private final OrikaBeanMapper mapper;

    @Autowired
    HenkiloServiceImpl(HenkiloHibernateRepository henkiloHibernateRepository,
                       PermissionCheckerService permissionCheckerService,
                       KayttoOikeusRyhmaTapahtumaHistoriaDataRepository kayttoOikeusRyhmaTapahtumaHistoriaDataRepository,
                       OrganisaatioHenkiloRepository organisaatioHenkiloRepository,
                       OrganisaatioHenkiloDataRepository organisaatioHenkiloDataRepository,
                       MyonnettyKayttoOikeusRyhmaTapahtumaDataRepository myonnettyKayttoOikeusRyhmaTapahtumaDataRepository,
                       CommonProperties commonProperties,
                       HenkiloDataRepository henkiloDataRepository,
                       OppijanumerorekisteriClient oppijanumerorekisteriClient,
                       OrikaBeanMapper mapper) {
        this.henkiloHibernateRepository = henkiloHibernateRepository;
        this.permissionCheckerService = permissionCheckerService;
        this.kayttoOikeusRyhmaTapahtumaHistoriaDataRepository = kayttoOikeusRyhmaTapahtumaHistoriaDataRepository;
        this.organisaatioHenkiloRepository = organisaatioHenkiloRepository;
        this.organisaatioHenkiloDataRepository = organisaatioHenkiloDataRepository;
        this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository = myonnettyKayttoOikeusRyhmaTapahtumaDataRepository;
        this.commonProperties = commonProperties;
        this.henkiloDataRepository = henkiloDataRepository;
        this.oppijanumerorekisteriClient = oppijanumerorekisteriClient;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public KayttooikeudetDto getKayttooikeudet(String henkiloOid, OrganisaatioHenkiloCriteria criteria) {
        // juuriorganisaatioon kuuluvalla henkilöllä on oikeus kaikkiin alla oleviin organisaatioihin
        String rootOrganizationOid = commonProperties.getRootOrganizationOid();
        if (organisaatioHenkiloRepository.isHenkiloInOrganisaatio(henkiloOid, rootOrganizationOid, false)) {
            if (criteria.getOrganisaatioOids() != null || criteria.getKayttoOikeusRyhmaNimet() != null) {
                // haetaan organisaatioon kuuluvat henkilöt
                return KayttooikeudetDto.admin(henkiloHibernateRepository.findOidsBy(criteria));
            } else {
                // henkilöllä on oikeutus kaikkiin henkilötietoihin
                return KayttooikeudetDto.admin(null);
            }
        }

        // perustapauksena henkilöllä on oikeus omien organisaatioiden henkilötietoihin
        return KayttooikeudetDto.user(henkiloHibernateRepository.findOidsBySamaOrganisaatio(henkiloOid, criteria));
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findHenkilos(OrganisaatioOidsSearchDto organisaatioOidsSearchDto) {
        ArrayList<String> allowedRoles = Lists.newArrayList("READ", "READ_UPDATE", "CRUD");
        Set<String> roles = getCasRoles();

        return henkiloHibernateRepository.findHenkiloOids(organisaatioOidsSearchDto.getHenkiloTyyppi(),
                organisaatioOidsSearchDto.getOrganisaatioOids(), organisaatioOidsSearchDto.getGroupName())
                .stream()
                .filter(henkiloOid -> permissionCheckerService.hasInternalAccess(henkiloOid, allowedRoles, roles))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void disableHenkiloOrganisationsAndKayttooikeus(String henkiloOid, String kasittelijaOid) {
        if(StringUtils.isEmpty(kasittelijaOid)) {
            kasittelijaOid = getCurrentUserOid();
        }
        final String kasittelijaOidFinal = kasittelijaOid;
        List<OrganisaatioHenkilo> orgHenkilos = this.organisaatioHenkiloDataRepository.findByHenkiloOidHenkilo(henkiloOid);
        for (OrganisaatioHenkilo oh : orgHenkilos) {
            oh.setPassivoitu(true);
            Set<MyonnettyKayttoOikeusRyhmaTapahtuma> mkorts = oh.getMyonnettyKayttoOikeusRyhmas();
            if (!CollectionUtils.isEmpty(mkorts)) {
                for (Iterator<MyonnettyKayttoOikeusRyhmaTapahtuma> mkortIterator = mkorts.iterator(); mkortIterator.hasNext();) {
                    MyonnettyKayttoOikeusRyhmaTapahtuma mkort = mkortIterator.next();
                    // Create event
                    Henkilo kasittelija = this.henkiloDataRepository.findByOidHenkilo(kasittelijaOid)
                            .orElseThrow(() -> new NotFoundException("Käsittelija not found by oid " + kasittelijaOidFinal));
                    KayttoOikeusRyhmaTapahtumaHistoria deleteEvent = mkort.toHistoria(
                            kasittelija, KayttoOikeudenTila.SULJETTU,
                            LocalDateTime.now(), "Oikeuksien poisto, koko henkilön passivointi");
                    this.kayttoOikeusRyhmaTapahtumaHistoriaDataRepository.save(deleteEvent);

                    // Remove kayttooikeus
                    mkortIterator.remove();
                    this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository.delete(mkort.getId());
                }
            }
        }
    }

    /*
    admin:
        ONR (mahd. iso query) -> rikastus (x)
    OPH-virkailija:
        1) ONR (mahd. iso query) -> rajaus -> rikastus (x)
        2) rajaustiedot (~100k query) -> ONR (oidrajauksella) -> rikastus
    muu:
        1) ONR (mahd. iso query) -> rajaus -> rikastus
        2) rajaustiedot -> ONR (oidrajauksella) -> rikastus (x)
     */
    @Override
    @Transactional(readOnly = true)
    public List<HenkilohakuResultDto> henkilohaku(HenkilohakuCriteriaDto henkilohakuCriteriaDto) {
        return new HenkilohakuBuilder(this.henkiloHibernateRepository, this.mapper, this.permissionCheckerService,
                this.organisaatioHenkiloDataRepository, this.henkiloDataRepository)
                .builder(henkilohakuCriteriaDto)
                .search()
                .exclusion()
                .enrichment()
                .build();
    }

}
