package fi.vm.sade.kayttooikeus.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.dto.*;
import fi.vm.sade.kayttooikeus.dto.types.AnomusTyyppi;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.*;
import fi.vm.sade.kayttooikeus.service.EmailService;
import fi.vm.sade.kayttooikeus.service.KayttooikeusAnomusService;
import fi.vm.sade.kayttooikeus.service.LocalizationService;
import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;
import fi.vm.sade.kayttooikeus.service.exception.ForbiddenException;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.exception.UnprocessableEntityException;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.kayttooikeus.service.validators.HaettuKayttooikeusryhmaValidator;
import java.util.Collection;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

import static java.util.stream.Collectors.toSet;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindException;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.joda.time.LocalTime;
import fi.vm.sade.kayttooikeus.service.LdapSynchronizationService;

@Service
public class KayttooikeusAnomusServiceImpl extends AbstractService implements KayttooikeusAnomusService {

    private final HaettuKayttooikeusRyhmaDataRepository haettuKayttooikeusRyhmaDataRepository;
    private final HenkiloRepository henkiloRepository;
    private final HenkiloHibernateRepository henkiloHibernateRepository;
    private final MyonnettyKayttoOikeusRyhmaTapahtumaDataRepository myonnettyKayttoOikeusRyhmaTapahtumaDataRepository;
    private final KayttoOikeusRyhmaMyontoViiteRepository kayttoOikeusRyhmaMyontoViiteRepository;
    private final KayttoOikeusRyhmaTapahtumaHistoriaDataRepository kayttoOikeusRyhmaTapahtumaHistoriaDataRepository;
    private final KayttooikeusryhmaDataRepository kayttooikeusryhmaDataRepository;
    private final AnomusRepository anomusRepository;

    private final OrikaBeanMapper mapper;
    private final LocalizationService localizationService;
    private final EmailService emailService;
    private final LdapSynchronizationService ldapSynchronizationService;

    private final HaettuKayttooikeusryhmaValidator haettuKayttooikeusryhmaValidator;
    private final PermissionCheckerService permissionCheckerService;

    private final CommonProperties commonProperties;

    private final OrganisaatioClient organisaatioClient;

    @Autowired
    public KayttooikeusAnomusServiceImpl(HaettuKayttooikeusRyhmaDataRepository haettuKayttooikeusRyhmaDataRepository,
                                         HenkiloRepository henkiloRepository,
                                         HenkiloHibernateRepository henkiloHibernateRepository,
                                         MyonnettyKayttoOikeusRyhmaTapahtumaDataRepository myonnettyKayttoOikeusRyhmaTapahtumaDataRepository,
                                         KayttoOikeusRyhmaMyontoViiteRepository kayttoOikeusRyhmaMyontoViiteRepository,
                                         KayttoOikeusRyhmaTapahtumaHistoriaDataRepository kayttoOikeusRyhmaTapahtumaHistoriaDataRepository,
                                         OrikaBeanMapper orikaBeanMapper,
                                         LocalizationService localizationService,
                                         EmailService emailService,
                                         LdapSynchronizationService ldapSynchronizationService,
                                         HaettuKayttooikeusryhmaValidator haettuKayttooikeusryhmaValidator,
                                         PermissionCheckerService permissionCheckerService,
                                         KayttooikeusryhmaDataRepository kayttooikeusryhmaDataRepository,
                                         CommonProperties commonProperties,
                                         OrganisaatioClient organisaatioClient,
                                         AnomusRepository anomusRepository) {
        this.haettuKayttooikeusRyhmaDataRepository = haettuKayttooikeusRyhmaDataRepository;
        this.henkiloRepository = henkiloRepository;
        this.henkiloHibernateRepository = henkiloHibernateRepository;
        this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository = myonnettyKayttoOikeusRyhmaTapahtumaDataRepository;
        this.kayttoOikeusRyhmaMyontoViiteRepository = kayttoOikeusRyhmaMyontoViiteRepository;
        this.kayttoOikeusRyhmaTapahtumaHistoriaDataRepository = kayttoOikeusRyhmaTapahtumaHistoriaDataRepository;
        this.mapper = orikaBeanMapper;
        this.localizationService = localizationService;
        this.emailService = emailService;
        this.ldapSynchronizationService = ldapSynchronizationService;
        this.haettuKayttooikeusryhmaValidator = haettuKayttooikeusryhmaValidator;
        this.permissionCheckerService = permissionCheckerService;
        this.kayttooikeusryhmaDataRepository = kayttooikeusryhmaDataRepository;
        this.commonProperties = commonProperties;
        this.organisaatioClient = organisaatioClient;
        this.anomusRepository = anomusRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public List<HaettuKayttooikeusryhmaDto> getAllActiveAnomusByHenkiloOid(String oidHenkilo, boolean activeOnly) {
        if(activeOnly) {
            return localizeKayttooikeusryhma(mapper.mapAsList(this.haettuKayttooikeusRyhmaDataRepository
                    .findByAnomusHenkiloOidHenkiloAndAnomusAnomuksenTila(oidHenkilo, AnomuksenTila.ANOTTU), HaettuKayttooikeusryhmaDto.class));
        }
        return localizeKayttooikeusryhma(mapper.mapAsList(this.haettuKayttooikeusRyhmaDataRepository
                .findByAnomusHenkiloOidHenkilo(oidHenkilo), HaettuKayttooikeusryhmaDto.class));

    }

    private List<HaettuKayttooikeusryhmaDto> localizeKayttooikeusryhma(List<HaettuKayttooikeusryhmaDto> unlocalizedDtos) {
        unlocalizedDtos
                .forEach(haettuKayttooikeusryhmaDto -> haettuKayttooikeusryhmaDto
                        .setKayttoOikeusRyhma(localizationService.localize(haettuKayttooikeusryhmaDto.getKayttoOikeusRyhma())));
        return unlocalizedDtos;
    }

    @Transactional
    @Override
    public void updateHaettuKayttooikeusryhma(UpdateHaettuKayttooikeusryhmaDto updateHaettuKayttooikeusryhmaDto) {
        HaettuKayttoOikeusRyhma haettuKayttoOikeusRyhma = this.haettuKayttooikeusRyhmaDataRepository
                .findById(updateHaettuKayttooikeusryhmaDto.getId())
                .orElseThrow(() -> new NotFoundException("Haettua kayttooikeusryhmaa ei löytynyt id:llä "
                        + updateHaettuKayttooikeusryhmaDto.getId()));
        Anomus anojanAnomus = haettuKayttoOikeusRyhma.getAnomus();
        KayttoOikeusRyhma anottuKayttoOikeusRyhma = haettuKayttoOikeusRyhma.getKayttoOikeusRyhma();

        // Permission checks for declining requisition (there are separate checks for granting)
        this.notEditingOwnData(anojanAnomus.getHenkilo().getOidHenkilo());
        this.inSameOrParentOrganisation(anojanAnomus.getOrganisaatioOid());
        this.organisaatioViiteLimitationsAreValid(anottuKayttoOikeusRyhma.getId(),
                anojanAnomus.getOrganisaatioOid());
        this.kayttooikeusryhmaLimitationsAreValid(anottuKayttoOikeusRyhma.getId());

        // Post validation
        BindException errors = new BindException(haettuKayttoOikeusRyhma, "haettuKayttoOikeusRyhma");
        this.haettuKayttooikeusryhmaValidator.validate(haettuKayttoOikeusRyhma, errors);
        if(errors.hasErrors()) {
            throw new UnprocessableEntityException(errors);
        }

        Henkilo kasittelija = this.henkiloRepository.findByOidHenkilo(this.permissionCheckerService.getCurrentUserOid())
                .orElseThrow(() -> new NotFoundException("Kasittelija not found with oid " + this.permissionCheckerService.getCurrentUserOid()));
        anojanAnomus.setKasittelija(kasittelija);

        Henkilo anoja = this.henkiloRepository.findByOidHenkilo(anojanAnomus.getHenkilo().getOidHenkilo())
                .orElseThrow(() -> new NotFoundException("Anoja not found with oid "
                        + anojanAnomus.getHenkilo().getOidHenkilo()));

        this.updateAnomusAndRemoveHaettuKayttooikeusRyhma(haettuKayttoOikeusRyhma,
                KayttoOikeudenTila.valueOf(updateHaettuKayttooikeusryhmaDto.getKayttoOikeudenTila()));

        // Event is created only when kayttooikeus has been granted.
        if(KayttoOikeudenTila.valueOf(updateHaettuKayttooikeusryhmaDto.getKayttoOikeudenTila()) == KayttoOikeudenTila.MYONNETTY) {
            MyonnettyKayttoOikeusRyhmaTapahtuma myonnettyKayttoOikeusRyhmaTapahtuma = this.grantKayttooikeusryhma(
                    updateHaettuKayttooikeusryhmaDto.getAlkupvm(),
                    updateHaettuKayttooikeusryhmaDto.getLoppupvm(),
                    anoja.getOidHenkilo(),
                    anojanAnomus.getOrganisaatioOid(),
                    anottuKayttoOikeusRyhma,
                    anojanAnomus.getTehtavanimike());
            anojanAnomus.addMyonnettyKayttooikeusRyhma(myonnettyKayttoOikeusRyhmaTapahtuma);
        }

        // If everything is handled and something is granted on anomus send email notification to anoja.
        if(anojanAnomus.getHaettuKayttoOikeusRyhmas().isEmpty()
                && !anojanAnomus.getMyonnettyKayttooikeusRyhmas().isEmpty()) {
            this.emailService.sendEmailAnomusAccepted(anojanAnomus);
        }
    }

    private void notEditingOwnData(String oidHenkilo) {
        // Cant edit own data
        if(!this.permissionCheckerService.notOwnData(oidHenkilo)) {
            throw new ForbiddenException("User can't edit his own data.");
        }
    }

    private void inSameOrParentOrganisation(String organisaatioOid) {
        // User has to be in the same or one of the parent organisations
        if(!this.permissionCheckerService.checkRoleForOrganisation(
                Lists.newArrayList(organisaatioOid),
                Lists.newArrayList("READ_UPDATE", "CRUD"))) {
            throw new ForbiddenException("No access through organisation hierarchy.");
        }
    }

    private void organisaatioViiteLimitationsAreValid(Long kayttooikeusryhmaId, String organisaatioOid) {
        Set<OrganisaatioViite> organisaatioViite = this.kayttooikeusryhmaDataRepository.findById(kayttooikeusryhmaId)
                .orElseThrow(() -> new NotFoundException("Could not find kayttooikeusryhma with id " + kayttooikeusryhmaId.toString()))
                .getOrganisaatioViite();
        // Organisaatiohenkilo limitations are valid
        if(!CollectionUtils.isEmpty(organisaatioViite)
                // only root organisation should not have organisaatioviite
                && !commonProperties.getRootOrganizationOid().equals(organisaatioOid)
                && !this.permissionCheckerService.organisaatioLimitationCheck(organisaatioOid, organisaatioViite)) {
            throw new ForbiddenException("Target organization has invalid organization type");
        }
    }

    private void kayttooikeusryhmaLimitationsAreValid(Long kayttooikeusryhmaId) {
        // The granting person's limitations must be checked always since there there shouldn't be a situation where the
        // the granting person doesn't have access rights limitations (except admin users who have full access)
        if(!this.permissionCheckerService.kayttooikeusMyontoviiteLimitationCheck(kayttooikeusryhmaId)) {
            throw new ForbiddenException("User doesn't have access rights to grant this group");
        }
    }

    private void updateAnomusAndRemoveHaettuKayttooikeusRyhma(HaettuKayttoOikeusRyhma haettuKayttoOikeusRyhma,
                                                              KayttoOikeudenTila newKayttooikeudenTila) {
        if(haettuKayttoOikeusRyhma.getAnomus().getHaettuKayttoOikeusRyhmas().size() == 1) {
            // This is the last kayttooikeus on anomus so we mark anomus as KASITELTY or HYLATTY
            haettuKayttoOikeusRyhma.getAnomus().setAnomuksenTila(
                    newKayttooikeudenTila == KayttoOikeudenTila.MYONNETTY || newKayttooikeudenTila == KayttoOikeudenTila.UUSITTU
                            ? AnomuksenTila.KASITELTY
                            : AnomuksenTila.HYLATTY);
        }
        haettuKayttoOikeusRyhma.getAnomus().setAnomusTilaTapahtumaPvm(new DateTime());

        // Remove the currently handled kayttooikeus from anomus
        haettuKayttoOikeusRyhma.getAnomus().getHaettuKayttoOikeusRyhmas().remove(haettuKayttoOikeusRyhma);
        this.haettuKayttooikeusRyhmaDataRepository.delete(haettuKayttoOikeusRyhma);
    }

    // Sets organisaatiohenkilo active since it might be passive
    private OrganisaatioHenkilo findOrCreateHaettuOrganisaatioHenkilo(String organisaatioOid, Henkilo anoja, String tehtavanimike) {
        OrganisaatioHenkilo foundOrCreatedOrganisaatioHenkilo = anoja.getOrganisaatioHenkilos().stream()
                .filter(organisaatioHenkilo ->
                        Objects.equals(organisaatioHenkilo.getOrganisaatioOid(), organisaatioOid))
                .findFirst().orElseGet(() ->
                anoja.addOrganisaatioHenkilo(OrganisaatioHenkilo.builder()
                        .organisaatioOid(organisaatioOid)
                        .tehtavanimike(tehtavanimike)
                        .henkilo(anoja)
                        .build()));
        foundOrCreatedOrganisaatioHenkilo.setPassivoitu(false);
        return foundOrCreatedOrganisaatioHenkilo;
    }

    @Override
    @Transactional
    public void grantKayttooikeusryhma(String anojaOid,
                                       String organisaatioOid,
                                       List<GrantKayttooikeusryhmaDto> updateHaettuKayttooikeusryhmaDtoList) {
        // Permission checks
        this.notEditingOwnData(anojaOid);
        this.inSameOrParentOrganisation(organisaatioOid);
        updateHaettuKayttooikeusryhmaDtoList.forEach(updateHaettuKayttooikeusryhmaDto -> {
                    this.organisaatioViiteLimitationsAreValid(updateHaettuKayttooikeusryhmaDto.getId(), organisaatioOid);
                    this.kayttooikeusryhmaLimitationsAreValid(updateHaettuKayttooikeusryhmaDto.getId());
        });

        updateHaettuKayttooikeusryhmaDtoList.forEach(haettuKayttooikeusryhmaDto ->
                this.grantKayttooikeusryhma(haettuKayttooikeusryhmaDto.getAlkupvm(),
                        haettuKayttooikeusryhmaDto.getLoppupvm(),
                        anojaOid,
                        organisaatioOid,
                        this.kayttooikeusryhmaDataRepository.findById(haettuKayttooikeusryhmaDto.getId())
                                .orElseThrow(() -> new NotFoundException("Kayttooikeusryhma not found with id " + haettuKayttooikeusryhmaDto.getId())),
                        ""));
    }

    @Override
    @Transactional
    public Long createKayttooikeusAnomus(String anojaOid, KayttooikeusAnomusDto kayttooikeusAnomusDto) {
        Henkilo anoja = this.henkiloRepository.findByOidHenkilo(anojaOid)
                .orElseThrow(() -> new NotFoundException("Anoja not found with oid " + anojaOid));

        Anomus anomus = new Anomus();
        anomus.setHenkilo(anoja);
        anomus.setAnomuksenTila(AnomuksenTila.ANOTTU);
        anomus.setSahkopostiosoite(kayttooikeusAnomusDto.getEmail());
        anomus.setPerustelut(kayttooikeusAnomusDto.getPerustelut());
        anomus.setAnomusTyyppi(AnomusTyyppi.UUSI);
        anomus.setAnomusTilaTapahtumaPvm(new DateTime());
        anomus.setAnottuPvm(new DateTime());
        anomus.setOrganisaatioOid(kayttooikeusAnomusDto.getOrganisaatioOrRyhmaOid());
        anomus.setTehtavanimike(kayttooikeusAnomusDto.getTehtavaNimike());

        Iterable<KayttoOikeusRyhma> kayttoOikeusRyhmas =  this.kayttooikeusryhmaDataRepository
                .findAll(kayttooikeusAnomusDto.getKayttooikeusRyhmaIds());

        kayttoOikeusRyhmas.forEach( k -> {
            HaettuKayttoOikeusRyhma h = new HaettuKayttoOikeusRyhma();
            h.setKayttoOikeusRyhma(k);
            anomus.addHaettuKayttoOikeusRyhma(h);
        });
        return this.anomusRepository.save(anomus).getId();
    }

    @Override
    @Transactional
    public void lahetaUusienAnomuksienIlmoitukset(LocalDate anottuPvm) {
        AnomusCriteria criteria = AnomusCriteria.builder()
                .anottuAlku(anottuPvm.toDateTimeAtStartOfDay())
                .anottuLoppu(anottuPvm.toDateTime(LocalTime.MIDNIGHT.minusMillis(1)))
                .tila(AnomuksenTila.ANOTTU)
                .build();
        List<Anomus> anomukset = anomusRepository.findBy(criteria);

        Set<String> hyvaksyjat = anomukset.stream()
                .map(this::getAnomuksenHyvaksyjat)
                .flatMap(Collection::stream)
                .collect(toSet());
        emailService.sendNewRequisitionNotificationEmails(hyvaksyjat);
    }

    private Set<String> getAnomuksenHyvaksyjat(Anomus anomus) {
        Set<Long> kayttoOikeusRyhmaIds = getHyvaksyjaKayttoOikeusRyhmat(anomus);
        if (kayttoOikeusRyhmaIds.isEmpty()) {
            logger.info("Ei löytynyt käyttöoikeusryhmiä, jotka voisivat hyväksyä anomuksen {}", anomus.getId());
            return emptySet();
        }
        Set<String> organisaatioOids = getHyvaksyjaOrganisaatiot(anomus);
        if (organisaatioOids.isEmpty()) {
            logger.info("Ei löytynyt organisaatioita, jotka voisivat hyväksyä anomuksen {}", anomus.getId());
            return emptySet();
        }
        Set<String> henkiloOids = henkiloHibernateRepository.findByKayttoOikeusRyhmatAndOrganisaatiot(kayttoOikeusRyhmaIds, organisaatioOids)
                .stream()
                .map(Henkilo::getOidHenkilo)
                // Henkilö ei saa hyväksyä omaa käyttöoikeusanomusta
                .filter(t -> !t.equals(anomus.getHenkilo().getOidHenkilo()))
                .collect(toSet());
        if (henkiloOids.isEmpty()) {
            logger.info("Anomuksella {} ei ole hyväksyjiä", anomus.getId());
        }
        return henkiloOids;
    }

    private Set<Long> getHyvaksyjaKayttoOikeusRyhmat(Anomus anomus) {
        Set<Long> slaveIds = anomus.getHaettuKayttoOikeusRyhmas().stream()
                .map(t -> t.getKayttoOikeusRyhma().getId())
                .collect(toSet());
        return kayttoOikeusRyhmaMyontoViiteRepository.getMasterIdsBySlaveIds(slaveIds);
    }

    private Set<String> getHyvaksyjaOrganisaatiot(Anomus anomus) {
        if (commonProperties.getRootOrganizationOid().equals(anomus.getOrganisaatioOid())) {
            return singleton(commonProperties.getRootOrganizationOid());
        }
        return organisaatioClient.getParentOids(anomus.getOrganisaatioOid()).stream()
                // Ei lähetetä root-organisaation henkilöille jokaisesta anomuksesta ilmoitusta
                .filter(t -> !commonProperties.getRootOrganizationOid().equals(t))
                .collect(toSet());
    }

    // Grant kayttooikeusryhma and create event. DOES NOT CONTAIN PERMISSION CHECKS SO DONT CALL DIRECTLY
    private MyonnettyKayttoOikeusRyhmaTapahtuma grantKayttooikeusryhma(LocalDate alkupvm,
                                        LocalDate loppupvm,
                                        String anojaOid,
                                        String organisaatioOid,
                                        KayttoOikeusRyhma myonnettavaKayttoOikeusRyhma,
                                        String tehtavanimike) {
        Henkilo anoja = this.henkiloRepository.findByOidHenkilo(anojaOid)
                .orElseThrow(() -> new NotFoundException("Anoja not found with oid " + anojaOid));
        Henkilo kasittelija = this.henkiloRepository.findByOidHenkilo(this.permissionCheckerService.getCurrentUserOid())
                .orElseThrow(() -> new NotFoundException("Kasittelija not found with oid " + this.getCurrentUserOid()));

        OrganisaatioHenkilo myonnettavaOrganisaatioHenkilo = this.findOrCreateHaettuOrganisaatioHenkilo(
                organisaatioOid, anoja, tehtavanimike);

        MyonnettyKayttoOikeusRyhmaTapahtuma myonnettyKayttoOikeusRyhmaTapahtuma =
                this.findOrCreateMyonnettyKayttooikeusryhmaTapahtuma(anojaOid, myonnettavaOrganisaatioHenkilo,
                myonnettavaKayttoOikeusRyhma);

        myonnettyKayttoOikeusRyhmaTapahtuma.setVoimassaAlkuPvm(alkupvm);
        myonnettyKayttoOikeusRyhmaTapahtuma.setVoimassaLoppuPvm(loppupvm);
        myonnettyKayttoOikeusRyhmaTapahtuma.setAikaleima(DateTime.now());
        myonnettyKayttoOikeusRyhmaTapahtuma.setKasittelija(kasittelija);
        myonnettyKayttoOikeusRyhmaTapahtuma.setTila(myonnettyKayttoOikeusRyhmaTapahtuma.getId() == null
                ? KayttoOikeudenTila.MYONNETTY
                : KayttoOikeudenTila.UUSITTU);

        this.createGrantedHistoryEvent(myonnettyKayttoOikeusRyhmaTapahtuma,
                myonnettyKayttoOikeusRyhmaTapahtuma.getId() == null
                        ? "Oikeuksien lisäys"
                        : "Oikeuksien päivitys");

        this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository.save(myonnettyKayttoOikeusRyhmaTapahtuma);

        ldapSynchronizationService.updateHenkiloAsap(anojaOid);

        return myonnettyKayttoOikeusRyhmaTapahtuma;
    }

    // New history event for a change on kayttooikeusryhmatapahtuma.
    private void createGrantedHistoryEvent(MyonnettyKayttoOikeusRyhmaTapahtuma myonnettyKayttoOikeusRyhmaTapahtuma, String reason) {
        this.kayttoOikeusRyhmaTapahtumaHistoriaDataRepository.save(myonnettyKayttoOikeusRyhmaTapahtuma.toHistoria(DateTime.now(), reason));
    }

    private MyonnettyKayttoOikeusRyhmaTapahtuma findOrCreateMyonnettyKayttooikeusryhmaTapahtuma(String oidHenkilo,
                                                                                                OrganisaatioHenkilo organisaatioHenkilo,
                                                                                                KayttoOikeusRyhma kayttoOikeusRyhma) {
        return this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository.findMyonnettyTapahtuma(kayttoOikeusRyhma.getId(),
                organisaatioHenkilo.getOrganisaatioOid(), oidHenkilo)
                .orElseGet(() -> MyonnettyKayttoOikeusRyhmaTapahtuma.builder()
                        .kayttoOikeusRyhma(kayttoOikeusRyhma)
                        .organisaatioHenkilo(organisaatioHenkilo)
                        .anomus(Sets.newHashSet()).build());
    }

    @Override
    @Transactional
    public void cancelKayttooikeusAnomus(Long kayttooikeusRyhmaId) {
        HaettuKayttoOikeusRyhma haettuKayttoOikeusRyhma = this.haettuKayttooikeusRyhmaDataRepository.findById(kayttooikeusRyhmaId)
                .orElseThrow( () -> new NotFoundException("HaettuKayttooikeusRyhma not found with id " + kayttooikeusRyhmaId) );
        Anomus anomus = haettuKayttoOikeusRyhma.getAnomus();
        anomus.getHaettuKayttoOikeusRyhmas().removeIf( h -> h.getId().equals(haettuKayttoOikeusRyhma.getId()) );
        if(anomus.getHaettuKayttoOikeusRyhmas().isEmpty()) {
            anomus.setAnomuksenTila(AnomuksenTila.PERUTTU);
        }
        this.haettuKayttooikeusRyhmaDataRepository.delete(kayttooikeusRyhmaId);
    }

}
