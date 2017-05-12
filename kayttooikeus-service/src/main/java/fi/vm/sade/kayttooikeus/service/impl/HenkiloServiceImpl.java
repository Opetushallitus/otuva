package fi.vm.sade.kayttooikeus.service.impl;

import com.google.common.collect.Lists;
import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.dto.KayttoOikeudenTila;
import fi.vm.sade.kayttooikeus.dto.KayttooikeudetDto;
import fi.vm.sade.kayttooikeus.repositories.OrganisaatioHenkiloCriteria;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioOidsSearchDto;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.*;
import fi.vm.sade.kayttooikeus.service.HenkiloService;
import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class HenkiloServiceImpl extends AbstractService implements HenkiloService {

    private HenkiloHibernateRepository henkiloHibernateRepository;

    private PermissionCheckerService permissionCheckerService;

    private final OrganisaatioHenkiloRepository organisaatioHenkiloRepository;
    private final OrganisaatioHenkiloDataRepository organisaatioHenkiloDataRepository;
    private final MyonnettyKayttoOikeusRyhmaTapahtumaDataRepository myonnettyKayttoOikeusRyhmaTapahtumaDataRepository;
    private final KayttoOikeusRyhmaTapahtumaHistoriaDataRepository kayttoOikeusRyhmaTapahtumaHistoriaDataRepository;
    private final HenkiloRepository henkiloRepository;
    private final CommonProperties commonProperties;

    @Autowired
    HenkiloServiceImpl(HenkiloHibernateRepository henkiloHibernateRepository,
                       PermissionCheckerService permissionCheckerService,
                       KayttoOikeusRyhmaTapahtumaHistoriaDataRepository kayttoOikeusRyhmaTapahtumaHistoriaDataRepository,
                       OrganisaatioHenkiloRepository organisaatioHenkiloRepository,
                       OrganisaatioHenkiloDataRepository organisaatioHenkiloDataRepository,
                       MyonnettyKayttoOikeusRyhmaTapahtumaDataRepository myonnettyKayttoOikeusRyhmaTapahtumaDataRepository,
                       CommonProperties commonProperties,
                       HenkiloRepository henkiloRepository) {
        this.henkiloHibernateRepository = henkiloHibernateRepository;
        this.permissionCheckerService = permissionCheckerService;
        this.kayttoOikeusRyhmaTapahtumaHistoriaDataRepository = kayttoOikeusRyhmaTapahtumaHistoriaDataRepository;
        this.organisaatioHenkiloRepository = organisaatioHenkiloRepository;
        this.organisaatioHenkiloDataRepository = organisaatioHenkiloDataRepository;
        this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository = myonnettyKayttoOikeusRyhmaTapahtumaDataRepository;
        this.commonProperties = commonProperties;
        this.henkiloRepository = henkiloRepository;
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
                    Henkilo kasittelija = this.henkiloRepository.findByOidHenkilo(kasittelijaOid)
                            .orElseThrow(() -> new NotFoundException("Käsittelija not found by oid " + kasittelijaOidFinal));
                    KayttoOikeusRyhmaTapahtumaHistoria deleteEvent = mkort.toHistoria(
                            kasittelija, KayttoOikeudenTila.SULJETTU,
                            new DateTime(), "Oikeuksien poisto, koko henkilön passivointi");
                    this.kayttoOikeusRyhmaTapahtumaHistoriaDataRepository.save(deleteEvent);

                    // Remove kayttooikeus
                    mkortIterator.remove();
                    this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository.delete(mkort.getId());
                }
            }
        }
    }
}
