package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.dto.*;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.*;
import fi.vm.sade.kayttooikeus.repositories.criteria.AnomusCriteria;
import fi.vm.sade.kayttooikeus.repositories.criteria.OrganisaatioHenkiloCriteria;
import fi.vm.sade.kayttooikeus.repositories.dto.HenkilohakuResultDto;
import fi.vm.sade.kayttooikeus.service.MyonnettyKayttoOikeusService;
import fi.vm.sade.kayttooikeus.service.OrganisaatioHenkiloService;
import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioPerustieto;
import fi.vm.sade.kayttooikeus.util.OrganisaatioMyontoPredicate;
import fi.vm.sade.kayttooikeus.util.UserDetailsUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.StreamSupport;

import static fi.vm.sade.kayttooikeus.dto.Localizable.comparingPrimarlyBy;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganisaatioHenkiloServiceImpl implements OrganisaatioHenkiloService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganisaatioHenkiloServiceImpl.class);
    private static final int LAKKAUTUS_BATCH_SIZE = 50;

    private final String FALLBACK_LANGUAGE = "fi";

    private final OrganisaatioHenkiloRepository organisaatioHenkiloRepository;
    private final HenkiloDataRepository henkiloDataRepository;
    private final LakkautettuOrganisaatioRepository lakkautettuOrganisaatioRepository;
    private final HaettuKayttooikeusRyhmaRepository haettuKayttooikeusRyhmaRepository;

    private final PermissionCheckerService permissionCheckerService;
    private final MyonnettyKayttoOikeusService myonnettyKayttoOikeusService;

    private final OrganisaatioClient organisaatioClient;

    private final CommonProperties commonProperties;

    @Override
    @Transactional(readOnly = true)
    public List<OrganisaatioHenkiloWithOrganisaatioDto> listOrganisaatioHenkilos(String henkiloOid, String compareByLang, PalveluRooliGroup requiredRoles) {
        return organisaatioHenkiloRepository.findActiveOrganisaatioHenkiloListDtos(henkiloOid, requiredRoles)
                .stream()
                .peek(organisaatioHenkilo ->
                    organisaatioHenkilo.setOrganisaatio(
                        mapOrganisaatioDtoRecursive(
                                this.organisaatioClient
                                        .getOrganisaatioPerustiedotCached(organisaatioHenkilo.getOrganisaatio().getOid())
                                        .orElseGet(() -> {
                                            String organisaatioOid = organisaatioHenkilo.getOrganisaatio().getOid();
                                            LOGGER.warn("Henkilön {} organisaatiota {} ei löytynyt", henkiloOid, organisaatioOid);
                                            return UserDetailsUtil.createUnknownOrganisation(organisaatioOid);
                                        }),
                                compareByLang, permissionCheckerService.isCurrentUserAdmin()))
                ).sorted(Comparator.comparing(dto -> dto.getOrganisaatio().getNimi(),
                        comparingPrimarlyBy(ofNullable(compareByLang).orElse(FALLBACK_LANGUAGE)))).collect(toList());
    }

    private OrganisaatioWithChildrenDto mapOrganisaatioDtoRecursive(OrganisaatioPerustieto perustiedot, String compareByLang, boolean passiiviset) {
        OrganisaatioWithChildrenDto dto = new OrganisaatioWithChildrenDto();
        dto.setOid(perustiedot.getOid());
        dto.setNimi(new TextGroupMapDto(null, perustiedot.getNimi()));
        dto.setParentOidPath(perustiedot.getParentOidPath());
        dto.setTyypit(perustiedot.getTyypit());
        dto.setStatus(perustiedot.getStatus());
        dto.setChildren(perustiedot.getChildren().stream()
               .filter(new OrganisaatioMyontoPredicate(passiiviset))
                .map(child -> mapOrganisaatioDtoRecursive(child, compareByLang, passiiviset))
                .sorted(Comparator.comparing(OrganisaatioWithChildrenDto::getNimi, comparingPrimarlyBy(ofNullable(compareByLang).orElse(FALLBACK_LANGUAGE))))
                .collect(toList()));
        return dto;
    }

    @Override
    public Collection<String> listOrganisaatioOidBy(OrganisaatioHenkiloCriteria criteria) {
        String kayttajaOid = permissionCheckerService.getCurrentUserOid();
        List<String> organisaatioOids = organisaatioHenkiloRepository.findUsersOrganisaatioHenkilosByPalveluRoolis(
                kayttajaOid, PalveluRooliGroup.KAYTTAJAHAKU);
        if (!organisaatioOids.contains(commonProperties.getRootOrganizationOid())) {
            criteria.setOrRetainOrganisaatioOids(organisaatioOids);
        }
        return organisaatioHenkiloRepository.findOrganisaatioOidBy(criteria);
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

    @Transactional
    @Override
    public void passivoiHenkiloOrganisation(String oidHenkilo, String henkiloOrganisationOid) {
        Henkilo kasittelija = this.henkiloDataRepository.findByOidHenkilo(UserDetailsUtil.getCurrentUserOid())
                .orElseThrow(() -> new NotFoundException("Could not find current henkilo with oid " + UserDetailsUtil.getCurrentUserOid()));
        OrganisaatioHenkilo organisaatioHenkilo = this.organisaatioHenkiloRepository
                .findByHenkiloOidHenkiloAndOrganisaatioOid(oidHenkilo, henkiloOrganisationOid)
                .orElseThrow(() -> new NotFoundException("Unknown organisation" + henkiloOrganisationOid + "for henkilo" + oidHenkilo));
        this.passivoiOrganisaatioHenkiloJaPoistaKayttooikeudet(organisaatioHenkilo, kasittelija, "Henkilön passivointi");
    }

    // passivoi organisaatiohenkilön ja poistaa käyttöoikeudet
    private void passivoiOrganisaatioHenkiloJaPoistaKayttooikeudet(OrganisaatioHenkilo organisaatioHenkilo, Henkilo kasittelija, String selite) {
        myonnettyKayttoOikeusService.passivoi(organisaatioHenkilo, new MyonnettyKayttoOikeusService.DeleteDetails(
                kasittelija, KayttoOikeudenTila.SULJETTU, selite));

        this.henkiloDataRepository.findByOidHenkilo(organisaatioHenkilo.getHenkilo().getOidHenkilo())
                .ifPresent(this::disableNonValidVarmennettavas);
    }

    // HenkiloVarmentaja suhde on validi jos löytyy yhä yhteinen aktiivinen organisaatio
    private void disableNonValidVarmennettavas(Henkilo henkilo) {
        henkilo.getHenkiloVarmennettavas().forEach(henkiloVarmentaja -> {
            boolean isValid = henkiloVarmentaja.getVarmennettavaHenkilo().getOrganisaatioHenkilos().stream()
                    .filter(OrganisaatioHenkilo::isAktiivinen)
                    .map(OrganisaatioHenkilo::getOrganisaatioOid)
                    .anyMatch(organisaatioHenkilo -> henkilo.getOrganisaatioHenkilos().stream()
                            .filter(OrganisaatioHenkilo::isAktiivinen)
                            .map(OrganisaatioHenkilo::getOrganisaatioOid)
                            .anyMatch(organisaatioOid -> organisaatioOid.equals(organisaatioHenkilo)));
            henkiloVarmentaja.setTila(isValid);
        });
    }

    @Transactional
    @Override
    public void kasitteleOrganisaatioidenLakkautus(String kasittelijaOid) {
        LOGGER.info("Aloitetaan passivoitujen organisaatioiden organisaatiohenkilöiden passivointi sekä käyttöoikeuksien ja anomusten poisto");
        Henkilo kasittelija = this.henkiloDataRepository.findByOidHenkilo(kasittelijaOid)
                .orElseThrow(() -> new NotFoundException("Could not find henkilo with oid " + kasittelijaOid));
        Set<String> passiivisetOids = organisaatioClient.getLakkautetutOids();
        Set<String> kasiteltyOids = StreamSupport.stream(lakkautettuOrganisaatioRepository.findAll().spliterator(), false)
                .map(LakkautettuOrganisaatio::getOid)
                .collect(toSet());
        passiivisetOids.removeAll(kasiteltyOids);
        List<OrganisaatioHenkilo> aktiivisetOrganisaatioHenkilosInLakkautetutOrganisaatios = this.organisaatioHenkiloRepository.findByOrganisaatioOidIn(passiivisetOids)
                .stream().filter(oh -> oh.isAktiivinen()).collect(toList());
        LOGGER.info("Passivoidaan {} aktiivista organisaatiohenkilöä ja näiden voimassa olevat käyttöoikeudet.", aktiivisetOrganisaatioHenkilosInLakkautetutOrganisaatios.size());
        aktiivisetOrganisaatioHenkilosInLakkautetutOrganisaatios.forEach(organisaatioHenkilo -> this.passivoiOrganisaatioHenkiloJaPoistaKayttooikeudet(organisaatioHenkilo, kasittelija, "Passivoidun organisaation organisaatiohenkilön passivointi ja käyttöoikeuksien poisto"));

        if (!passiivisetOids.isEmpty()) { // anomushaku palauttaa tyhjällä organisaatioOids-listalla kaikki anomukset
            AnomusCriteria anomusCriteria = AnomusCriteria.builder().organisaatioOids(passiivisetOids).onlyActive(true).build();
            this.poistaAnomuksetOrganisaatioista(anomusCriteria);
        }

        lakkautettuOrganisaatioRepository.persistInBatch(passiivisetOids, LAKKAUTUS_BATCH_SIZE);
        LOGGER.info("Lopetetaan passivoitujen organisaatioiden organisaatiohenkilöiden passivointi sekä käyttöoikeuksien ja anomusten poisto");
    }

    private void poistaAnomuksetOrganisaatioista(AnomusCriteria criteria) {
        List<HaettuKayttoOikeusRyhma> haettuKayttoOikeusRyhmas = this.haettuKayttooikeusRyhmaRepository.findBy(criteria.createAnomusSearchCondition(this.organisaatioClient));

        log.info("Poistetaan {} anomusta ja {} niihin liittyvää haettua käyttöoikeusryhmää",
                haettuKayttoOikeusRyhmas.stream().map(h -> h.getAnomus().getId()).distinct().count(), haettuKayttoOikeusRyhmas.size());
        haettuKayttoOikeusRyhmas.stream().forEach(h -> {
            Anomus anomus = h.getAnomus();
            if(h.getAnomus().getHaettuKayttoOikeusRyhmas().size() == 1) {
//                 Asetetaan anomus hylätyksi, jos ollaan poistamassa viimeistä siihen liitettyä haettua käyttöoikeusryhmä
                anomus.setAnomuksenTila(AnomuksenTila.HYLATTY);
                anomus.setHylkaamisperuste("Hylätään lakkautetun organisaation anomuksena");
            }
            anomus.getHaettuKayttoOikeusRyhmas().remove(h);
            anomus.setAnomusTilaTapahtumaPvm(LocalDateTime.now());
            this.haettuKayttooikeusRyhmaRepository.delete(h);
        });
    }

    @Override
    public Set<HenkilohakuResultDto> addOrganisaatioInformation(Set<HenkilohakuResultDto> set) {
        List<String> oidList = set.stream().map(HenkilohakuResultDto::getOidHenkilo).collect(toList());
        Map<String, List<String>> organisaatioHenkiloByHenkiloOid = organisaatioHenkiloRepository.findActiveByHenkiloOids(oidList);
        return set.stream().map(dto -> {
            var orgList = organisaatioHenkiloByHenkiloOid.get(dto.getOidHenkilo());
            if (orgList != null) {
                dto.setOrganisaatioNimiList(
                    orgList.stream()
                        .map(oid -> {
                            var perustiedot = organisaatioClient.getOrganisaatioPerustiedotCached(oid)
                                    .orElseGet(() -> UserDetailsUtil.createUnknownOrganisation(oid));
                            return new OrganisaatioMinimalDto(oid, perustiedot.getOrganisaatiotyypit(), perustiedot.getNimi());
                        })
                        .collect(toList())
                );
            }
            return dto;
        }).collect(toSet());
    }
}
