package fi.vm.sade.kayttooikeus.service.impl;

import com.querydsl.core.Tuple;
import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.dto.*;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.*;
import fi.vm.sade.kayttooikeus.repositories.dto.ExpiringKayttoOikeusDto;
import fi.vm.sade.kayttooikeus.service.KayttoOikeusService;
import fi.vm.sade.kayttooikeus.service.LocalizationService;
import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;
import fi.vm.sade.kayttooikeus.service.exception.InvalidKayttoOikeusException;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient.Mode;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioPerustieto;
import fi.vm.sade.kayttooikeus.util.UserDetailsUtil;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloPerustietoDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.springframework.util.CollectionUtils.isEmpty;
import fi.vm.sade.kayttooikeus.service.LdapSynchronizationService;

@Service
public class KayttoOikeusServiceImpl extends AbstractService implements KayttoOikeusService {
    public static final String FI = "FI";
    public static final String SV = "SV";
    public static final String EN = "EN";

    private final String rootOrganizationOid;
    private final String groupOrganizationId;
    private OrganisaatioClient organisaatioClient;
    private KayttoOikeusRyhmaRepository kayttoOikeusRyhmaRepository;
    private KayttoOikeusRepository kayttoOikeusRepository;
    private LocalizationService localizationService;
    private OrikaBeanMapper mapper;
    private KayttoOikeusRyhmaMyontoViiteRepository kayttoOikeusRyhmaMyontoViiteRepository;
    private MyonnettyKayttoOikeusRyhmaTapahtumaRepository myonnettyKayttoOikeusRyhmaTapahtumaRepository;
    private KayttoOikeusRyhmaTapahtumaHistoriaRepository kayttoOikeusRyhmaTapahtumaHistoriaRepository;
    private PalveluRepository palveluRepository;
    private OrganisaatioViiteRepository organisaatioViiteRepository;
    private LdapSynchronizationService ldapSynchronizationService;
    private PermissionCheckerService permissionCheckerService;

    private final OppijanumerorekisteriClient oppijanumerorekisteriClient;

    @Autowired
    public KayttoOikeusServiceImpl(KayttoOikeusRyhmaRepository kayttoOikeusRyhmaRepository,
                                   KayttoOikeusRepository kayttoOikeusRepository,
                                   LocalizationService localizationService,
                                   OrikaBeanMapper mapper,
                                   KayttoOikeusRyhmaMyontoViiteRepository kayttoOikeusRyhmaMyontoViiteRepository,
                                   MyonnettyKayttoOikeusRyhmaTapahtumaRepository myonnettyKayttoOikeusRyhmaTapahtumaRepository,
                                   KayttoOikeusRyhmaTapahtumaHistoriaRepository kayttoOikeusRyhmaTapahtumaHistoriaRepository,
                                   PalveluRepository palveluRepository,
                                   OrganisaatioViiteRepository organisaatioViiteRepository,
                                   LdapSynchronizationService ldapSynchronizationService,
                                   OrganisaatioClient organisaatioClient,
                                   CommonProperties commonProperties,
                                   OppijanumerorekisteriClient oppijanumerorekisteriClient,
                                   PermissionCheckerService permissionCheckerService) {
        this.kayttoOikeusRyhmaRepository = kayttoOikeusRyhmaRepository;
        this.kayttoOikeusRepository = kayttoOikeusRepository;
        this.localizationService = localizationService;
        this.mapper = mapper;
        this.kayttoOikeusRyhmaMyontoViiteRepository = kayttoOikeusRyhmaMyontoViiteRepository;
        this.myonnettyKayttoOikeusRyhmaTapahtumaRepository = myonnettyKayttoOikeusRyhmaTapahtumaRepository;
        this.kayttoOikeusRyhmaTapahtumaHistoriaRepository = kayttoOikeusRyhmaTapahtumaHistoriaRepository;
        this.palveluRepository = palveluRepository;
        this.organisaatioViiteRepository = organisaatioViiteRepository;
        this.ldapSynchronizationService = ldapSynchronizationService;
        this.organisaatioClient = organisaatioClient;
        this.rootOrganizationOid = commonProperties.getRootOrganizationOid();
        this.groupOrganizationId = commonProperties.getGroupOrganizationId();
        this.oppijanumerorekisteriClient = oppijanumerorekisteriClient;
        this.permissionCheckerService = permissionCheckerService;
    }

    @Override
    public KayttoOikeusDto findKayttoOikeusById(long kayttoOikeusId) {
        return mapper.map(kayttoOikeusRepository.findById(kayttoOikeusId).orElseThrow(()
                -> new NotFoundException("kayttöoikeus not found")), KayttoOikeusDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KayttoOikeusRyhmaDto> listAllKayttoOikeusRyhmas() {
        return localizationService.localize(findAllKayttoOikeusRyhmasAsDtos());
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
    public Map<String, List<Integer>> findKayttooikeusryhmatAndOrganisaatioByHenkiloOid(String henkiloOid)  {
        List<Tuple> results = this.kayttoOikeusRyhmaRepository.findOrganisaatioOidAndRyhmaIdByHenkiloOid(henkiloOid);

        HashMap<String, List<Integer>> kayttooikeusRyhmasByOrganisation = new HashMap<>();
        QOrganisaatioHenkilo organisaatioHenkilo = QOrganisaatioHenkilo.organisaatioHenkilo;
        QMyonnettyKayttoOikeusRyhmaTapahtuma myonnettyKayttoOikeusRyhmaTapahtuma
                = QMyonnettyKayttoOikeusRyhmaTapahtuma.myonnettyKayttoOikeusRyhmaTapahtuma;
        for(Tuple result : results) {
            String organisaatioOid = result.get(organisaatioHenkilo.organisaatioOid);
            Integer ryhmaId = Optional.ofNullable(result.get(myonnettyKayttoOikeusRyhmaTapahtuma.kayttoOikeusRyhma.id))
                    .orElseThrow(() -> new NullPointerException("null_ryhma_id")).intValue();

            List<Integer> ryhmasInOrganisaatio = kayttooikeusRyhmasByOrganisation.get(organisaatioOid);
            if(ryhmasInOrganisaatio == null) {
                ryhmasInOrganisaatio = new ArrayList<>();
                kayttooikeusRyhmasByOrganisation.put(organisaatioOid, ryhmasInOrganisaatio);
            }
            ryhmasInOrganisaatio.add(ryhmaId);
        }
        return kayttooikeusRyhmasByOrganisation;
    }

    @Override
    @Transactional(readOnly = true)
    public List<KayttoOikeusRyhmaDto> listPossibleRyhmasByOrganization(String organisaatioOid) {
        return localizationService.localize(getRyhmasWithoutOrganizationLimitations(
                organisaatioOid, findAllKayttoOikeusRyhmasAsDtos()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MyonnettyKayttoOikeusDto> listMyonnettyKayttoOikeusRyhmasMergedWithHenkilos(String henkiloOid, String organisaatioOid, String myontajaOid) {
        List<KayttoOikeusRyhmaDto> allRyhmas = getGrantableRyhmasWithoutOrgLimitations(organisaatioOid, myontajaOid);
        if (allRyhmas.isEmpty()){
            return Collections.emptyList();
        }

        List<MyonnettyKayttoOikeusDto> kayttoOikeusForHenkilo = myonnettyKayttoOikeusRyhmaTapahtumaRepository.findByHenkiloInOrganisaatio(henkiloOid, organisaatioOid);
        List<MyonnettyKayttoOikeusDto> all = allRyhmas.stream()
                .map(kayttoOikeusRyhmaDto -> {
            MyonnettyKayttoOikeusDto dto = new MyonnettyKayttoOikeusDto();
            dto.setRyhmaId(kayttoOikeusRyhmaDto.getId());
            if (kayttoOikeusRyhmaDto.getDescription() != null) {
                dto.setRyhmaNamesId(kayttoOikeusRyhmaDto.getDescription().getId());
            }
            dto.setSelected(false);
            kayttoOikeusForHenkilo.stream()
                    .filter(myonnettyKayttoOikeusDto -> myonnettyKayttoOikeusDto.getRyhmaId().equals(dto.getRyhmaId()))
                    .findFirst().ifPresent(myonnettyKayttoOikeusDto -> {
                        dto.setMyonnettyTapahtumaId(myonnettyKayttoOikeusDto.getMyonnettyTapahtumaId());
                        dto.setAlkuPvm(myonnettyKayttoOikeusDto.getAlkuPvm());
                        dto.setVoimassaPvm(myonnettyKayttoOikeusDto.getVoimassaPvm());
                        dto.setSelected(true);
                    });
            return dto;
        }).collect(Collectors.toList());

        return localizationService.localize(all);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MyonnettyKayttoOikeusDto> listMyonnettyKayttoOikeusRyhmasByHenkiloAndOrganisaatio(String henkiloOid, String organisaatioOid) {

        List<MyonnettyKayttoOikeusDto> all = myonnettyKayttoOikeusRyhmaTapahtumaRepository.findByHenkiloInOrganisaatio(henkiloOid, organisaatioOid);
        /* History data must be fetched also since it's additional information for admin users
         * if they need to solve possible conflicts with users' access rights
         */
        List<MyonnettyKayttoOikeusDto> ryhmaTapahtumaHistorias = kayttoOikeusRyhmaTapahtumaHistoriaRepository
                .findByHenkiloInOrganisaatio(henkiloOid, organisaatioOid);
        all.addAll(ryhmaTapahtumaHistorias);
        return this.mapKasittelijaNimiToMyonnettyKayttooikeusDto(this.localizationService.localize(all));
    }

    private List<MyonnettyKayttoOikeusDto> mapKasittelijaNimiToMyonnettyKayttooikeusDto(
            List<MyonnettyKayttoOikeusDto> myonnettyKayttoOikeusDtoList) {
        List<HenkiloPerustietoDto> henkiloPerustiedotDtos = this.oppijanumerorekisteriClient.getHenkilonPerustiedot(myonnettyKayttoOikeusDtoList.stream()
                .map(MyonnettyKayttoOikeusDto::getKasittelijaOid).collect(Collectors.toList()));
        return myonnettyKayttoOikeusDtoList.stream()
                .map(myonnettyKayttoOikeusDto -> {
                    HenkiloPerustietoDto kasittelija = henkiloPerustiedotDtos.stream()
                            .filter(henkiloPerustietoDto -> Objects.equals(henkiloPerustietoDto.getOidHenkilo(), henkiloPerustietoDto.getOidHenkilo()))
                            .findFirst().orElseThrow(() -> new NotFoundException("Kasittelija not found with oid " + myonnettyKayttoOikeusDto.getKasittelijaOid()));
                    myonnettyKayttoOikeusDto.setKasittelijaNimi(kasittelija.getKutsumanimi() + " " + kasittelija.getSukunimi());
                    return myonnettyKayttoOikeusDto;
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public KayttoOikeusRyhmaDto findKayttoOikeusRyhma(long id) {
        KayttoOikeusRyhmaDto ryhma = mapper.map(kayttoOikeusRyhmaRepository.findByRyhmaId(id).orElseThrow(()
                -> new NotFoundException("kayttooikeusryhma not found")), KayttoOikeusRyhmaDto.class);
        return localizationService.localize(ryhma);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KayttoOikeusRyhmaDto> findSubRyhmasByMasterRyhma(long id) {
        List<Long> slaveIds = kayttoOikeusRyhmaMyontoViiteRepository.getSlaveIdsByMasterIds(Collections.singletonList(id));
        return localizationService.localize(kayttoOikeusRyhmaRepository.findByIdList(slaveIds));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PalveluRooliDto> findPalveluRoolisByKayttoOikeusRyhma(long ryhmaId) {
        return localizationService.localize(kayttoOikeusRepository.findPalveluRoolitByKayttoOikeusRyhmaId(ryhmaId));
    }

    @Override
    @Transactional(readOnly = true)
    public RyhmanHenkilotDto findHenkilotByKayttoOikeusRyhma(long id) {
        RyhmanHenkilotDto resp = new RyhmanHenkilotDto();
        resp.setPersonOids(kayttoOikeusRepository.findHenkilosByRyhma(id));
        return resp;
    }

    @Override
    @Transactional
    public long createKayttoOikeusRyhma(KayttoOikeusRyhmaModifyDto uusiRyhma) {
        if (kayttoOikeusRyhmaRepository.ryhmaNameFiExists(uusiRyhma.getRyhmaName().get(FI))) {
            throw new IllegalArgumentException("Group name already in use");
        }

        KayttoOikeusRyhma kayttoOikeusRyhma = new KayttoOikeusRyhma();
        kayttoOikeusRyhma.setName(uusiRyhma.getRyhmaName().get(FI) + "_" + System.currentTimeMillis());
        TextGroup tg = createRyhmaDescription(uusiRyhma.getRyhmaName());
        kayttoOikeusRyhma.setDescription(tg);
        kayttoOikeusRyhma.setRooliRajoite(uusiRyhma.getRooliRajoite());

        kayttoOikeusRyhma.getKayttoOikeus().addAll(uusiRyhma.getPalvelutRoolit().stream()
                .map(palveluRoooliDto ->
                        ofNullable(kayttoOikeusRepository.findByRooliAndPalvelu(palveluRoooliDto.getRooli(),
                                palveluRoooliDto.getPalveluName()))
                    .orElseGet(() -> kayttoOikeusRepository.persist(new KayttoOikeus(palveluRoooliDto.getRooli(),
                            palveluRepository.findByName(palveluRoooliDto.getPalveluName())
                                    .orElseThrow(() -> new NotFoundException("palvelu not found"))
                    )))).collect(toList()));

        kayttoOikeusRyhma = kayttoOikeusRyhmaRepository.persist(kayttoOikeusRyhma);

        // Organization limitation must be set only if the Organizatio OIDs are defined
        if (!isEmpty(uusiRyhma.getOrganisaatioTyypit())) {
            for (String orgTyyppi : uusiRyhma.getOrganisaatioTyypit()) {
                OrganisaatioViite ov = new OrganisaatioViite();
                ov.setOrganisaatioTyyppi(orgTyyppi);
                kayttoOikeusRyhma.addOrganisaatioViite(ov);
                organisaatioViiteRepository.persist(ov);
            }
        }

        // Group limitations must be set only if slave IDs have been defined
        if (!isEmpty(uusiRyhma.getSlaveIds())) {
            checkAndInsertSlaveGroups(uusiRyhma, kayttoOikeusRyhma);
        }

        return kayttoOikeusRyhma.getId();
    }

    @Override
    @Transactional
    public long createKayttoOikeus(KayttoOikeusCreateDto kayttoOikeus) {
        KayttoOikeus ko = mapper.map(kayttoOikeus, KayttoOikeus.class);
        ko.setPalvelu(palveluRepository.findByName(kayttoOikeus.getPalveluName()).orElseThrow(()
                -> new NotFoundException("palvelu not found")));
        return kayttoOikeusRepository.persist(ko).getId();
    }

    @Override
    @Transactional
    public void updateKayttoOikeusForKayttoOikeusRyhma(long id, KayttoOikeusRyhmaModifyDto ryhmaData) {
        KayttoOikeusRyhma kayttoOikeusRyhma = kayttoOikeusRyhmaRepository.findById(id).orElseThrow(()
                -> new NotFoundException("kayttooikeusryhma not found"));

        // UI must always send the list of group names even if they don't change!!
        setRyhmaDescription(ryhmaData, kayttoOikeusRyhma);

        for (KayttoOikeusRyhmaMyontoViite viite : kayttoOikeusRyhmaMyontoViiteRepository.getMyontoViites(kayttoOikeusRyhma.getId())) {
            kayttoOikeusRyhmaMyontoViiteRepository.remove(viite);
        }

        if (!isEmpty(ryhmaData.getSlaveIds())) {
            checkAndInsertSlaveGroups(ryhmaData, kayttoOikeusRyhma);
        }

        kayttoOikeusRyhma.setRooliRajoite(ryhmaData.getRooliRajoite());
        // UI must always send the list of organization restrictions even if they don't change!!
        setRyhmaOrganisaatioViites(ryhmaData, kayttoOikeusRyhma);

        setKayttoOikeusRyhmas(ryhmaData, kayttoOikeusRyhma);

        ldapSynchronizationService.updateKayttoOikeusRyhma(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KayttoOikeusRyhmaDto> findKayttoOikeusRyhmasByKayttoOikeusList(Map<String, String> kayttoOikeusList) {
        List<Long> kayttoOikeusIds = kayttoOikeusList.keySet().stream()
                .map(key -> kayttoOikeusRepository.findByRooliAndPalvelu(kayttoOikeusList.get(key), key))
                .filter(Objects::nonNull).map(IdentifiableAndVersionedEntity::getId).collect(Collectors.toList());

        List<KayttoOikeusRyhmaDto> ryhmas = addOrganisaatioViitteesToRyhmas(kayttoOikeusRyhmaRepository
                .findKayttoOikeusRyhmasByKayttoOikeusIds(kayttoOikeusIds));
        return localizationService.localize(ryhmas);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthorizationDataDto findAuthorizationDataByOid(String oid) {
        AccessRightListTypeDto accessRights = new AccessRightListTypeDto(myonnettyKayttoOikeusRyhmaTapahtumaRepository
                .findValidAccessRightsByOid(oid));
        GroupListTypeDto groups = new GroupListTypeDto(myonnettyKayttoOikeusRyhmaTapahtumaRepository
                .findValidGroupsByHenkilo(oid));
        return new AuthorizationDataDto(accessRights, groups);
    }

    private void setKayttoOikeusRyhmas(KayttoOikeusRyhmaModifyDto ryhmaData, KayttoOikeusRyhma kayttoOikeusRyhma) {
        Set<KayttoOikeus> givenKOs = new HashSet<>();
        for (PalveluRooliDto prDto : ryhmaData.getPalvelutRoolit()) {
            Palvelu palvelu = palveluRepository.findByName(prDto.getPalveluName()).orElseThrow(()
                    -> new NotFoundException("palvelu not found"));
            KayttoOikeus tempKo = kayttoOikeusRepository.findByRooliAndPalvelu(prDto.getRooli(), palvelu.getName());
            givenKOs.add(tempKo);
        }

        // removed KayttoOikeus objects that haven't been passed in the request
        kayttoOikeusRyhma.getKayttoOikeus().stream()
                .filter(kayttoOikeus -> !givenKOs.contains(kayttoOikeus))
                .collect(toList())
                .forEach(kayttoOikeusRyhma.getKayttoOikeus()::remove);
        kayttoOikeusRyhma.getKayttoOikeus().addAll(givenKOs);
    }

    private void setRyhmaOrganisaatioViites(KayttoOikeusRyhmaModifyDto ryhmaData, KayttoOikeusRyhma kayttoOikeusRyhma) {
        if (!isEmpty(kayttoOikeusRyhma.getOrganisaatioViite())) {
            for (OrganisaatioViite orgV : kayttoOikeusRyhma.getOrganisaatioViite()) {
                organisaatioViiteRepository.remove(orgV);
            }
            kayttoOikeusRyhma.removeAllOrganisaatioViites();
        }

        ofNullable(ryhmaData.getOrganisaatioTyypit())
                .orElseGet(Collections::emptyList)
                .forEach(orgTyyppi -> {
                    OrganisaatioViite ov = new OrganisaatioViite();
                    ov.setOrganisaatioTyyppi(orgTyyppi);
                    kayttoOikeusRyhma.addOrganisaatioViite(ov);
                });
    }

    private void setRyhmaDescription(KayttoOikeusRyhmaModifyDto ryhmaData, KayttoOikeusRyhma kayttoOikeusRyhma) {
        if (kayttoOikeusRyhma.getDescription() == null) {
            kayttoOikeusRyhma.setDescription(createRyhmaDescription(ryhmaData.getRyhmaName()));
        } else {
            TextGroup description = kayttoOikeusRyhma.getDescription();
            Set<Text> ryhmaNames = description.getTexts();

            updateOrAddTextForLang(ryhmaNames, FI, ryhmaData.getRyhmaName().get(FI));
            updateOrAddTextForLang(ryhmaNames, SV, ryhmaData.getRyhmaName().get(SV));
            updateOrAddTextForLang(ryhmaNames, EN, ryhmaData.getRyhmaName().get(EN));
        }
    }

    private List<KayttoOikeusRyhmaDto> addOrganisaatioViitteesToRyhmas(List<KayttoOikeusRyhmaDto> ryhmas){
        Map<Long, KayttoOikeusRyhmaDto> byIds = ryhmas.stream()
                .collect(Collectors.toMap(KayttoOikeusRyhmaDto::getId, Function.identity()));

        organisaatioViiteRepository.findByKayttoOikeusRyhmaIds(byIds.keySet())
                .forEach(fetched -> byIds.get(fetched.getKayttoOikeusRyhmaId())
                        .getOrganisaatioViite().add(fetched));

        return new ArrayList<>(byIds.values());
    }

    private List<KayttoOikeusRyhmaDto> findAllKayttoOikeusRyhmasAsDtos() {
        return addOrganisaatioViitteesToRyhmas(kayttoOikeusRyhmaRepository.listAll());
    }

    private List<KayttoOikeusRyhmaDto> getGrantableRyhmasWithoutOrgLimitations(String organisaatioOid, String myontajaOid) {
        List<Long> slaveIds =  kayttoOikeusRyhmaMyontoViiteRepository.getSlaveIdsByMasterIds(
                myonnettyKayttoOikeusRyhmaTapahtumaRepository.findMasterIdsByHenkilo(myontajaOid));

        List<KayttoOikeusRyhmaDto> allRyhmas = isEmpty(slaveIds) ?
                kayttoOikeusRyhmaRepository.listAll() : kayttoOikeusRyhmaRepository.findByIdList(slaveIds);
        return getRyhmasWithoutOrganizationLimitations(organisaatioOid,
                addOrganisaatioViitteesToRyhmas(allRyhmas));
    }

    private void checkAndInsertSlaveGroups(KayttoOikeusRyhmaModifyDto ryhmaData, KayttoOikeusRyhma koRyhma) {
        if (isEmpty(ryhmaData.getSlaveIds())) {
            return;
        }
        if (kayttoOikeusRyhmaMyontoViiteRepository.isCyclicMyontoViite(koRyhma.getId(), ryhmaData.getSlaveIds())) {
            throw new InvalidKayttoOikeusException("Cyclic master-slave dependency");
        }

        for (Long slaveId : ryhmaData.getSlaveIds()) {
            KayttoOikeusRyhmaMyontoViite myontoViite = new KayttoOikeusRyhmaMyontoViite();
            myontoViite.setMasterId(koRyhma.getId());
            myontoViite.setSlaveId(slaveId);
            kayttoOikeusRyhmaMyontoViiteRepository.persist(myontoViite);
        }
    }

    private void updateOrAddTextForLang(Set<Text> ryhmaNames, String lang, String newText) {
        Optional<Text> name = ryhmaNames.stream().filter(text -> text.getLang().equals(lang)).findFirst();
        if (name.isPresent()) {
            name.get().setText(newText);
        } else {
            ryhmaNames.add(new Text(null, lang, newText));
        }
    }

    private TextGroup createRyhmaDescription(TextGroupDto name) {
        TextGroup tg = new TextGroup();
        tg.addText(new Text(tg, FI, name.get(FI)));
        tg.addText(new Text(tg, SV, name.get(SV)));
        tg.addText(new Text(tg, EN, name.get(EN)));
        return tg;
    }

    private List<KayttoOikeusRyhmaDto> getRyhmasWithoutOrganizationLimitations(String organisaatioOid, List<KayttoOikeusRyhmaDto> allRyhmas) {
        boolean isOphOrganisation = organisaatioOid.equals(rootOrganizationOid);
        List<OrganisaatioPerustieto> hakuTulos = organisaatioClient.listActiveOganisaatioPerustiedotRecursiveCached(organisaatioOid, Mode.single());
        return allRyhmas.stream()
                .filter(kayttoOikeusRyhma -> {
                    if (isEmpty(kayttoOikeusRyhma.getOrganisaatioViite()) && !isOphOrganisation) {
                        return false;
                    }
                    boolean noOrgLimits = !isEmpty(kayttoOikeusRyhma.getOrganisaatioViite())
                            && !isOphOrganisation && !checkOrganizationLimitations(organisaatioOid, hakuTulos, kayttoOikeusRyhma.getOrganisaatioViite());
                    return !noOrgLimits;
                }).collect(toList());
    }

    private boolean checkOrganizationLimitations(String organisaatioOid, List<OrganisaatioPerustieto> hakuTulos, List<OrganisaatioViiteDto> viites) {
        Set<String> tyyppis = viites.stream().map(OrganisaatioViiteDto::getOrganisaatioTyyppi).collect(toSet());

        if (organisaatioOid.startsWith(groupOrganizationId)) {
            return oidIsFoundInViites(groupOrganizationId, tyyppis);
        }
        if (oidIsFoundInViites(organisaatioOid, tyyppis)) {
            return true;
        }
        return hakuTulos.stream().filter(pt -> !isEmpty(pt.getChildren()))
                .anyMatch(perustieto -> orgTypeMatchesOrOidIsFoundInViites(organisaatioOid, tyyppis, perustieto));
    }

    private boolean orgTypeMatchesOrOidIsFoundInViites(String organisaatioOid, Set<String> organisaatioTyyppis, OrganisaatioPerustieto opt) {
        return opt.getChildren().stream()
                .anyMatch(child -> {
                    String laitosTyyppi = StringUtils.isNotBlank(child.getOppilaitostyyppi()) ? child.getOppilaitostyyppi().substring(17, 19) : null;
                    return organisaatioTyyppis.stream()
                            .anyMatch(s -> s.equals(laitosTyyppi) || s.equals(organisaatioOid));
                });
    }

    private boolean oidIsFoundInViites(String organisaatioOid, Set<String> organisaatioTyyppis) {
        return organisaatioTyyppis.stream().anyMatch(tyyppi -> tyyppi.equals(organisaatioOid));
    }

}
