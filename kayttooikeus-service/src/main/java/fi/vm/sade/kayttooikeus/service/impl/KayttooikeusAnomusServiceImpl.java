package fi.vm.sade.kayttooikeus.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.dto.*;
import fi.vm.sade.kayttooikeus.dto.types.AnomusTyyppi;
import fi.vm.sade.kayttooikeus.enumeration.OrderByAnomus;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.*;
import fi.vm.sade.kayttooikeus.repositories.criteria.AnomusCriteria;
import fi.vm.sade.kayttooikeus.service.*;
import fi.vm.sade.kayttooikeus.service.exception.ForbiddenException;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.exception.UnprocessableEntityException;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.kayttooikeus.service.validators.HaettuKayttooikeusryhmaValidator;
import fi.vm.sade.kayttooikeus.util.UserDetailsUtil;
import java.util.EnumSet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
public class KayttooikeusAnomusServiceImpl extends AbstractService implements KayttooikeusAnomusService {

    private final HaettuKayttooikeusRyhmaRepository haettuKayttooikeusRyhmaRepository;
    private final HenkiloDataRepository henkiloDataRepository;
    private final HenkiloHibernateRepository henkiloHibernateRepository;
    private final MyonnettyKayttoOikeusRyhmaTapahtumaDataRepository myonnettyKayttoOikeusRyhmaTapahtumaDataRepository;
    private final KayttoOikeusRyhmaMyontoViiteRepository kayttoOikeusRyhmaMyontoViiteRepository;
    private final KayttoOikeusRyhmaTapahtumaHistoriaDataRepository kayttoOikeusRyhmaTapahtumaHistoriaDataRepository;
    private final KayttooikeusryhmaDataRepository kayttooikeusryhmaDataRepository;
    private final AnomusRepository anomusRepository;
    private final OrganisaatioHenkiloDataRepository organisaatioHenkiloDataRepository;

    private final OrikaBeanMapper mapper;
    private final LocalizationService localizationService;
    private final EmailService emailService;
    private final LdapSynchronizationService ldapSynchronizationService;

    private final HaettuKayttooikeusryhmaValidator haettuKayttooikeusryhmaValidator;
    private final PermissionCheckerService permissionCheckerService;

    private final CommonProperties commonProperties;

    private final OrganisaatioClient organisaatioClient;

    @Override
    @Transactional(readOnly = true)
    public List<HaettuKayttooikeusryhmaDto> listHaetutKayttoOikeusRyhmat(AnomusCriteria criteria) {
        return this.listHaetutKayttoOikeusRyhmat(criteria, null, null, null, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HaettuKayttooikeusryhmaDto> listHaetutKayttoOikeusRyhmat(AnomusCriteria criteria,
                                                                         Long limit,
                                                                         Long offset,
                                                                         OrderByAnomus orderBy,
                                                                         boolean showOwnAnomus) {
        if(!showOwnAnomus) {
            if(criteria.getHenkiloOidRestrictionList() != null) {
                criteria.addHenkiloOidRestriction(UserDetailsUtil.getCurrentUserOid());
            }
            else {
                criteria.setHenkiloOidRestrictionList(Sets.newHashSet(UserDetailsUtil.getCurrentUserOid()));
            }
        }
        return localizeKayttooikeusryhma(mapper.mapAsList(this.haettuKayttooikeusRyhmaRepository
                .findBy(criteria, limit, offset, orderBy), HaettuKayttooikeusryhmaDto.class));
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
        HaettuKayttoOikeusRyhma haettuKayttoOikeusRyhma = this.haettuKayttooikeusRyhmaRepository
                .findById(updateHaettuKayttooikeusryhmaDto.getId())
                .orElseThrow(() -> new NotFoundException("Haettua kayttooikeusryhmaa ei löytynyt id:llä "
                        + updateHaettuKayttooikeusryhmaDto.getId()));
        Anomus anojanAnomus = haettuKayttoOikeusRyhma.getAnomus();
        KayttoOikeusRyhma anottuKayttoOikeusRyhma = haettuKayttoOikeusRyhma.getKayttoOikeusRyhma();

        // Permission checks for declining requisition (there are separate checks for granting)
        this.notEditingOwnData(anojanAnomus.getHenkilo().getOidHenkilo());
        this.inSameOrParentOrganisation(anojanAnomus.getOrganisaatioOid());
        this.organisaatioViiteLimitationsAreValidThrows(anottuKayttoOikeusRyhma.getId());
        this.kayttooikeusryhmaLimitationsAreValid(anottuKayttoOikeusRyhma.getId());

        // Post validation
        BindException errors = new BindException(haettuKayttoOikeusRyhma, "haettuKayttoOikeusRyhma");
        this.haettuKayttooikeusryhmaValidator.validate(haettuKayttoOikeusRyhma, errors);
        if(errors.hasErrors()) {
            throw new UnprocessableEntityException(errors);
        }

        Henkilo kasittelija = this.henkiloDataRepository.findByOidHenkilo(this.permissionCheckerService.getCurrentUserOid())
                .orElseThrow(() -> new NotFoundException("Kasittelija not found with oid " + this.permissionCheckerService.getCurrentUserOid()));
        anojanAnomus.setKasittelija(kasittelija);

        Henkilo anoja = this.henkiloDataRepository.findByOidHenkilo(anojanAnomus.getHenkilo().getOidHenkilo())
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

    private void organisaatioViiteLimitationsAreValidThrows(Long kayttooikeusryhmaId) {
        if(!this.organisaatioViiteLimitationsAreValid(kayttooikeusryhmaId)) {
            throw new ForbiddenException("Target organization has invalid organization type");
        }
    }

    private boolean organisaatioViiteLimitationsAreValid(Long kayttooikeusryhmaId) {
        Set<OrganisaatioViite> organisaatioViite = this.kayttooikeusryhmaDataRepository.findById(kayttooikeusryhmaId)
                .orElseThrow(() -> new NotFoundException("Could not find kayttooikeusryhma with id " + kayttooikeusryhmaId.toString()))
                .getOrganisaatioViite();
        List<String> currentUserOrganisaatioOids = this.organisaatioHenkiloDataRepository
                .findByHenkiloOidHenkilo(UserDetailsUtil.getCurrentUserOid()).stream()
                .filter(((Predicate<OrganisaatioHenkilo>) OrganisaatioHenkilo::isPassivoitu).negate())
                .map(OrganisaatioHenkilo::getOrganisaatioOid)
                .collect(Collectors.toList());

        // When granting to root organisation it has no organisaatioviite
        return !(!CollectionUtils.isEmpty(organisaatioViite)
                // Root organisation users do not need to pass organisaatioviite
                && currentUserOrganisaatioOids.stream().noneMatch((orgOid) -> commonProperties.getRootOrganizationOid().equals(orgOid))
                // Organisaatiohenkilo limitations are valid
                && currentUserOrganisaatioOids.stream().noneMatch((orgOid) -> this.permissionCheckerService.organisaatioLimitationCheck(orgOid, organisaatioViite)));
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
        haettuKayttoOikeusRyhma.getAnomus().setAnomusTilaTapahtumaPvm(LocalDateTime.now());

        // Remove the currently handled kayttooikeus from anomus
        haettuKayttoOikeusRyhma.getAnomus().getHaettuKayttoOikeusRyhmas().remove(haettuKayttoOikeusRyhma);
        this.haettuKayttooikeusRyhmaRepository.delete(haettuKayttoOikeusRyhma);
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
            this.organisaatioViiteLimitationsAreValidThrows(updateHaettuKayttooikeusryhmaDto.getId());
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
        Henkilo anoja = this.henkiloDataRepository.findByOidHenkilo(anojaOid)
                .orElseThrow(() -> new NotFoundException("Anoja not found with oid " + anojaOid));

        Anomus anomus = new Anomus();
        anomus.setHenkilo(anoja);
        anomus.setAnomuksenTila(AnomuksenTila.ANOTTU);
        anomus.setSahkopostiosoite(kayttooikeusAnomusDto.getEmail());
        anomus.setPerustelut(kayttooikeusAnomusDto.getPerustelut());
        anomus.setAnomusTyyppi(AnomusTyyppi.UUSI);
        anomus.setAnomusTilaTapahtumaPvm(LocalDateTime.now());
        anomus.setAnottuPvm(LocalDateTime.now());
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
                .anottuAlku(anottuPvm.atStartOfDay())
                .anottuLoppu(anottuPvm.atTime(LocalTime.MIDNIGHT.minusSeconds(1)))
                .anomuksenTilat(EnumSet.of(AnomuksenTila.ANOTTU))
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
        Henkilo anoja = this.henkiloDataRepository.findByOidHenkilo(anojaOid)
                .orElseThrow(() -> new NotFoundException("Anoja not found with oid " + anojaOid));
        Henkilo kasittelija = this.henkiloDataRepository.findByOidHenkilo(this.permissionCheckerService.getCurrentUserOid())
                .orElseThrow(() -> new NotFoundException("Kasittelija not found with oid " + this.getCurrentUserOid()));

        OrganisaatioHenkilo myonnettavaOrganisaatioHenkilo = this.findOrCreateHaettuOrganisaatioHenkilo(
                organisaatioOid, anoja, tehtavanimike);

        MyonnettyKayttoOikeusRyhmaTapahtuma myonnettyKayttoOikeusRyhmaTapahtuma =
                this.findOrCreateMyonnettyKayttooikeusryhmaTapahtuma(anojaOid, myonnettavaOrganisaatioHenkilo,
                        myonnettavaKayttoOikeusRyhma);

        myonnettyKayttoOikeusRyhmaTapahtuma.setVoimassaAlkuPvm(alkupvm);
        myonnettyKayttoOikeusRyhmaTapahtuma.setVoimassaLoppuPvm(loppupvm);
        myonnettyKayttoOikeusRyhmaTapahtuma.setAikaleima(LocalDateTime.now());
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
        this.kayttoOikeusRyhmaTapahtumaHistoriaDataRepository.save(myonnettyKayttoOikeusRyhmaTapahtuma.toHistoria(LocalDateTime.now(), reason));
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
        HaettuKayttoOikeusRyhma haettuKayttoOikeusRyhma = this.haettuKayttooikeusRyhmaRepository.findById(kayttooikeusRyhmaId)
                .orElseThrow( () -> new NotFoundException("HaettuKayttooikeusRyhma not found with id " + kayttooikeusRyhmaId) );
        Anomus anomus = haettuKayttoOikeusRyhma.getAnomus();
        anomus.getHaettuKayttoOikeusRyhmas().removeIf( h -> h.getId().equals(haettuKayttoOikeusRyhma.getId()) );
        if(anomus.getHaettuKayttoOikeusRyhmas().isEmpty()) {
            anomus.setAnomuksenTila(AnomuksenTila.PERUTTU);
        }
        this.haettuKayttooikeusRyhmaRepository.delete(kayttooikeusRyhmaId);
    }

    @Override
    @Transactional
    public void removePrivilege(String oidHenkilo, Long kayttooikeusryhmaId, String organisaatioOid) {
        // Permission checks
        this.notEditingOwnData(oidHenkilo);
        this.inSameOrParentOrganisation(organisaatioOid);
        this.organisaatioViiteLimitationsAreValidThrows(kayttooikeusryhmaId);
        this.kayttooikeusryhmaLimitationsAreValid(kayttooikeusryhmaId);

        Henkilo kasittelija = this.henkiloDataRepository.findByOidHenkilo(UserDetailsUtil.getCurrentUserOid())
                .orElseThrow(() -> new NotFoundException("Current user not found with oid " + UserDetailsUtil.getCurrentUserOid()));

        MyonnettyKayttoOikeusRyhmaTapahtuma myonnettyKayttoOikeusRyhmaTapahtuma =
                this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository.findMyonnettyTapahtuma(kayttooikeusryhmaId, organisaatioOid, oidHenkilo)
                        .orElseThrow(() -> new NotFoundException("Myonnettykayttooikeusryhma not found with KayttooikeusryhmaId "
                                + kayttooikeusryhmaId + " organisaatioOid " + organisaatioOid + " oidHenkilo " + oidHenkilo));
        this.kayttoOikeusRyhmaTapahtumaHistoriaDataRepository.save(myonnettyKayttoOikeusRyhmaTapahtuma
                .toHistoria(kasittelija, KayttoOikeudenTila.SULJETTU, LocalDateTime.now(),"Käyttöoikeuden sulkeminen"));
        this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository.delete(myonnettyKayttoOikeusRyhmaTapahtuma);
    }

    // Käsittelee admin, OPH-virkailija ja virkailija tyyppisiä käyttäjiä. Kaksi ensimmäistä käyttäytyvät tässä samoin.
    // Olettaa, että ennestään olleet ja haetut oikeudet voidaan myöntää uudelleen kyseiseen organisaatioon.
    @Override
    @Transactional(readOnly = true)
    public Map<String, Set<Long>> findCurrentHenkiloCanGrant(String accessedHenkiloOid) {
        final Map<String, Set<Long>> kayttooikeusByOrganisation = new HashMap<>();

        // Haetun henkilön anumukset, voimassa olevat käyttöoikeudet ja joskus voimassa olleet oikeudet
        this.anomusRepository.findByHenkiloOidHenkilo(accessedHenkiloOid)
                .forEach(anomus -> kayttooikeusByOrganisation.put(anomus.getOrganisaatioOid(),
                        anomus.getHaettuKayttoOikeusRyhmas().stream()
                                .map(HaettuKayttoOikeusRyhma::getKayttoOikeusRyhma)
                                .map(KayttoOikeusRyhma::getId)
                                .collect(toSet())));
        this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository.findByOrganisaatioHenkiloHenkiloOidHenkilo(accessedHenkiloOid)
                .forEach(mkrt -> kayttooikeusByOrganisation.compute(mkrt.getOrganisaatioHenkilo().getOrganisaatioOid(),
                        this.addIfNotExists(mkrt.getKayttoOikeusRyhma().getId())));
        this.kayttoOikeusRyhmaTapahtumaHistoriaDataRepository
                .findByOrganisaatioHenkiloHenkiloOidHenkiloAndTila(accessedHenkiloOid, KayttoOikeudenTila.SULJETTU)
                .forEach(krth -> kayttooikeusByOrganisation.compute(krth.getOrganisaatioHenkilo().getOrganisaatioOid(),
                        this.addIfNotExists(krth.getKayttoOikeusRyhma().getId())));

        List<String> allowedOrganisaatioOids = this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository
                .findCrudAnomustenhallinta(this.permissionCheckerService.getCurrentUserOid()).stream()
                .map(MyonnettyKayttoOikeusRyhmaTapahtuma::getOrganisaatioHenkilo)
                .map(OrganisaatioHenkilo::getOrganisaatioOid)
                .collect(Collectors.toList());

        if(!allowedOrganisaatioOids.contains(this.commonProperties.getRootOrganizationOid())) {
            allowedOrganisaatioOids.addAll(allowedOrganisaatioOids.stream().flatMap(organisaatioOid ->
                    this.organisaatioClient.getChildOids(organisaatioOid).stream()).collect(Collectors.toList()));

            kayttooikeusByOrganisation.keySet().removeIf(organisaatioOid ->
                    !allowedOrganisaatioOids.contains(organisaatioOid));

            regularUserChecks(kayttooikeusByOrganisation, allowedOrganisaatioOids);
        }

        // Filter off empty keys
        return kayttooikeusByOrganisation.entrySet().stream()
                .filter(entity -> !entity.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void regularUserChecks(Map<String, Set<Long>> kayttooikeusByOrganisation, List<String> allowedOrganisaatioOids) {
        // MyönnettyKäyttöoikeusryhmäTapahtumat joista löytyvät nämä validit organisaatiot
        this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository
                .findValidMyonnettyKayttooikeus(UserDetailsUtil.getCurrentUserOid()).stream()
                .filter(myonnettyKayttoOikeusRyhmaTapahtuma -> allowedOrganisaatioOids.stream()
                        .anyMatch(allowedRole -> allowedRole.contains(myonnettyKayttoOikeusRyhmaTapahtuma.getOrganisaatioHenkilo().getOrganisaatioOid())))
                // Tarkista myöntöviite slave idt (myönnettävissä olevat ryhmät, samaan ryhmään ei voi myöntää)
                .forEach(myonnettyKayttoOikeusRyhmaTapahtuma ->
                        Optional.ofNullable(this.kayttoOikeusRyhmaMyontoViiteRepository.getSlaveIdsByMasterIds(
                                Lists.newArrayList(myonnettyKayttoOikeusRyhmaTapahtuma.getKayttoOikeusRyhma().getId())))
                                .filter(kayttooikeusIdList -> !kayttooikeusByOrganisation.isEmpty())
                                .ifPresent(allowedIds -> Optional.ofNullable(kayttooikeusByOrganisation
                                        .get(myonnettyKayttoOikeusRyhmaTapahtuma.getOrganisaatioHenkilo().getOrganisaatioOid()))
                                        .ifPresent(kayttooikeusIdList -> kayttooikeusIdList.removeIf(ryhmaId ->
                                                !allowedIds.contains(ryhmaId)))));

        // Organisaatioviite (pystyykö tästä KOR myöntämään tähän organisaatioon)
        kayttooikeusByOrganisation.replaceAll((organisaatioOid, kayttooikeusryhmaIdList) ->
                kayttooikeusryhmaIdList.stream()
                        .filter(this::organisaatioViiteLimitationsAreValid)
                        .collect(Collectors.toSet()));
    }

    private BiFunction<String, Set<Long>, Set<Long>> addIfNotExists(Long ryhmaId) {
        return (key, value) -> {
            if(value == null) {
                value = new HashSet<>();
            }
            if(!value.contains(ryhmaId)) {
                value.add(ryhmaId);
            }
            return value;
        };
    }
}
