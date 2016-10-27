package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.dto.KayttoOikeusHistoriaDto;
import fi.vm.sade.kayttooikeus.dto.KayttoOikeusRyhmaDto;
import fi.vm.sade.kayttooikeus.dto.PalveluKayttoOikeusDto;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.*;
import fi.vm.sade.kayttooikeus.repositories.dto.ExpiringKayttoOikeusDto;
import fi.vm.sade.kayttooikeus.service.KayttoOikeusService;
import fi.vm.sade.kayttooikeus.service.LdapSynchronization;
import fi.vm.sade.kayttooikeus.service.LocalizationService;
import fi.vm.sade.kayttooikeus.service.dto.KayttoOikeusDto;
import fi.vm.sade.kayttooikeus.service.dto.KayttoOikeusRyhmaModifyDto;
import fi.vm.sade.kayttooikeus.service.dto.MyonnettyKayttoOikeusDto;
import fi.vm.sade.kayttooikeus.service.dto.PalveluRoooliDto;
import fi.vm.sade.kayttooikeus.service.exception.InvalidKayttoOikeusException;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.kayttooikeus.util.AccessRightManagementUtils;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloPerustietoDto;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KayttoOikeusServiceImpl extends AbstractService implements KayttoOikeusService {
    private KayttoOikeusRyhmaRepository kayttoOikeusRyhmaRepository;
    private KayttoOikeusRepository kayttoOikeusRepository;
    private LocalizationService localizationService;
    private OrikaBeanMapper mapper;
    private AccessRightManagementUtils accessRightManagementUtils;
    private KayttoOikeusRyhmaMyontoViiteRepository kayttoOikeusRyhmaMyontoViiteRepository;
    private MyonnettyKayttoOikeusRyhmaTapahtumaRepository myonnettyKayttoOikeusRyhmaTapahtumaRepository;
    private KayttoOikeusRyhmaTapahtumaHistoriaRepository kayttoOikeusRyhmaTapahtumaHistoriaRepository;
    private PalveluRepository palveluRepository;
    private OrganisaatioViiteRepository organisaatioViiteRepository;
    private TextGroupRepository textGroupRepository;
    private OppijanumerorekisteriClient oppijanumerorekisteriClient;
    private LdapSynchronization ldapSynchronization;

    @Autowired
    public KayttoOikeusServiceImpl(KayttoOikeusRyhmaRepository kayttoOikeusRyhmaRepository,
                                   KayttoOikeusRepository kayttoOikeusRepository,
                                   LocalizationService localizationService,
                                   OrikaBeanMapper mapper,
                                   AccessRightManagementUtils accessRightManagementUtils,
                                   KayttoOikeusRyhmaMyontoViiteRepository kayttoOikeusRyhmaMyontoViiteRepository,
                                   MyonnettyKayttoOikeusRyhmaTapahtumaRepository myonnettyKayttoOikeusRyhmaTapahtumaRepository,
                                   KayttoOikeusRyhmaTapahtumaHistoriaRepository kayttoOikeusRyhmaTapahtumaHistoriaRepository,
                                   PalveluRepository palveluRepository,
                                   OrganisaatioViiteRepository organisaatioViiteRepository,
                                   TextGroupRepository textGroupRepository,
                                   OppijanumerorekisteriClient oppijanumerorekisteriClient,
                                   LdapSynchronization ldapSynchronization) {
        this.kayttoOikeusRyhmaRepository = kayttoOikeusRyhmaRepository;
        this.kayttoOikeusRepository = kayttoOikeusRepository;
        this.localizationService = localizationService;
        this.mapper = mapper;
        this.accessRightManagementUtils = accessRightManagementUtils;
        this.kayttoOikeusRyhmaMyontoViiteRepository = kayttoOikeusRyhmaMyontoViiteRepository;
        this.myonnettyKayttoOikeusRyhmaTapahtumaRepository = myonnettyKayttoOikeusRyhmaTapahtumaRepository;
        this.kayttoOikeusRyhmaTapahtumaHistoriaRepository = kayttoOikeusRyhmaTapahtumaHistoriaRepository;
        this.palveluRepository = palveluRepository;
        this.organisaatioViiteRepository = organisaatioViiteRepository;
        this.textGroupRepository = textGroupRepository;
        this.oppijanumerorekisteriClient = oppijanumerorekisteriClient;
        this.ldapSynchronization = ldapSynchronization;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<KayttoOikeusRyhmaDto> listAllKayttoOikeusRyhmas() {
        return mapper.mapAsList(kayttoOikeusRyhmaRepository.listAll(), KayttoOikeusRyhmaDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PalveluKayttoOikeusDto> listKayttoOikeusByPalvelu(String palveluName) {
        return localizationService.localize(kayttoOikeusRepository.listKayttoOikeusByPalvelu(palveluName));
    }

    @Override
    @Transactional(readOnly = true)
    public List<KayttoOikeusHistoriaDto> listMyonnettyKayttoOikeusHistoriaForCurrentUser() {
        return localizationService.localize(kayttoOikeusRepository.listMyonnettyKayttoOikeusHistoriaForHenkilo(getCurrentUserOid()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpiringKayttoOikeusDto> findToBeExpiringMyonnettyKayttoOikeus(LocalDate at, Period... expirationPeriods) {
        return localizationService.localize(kayttoOikeusRepository.findSoonToBeExpiredTapahtumas(at, expirationPeriods));
    }

    @Override
    @Transactional(readOnly = true)
    public List<KayttoOikeusRyhmaDto> listPossibleRyhmasByOrganization(String organisaatioOid) {
        List<KayttoOikeusRyhma> allRyhmas = kayttoOikeusRyhmaRepository.listAll();
        accessRightManagementUtils.parseRyhmaLimitationsBasedOnOrgOid(organisaatioOid, allRyhmas);
        return mapper.mapAsList(allRyhmas, KayttoOikeusRyhmaDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MyonnettyKayttoOikeusDto> listMyonnettyKayttoOikeusRyhmasMergedWithHenkilos(String henkiloOid, String organisaatioOid, String myontajaOid) {
        List<KayttoOikeusRyhma> allRyhmas;
        /* The list of groups that can be granted must be checked
         * from the granting person's limitation list, if the granting
         * person has any limitations, if not then all groups are listed
         */

        List<Long> slaveIds = accessRightManagementUtils.getGrantableKayttooikeusRyhmas(myontajaOid);

        if (!CollectionUtils.isEmpty(slaveIds)) {
            allRyhmas = kayttoOikeusRyhmaRepository.findByIdList(slaveIds);
        } else {
            allRyhmas = kayttoOikeusRyhmaRepository.listAll();
        }

        /* If groups have limitations based on organization restrictions, those
         * groups must be removed from the list since it confuses the user as UI
         * can't know these limitations and the error message doesn't really help
         */
        accessRightManagementUtils.parseRyhmaLimitationsBasedOnOrgOid(organisaatioOid, allRyhmas);

        if (!allRyhmas.isEmpty()) {
            List<MyonnettyKayttoOikeusDto> henkilosKORs = localizationService.localize(myonnettyKayttoOikeusRyhmaTapahtumaRepository.findByHenkiloInOrganisaatio(henkiloOid, organisaatioOid));
            List<MyonnettyKayttoOikeusDto> results = new ArrayList<>();
            allRyhmas.forEach(kayttoOikeusRyhma -> {
                for (MyonnettyKayttoOikeusDto henkilosKOR : henkilosKORs) {
                    if (henkilosKOR.getRyhmaId().equals(kayttoOikeusRyhma.getId())) {
                        henkilosKOR.setSelected(true);
                        results.add(henkilosKOR);
                        break;
                    }
                }
            });
            return results;
        }

        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MyonnettyKayttoOikeusDto> listMyonnettyKayttoOikeusRyhmasByHenkiloAndOrganisaatio(String henkiloOid, String organisaatioOid) {

        List<MyonnettyKayttoOikeusDto> all = myonnettyKayttoOikeusRyhmaTapahtumaRepository.findByHenkiloInOrganisaatio(henkiloOid, organisaatioOid);
        /* History data must be fetched also since it's additional information for admin users
         * if they need to solve possible conflicts with users' access rights
         */
        List<MyonnettyKayttoOikeusDto> ryhmaTapahtumaHistorias = kayttoOikeusRyhmaTapahtumaHistoriaRepository.findByHenkiloInOrganisaatio(henkiloOid, organisaatioOid);
        all.addAll(ryhmaTapahtumaHistorias);

        List<String> kasittelijaOids = all.stream().map(MyonnettyKayttoOikeusDto::getKasittelijaOid).distinct().collect(Collectors.toList());
        Map<String, String> kasittelijaNimet = oppijanumerorekisteriClient.getHenkilonPerustiedot(kasittelijaOids)
                .stream()
                .collect(Collectors.toMap(HenkiloPerustietoDto::getOidhenkilo, t -> t.getSukunimi() + ", " + t.getKutsumanimi()));
        all.forEach(myonnettyKayttoOikeusDTO -> myonnettyKayttoOikeusDTO.setKasittelijaNimi(kasittelijaNimet.get(myonnettyKayttoOikeusDTO.getKasittelijaOid())));

        return localizationService.localize(all);
    }

    @Override
    @Transactional(readOnly = true)
    public KayttoOikeusRyhmaDto findKayttoOikeusRyhma(Long id) {
        return mapper.map(kayttoOikeusRyhmaRepository.findById(id), KayttoOikeusRyhmaDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KayttoOikeusRyhmaDto> findSubRyhmasByMasterRyhma(Long id) {
        List<Long> slaveIds = kayttoOikeusRyhmaMyontoViiteRepository.getSlaveIdsByMasterIds(Collections.singletonList(id));
        if(slaveIds != null && !slaveIds.isEmpty()) {
            return mapper.mapAsList(kayttoOikeusRyhmaRepository.findByIdList(slaveIds), KayttoOikeusRyhmaDto.class);
        }
        return new ArrayList<>();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PalveluRoooliDto> findPalveluRoolisByKayttoOikeusRyhma(Long id) {
        List<Long> kosIds = kayttoOikeusRepository.findByKayttoOikeusRyhmaIds(id);
        List<Palvelu> palvelus = palveluRepository.findByKayttoOikeusIds(kosIds);
        return accessRightManagementUtils.createPalveluRooliDTO(palvelus);
    }

    @Override
    @Transactional
    public KayttoOikeusRyhmaDto createKayttoOikeusRyhma(KayttoOikeusRyhmaModifyDto uusiRyhma) {
        TextGroup tg = createRyhmaDescription(uusiRyhma);

        // Checks if the Finnish name for access right group has been used
        if (kayttoOikeusRyhmaRepository.ryhmaNameFiExists(uusiRyhma.getRyhmaNameFi())) {
            throw new IllegalArgumentException("Group name already in use");
        }

        KayttoOikeusRyhma kor = new KayttoOikeusRyhma();
        kor.setName(uusiRyhma.getRyhmaNameFi() + "_" + System.currentTimeMillis());
        kor.setDescription(tg);

        if (uusiRyhma.getRooliRajoite() != null) {
            kor.setRooliRajoite(uusiRyhma.getRooliRajoite());
        }

        uusiRyhma.getPalvelutRoolit().forEach(palveluRoooliDto -> kor.getKayttoOikeus().add(createKayttoOikeus(kor, palveluRoooliDto)));

        final Set<KayttoOikeus> kayttoOikeusHashSet = accessRightManagementUtils.bindToNonTransientInstances(kor);
        kor.getKayttoOikeus().clear();
        kor.getKayttoOikeus().addAll(kayttoOikeusHashSet);

        KayttoOikeusRyhma createdRyhma = kayttoOikeusRyhmaRepository.insert(kor);

        // Organization limitation must be set only if the Organizatio OIDs are defined
        if (!CollectionUtils.isEmpty(uusiRyhma.getOrganisaatioTyypit())) {
            for (String orgTyyppi : uusiRyhma.getOrganisaatioTyypit()) {
                OrganisaatioViite ov = new OrganisaatioViite();
                ov.setOrganisaatioTyyppi(orgTyyppi);
                kor.addOrganisaatioViite(ov);
                organisaatioViiteRepository.insert(ov);
            }
        }

        // Group limitations must be set only if slave IDs have been defined
        if (uusiRyhma.getSlaveIds() != null && !uusiRyhma.getSlaveIds().isEmpty()) {
            checkAndInsertSlaveGroups(uusiRyhma, createdRyhma);
        }

        return mapper.map(createdRyhma, KayttoOikeusRyhmaDto.class);
    }

    private void checkAndInsertSlaveGroups(KayttoOikeusRyhmaModifyDto ryhmaData, KayttoOikeusRyhma koRyhma) {
        // If the given group has another master group ID in its slave IDs list
        // that MUST be prevented since it would create cyclic relationship!!

        if (kayttoOikeusRyhmaMyontoViiteRepository.checkCyclicMyontoViite(koRyhma.getId(), ryhmaData.getSlaveIds())) {
            throw new InvalidKayttoOikeusException("Cyclic master-slave dependency");
        }

        for (Long slaveId : ryhmaData.getSlaveIds()) {
            KayttoOikeusRyhmaMyontoViite myontoViite = new KayttoOikeusRyhmaMyontoViite();
            myontoViite.setMasterId(koRyhma.getId());
            myontoViite.setSlaveId(slaveId);

            kayttoOikeusRyhmaMyontoViiteRepository.insert(myontoViite);
        }
    }

    @Override
    @Transactional
    public KayttoOikeusDto createKayttoOikeus(KayttoOikeusDto kayttoOikeus) {
        if (kayttoOikeus.getRooli() == null) {
            throw new InvalidKayttoOikeusException("Rooli may not be null");
        }
        KayttoOikeus ko = mapper.map(kayttoOikeus, KayttoOikeus.class);
        TextGroup txtGroup = textGroupRepository.insert(ko.getTextGroup());
        ko.setTextGroup(txtGroup);

        ko = accessRightManagementUtils.createKayttoOikeus(ko);
        return mapper.map(ko, KayttoOikeusDto.class);
    }

    @Override
    @Transactional
    public KayttoOikeusRyhmaDto updateKayttoOikeusForKayttoOikeusRyhma(Long id, KayttoOikeusRyhmaModifyDto ryhmaData) {
        KayttoOikeusRyhma koRyhma = kayttoOikeusRyhmaRepository.findById(id);
        if (koRyhma == null) {
            throw new IllegalArgumentException("Could not find group with id: " + id);
        }

        if (hasMissingGroupNames(ryhmaData)) {
            throw new IllegalArgumentException("Missing group names");
        }

        // UI must always send the list of group names even if they don't change!!
        if (koRyhma.getDescription() == null) {
            koRyhma.setDescription(createRyhmaDescription(ryhmaData));
        } else {
            TextGroup description = koRyhma.getDescription();
            Set<Text> ryhmaNames = description.getTexts();

            updateOrAddTextForLang(ryhmaNames, "FI", ryhmaData.getRyhmaNameFi());
            updateOrAddTextForLang(ryhmaNames, "SV", ryhmaData.getRyhmaNameSv());
            updateOrAddTextForLang(ryhmaNames, "EN", ryhmaData.getRyhmaNameEn());
        }

        for (KayttoOikeusRyhmaMyontoViite viite : kayttoOikeusRyhmaMyontoViiteRepository.getMyontoViites(koRyhma.getId())) {
            kayttoOikeusRyhmaMyontoViiteRepository.delete(viite);
        }

        if (ryhmaData.getSlaveIds() != null && !ryhmaData.getSlaveIds().isEmpty()) {
            checkAndInsertSlaveGroups(ryhmaData, koRyhma);
        }


        koRyhma.setRooliRajoite(ryhmaData.getRooliRajoite());
        // UI must always send the list of organization restrictions even if they don't change!!
        if (koRyhma.getOrganisaatioViite() != null && !koRyhma.getOrganisaatioViite().isEmpty()) {
            for (OrganisaatioViite orgV : koRyhma.getOrganisaatioViite()) {
                organisaatioViiteRepository.delete(orgV);
            }
            koRyhma.removeAllOrganisaatioViites();
        }

        Optional.ofNullable(ryhmaData.getOrganisaatioTyypit())
                .orElseGet(Collections::emptyList)
                .forEach(orgTyyppi -> {
                    OrganisaatioViite ov = new OrganisaatioViite();
                    ov.setOrganisaatioTyyppi(orgTyyppi);
                    koRyhma.addOrganisaatioViite(ov);
                });

        Set<KayttoOikeus> givenKOs = new HashSet<>();
        for (PalveluRoooliDto prDto : ryhmaData.getPalvelutRoolit()) {
            List<Palvelu> palvelus = palveluRepository.findByName(prDto.getPalveluName());
            if (palvelus.isEmpty()) {
                throw new NotFoundException("palvelu not found by name : " + prDto.getPalveluName());
            }
            Palvelu palvelu = palvelus.get(0);

            KayttoOikeus tempKo = new KayttoOikeus();
            tempKo.setPalvelu(palvelu);
            tempKo.setRooli(prDto.getRooli());
            tempKo = kayttoOikeusRepository.findByRooliAndPalvelu(tempKo);
            givenKOs.add(tempKo);
        }

        // removed KayttoOikeus objects that haven't been passed in the request
        koRyhma.getKayttoOikeus().stream()
                .filter(kayttoOikeus -> !givenKOs.contains(kayttoOikeus))
                .collect(Collectors.toList())
                .forEach(koRyhma.getKayttoOikeus()::remove);
        koRyhma.getKayttoOikeus().addAll(givenKOs);

        ldapSynchronization.updateAccessRightGroup(id);
        return mapper.map(koRyhma, KayttoOikeusRyhmaDto.class);
    }

    private void updateOrAddTextForLang(Set<Text> ryhmaNames, String lang, String newText) {
        Optional<Text> name = ryhmaNames.stream().filter(text -> text.getLang().equals(lang)).findFirst();
        if (name.isPresent()) {
            name.get().setText(newText);
        }else{
            ryhmaNames.add(new Text(null, "FI", newText));
        }
    }

    private KayttoOikeus createKayttoOikeus(KayttoOikeusRyhma kor, PalveluRoooliDto palveluRoooliDto) {
        Palvelu tempPalvelu = new Palvelu();
        KayttoOikeus tempKO = new KayttoOikeus();
        tempPalvelu.setName(palveluRoooliDto.getPalveluName());
        tempKO.setRooli(palveluRoooliDto.getRooli());
        tempKO.setPalvelu(tempPalvelu);
        tempKO.getKayttooikeusRyhmas().add(kor);
        return tempKO;
    }

    private boolean hasMissingGroupNames(KayttoOikeusRyhmaModifyDto uusiRyhma) {
        return uusiRyhma.getRyhmaNameFi() == null || uusiRyhma.getRyhmaNameEn() == null || uusiRyhma.getRyhmaNameSv() == null;
    }

    private TextGroup createRyhmaDescription(KayttoOikeusRyhmaModifyDto uusiRyhma) {
        if (hasMissingGroupNames(uusiRyhma)) {
            throw new IllegalArgumentException("Missing group names");
        }

        TextGroup tg = new TextGroup();
        tg.addText(new Text(tg, "FI", uusiRyhma.getRyhmaNameFi()));
        tg.addText(new Text(tg, "SV", uusiRyhma.getRyhmaNameSv()));
        tg.addText(new Text(tg, "EN", uusiRyhma.getRyhmaNameEn()));
        return tg;
    }
}
