package fi.vm.sade.kayttooikeus.service.impl;

import com.google.common.collect.Lists;
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
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindException;

@Service
public class KayttooikeusAnomusServiceImpl extends AbstractService implements KayttooikeusAnomusService {

    private final HaettuKayttooikeusRyhmaDataRepository haettuKayttooikeusRyhmaDataRepository;
    private final HenkiloRepository henkiloRepository;
    private final MyonnettyKayttoOikeusRyhmaTapahtumaDataRepository myonnettyKayttoOikeusRyhmaTapahtumaDataRepository;
    private final KayttoOikeusRyhmaMyontoViiteRepository kayttoOikeusRyhmaMyontoViiteRepository;
    private final KayttoOikeusRyhmaTapahtumaHistoriaDataRepository kayttoOikeusRyhmaTapahtumaHistoriaDataRepository;
    private final KayttooikeusryhmaDataRepository kayttooikeusryhmaDataRepository;
    private final AnomusDataRepository anomusDataRepository;

    private final OrikaBeanMapper mapper;
    private final LocalizationService localizationService;
    private final EmailService emailService;

    private final HaettuKayttooikeusryhmaValidator haettuKayttooikeusryhmaValidator;
    private final PermissionCheckerService permissionCheckerService;

    private final CommonProperties commonProperties;

    private final OrganisaatioClient organisaatioClient;



    @Autowired
    public KayttooikeusAnomusServiceImpl(HaettuKayttooikeusRyhmaDataRepository haettuKayttooikeusRyhmaDataRepository,
                                         HenkiloRepository henkiloRepository,
                                         MyonnettyKayttoOikeusRyhmaTapahtumaDataRepository myonnettyKayttoOikeusRyhmaTapahtumaDataRepository,
                                         KayttoOikeusRyhmaMyontoViiteRepository kayttoOikeusRyhmaMyontoViiteRepository,
                                         KayttoOikeusRyhmaTapahtumaHistoriaDataRepository kayttoOikeusRyhmaTapahtumaHistoriaDataRepository,
                                         OrikaBeanMapper orikaBeanMapper,
                                         LocalizationService localizationService,
                                         HaettuKayttooikeusryhmaValidator haettuKayttooikeusryhmaValidator,
                                         PermissionCheckerService permissionCheckerService,
                                         KayttooikeusryhmaDataRepository kayttooikeusryhmaDataRepository,
                                         CommonProperties commonProperties,
                                         OrganisaatioClient organisaatioClient,
                                         AnomusDataRepository anomusDataRepository,
                                         EmailService emailService) {
        this.haettuKayttooikeusRyhmaDataRepository = haettuKayttooikeusRyhmaDataRepository;
        this.henkiloRepository = henkiloRepository;
        this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository = myonnettyKayttoOikeusRyhmaTapahtumaDataRepository;
        this.kayttoOikeusRyhmaMyontoViiteRepository = kayttoOikeusRyhmaMyontoViiteRepository;
        this.kayttoOikeusRyhmaTapahtumaHistoriaDataRepository = kayttoOikeusRyhmaTapahtumaHistoriaDataRepository;
        this.mapper = orikaBeanMapper;
        this.localizationService = localizationService;
        this.haettuKayttooikeusryhmaValidator = haettuKayttooikeusryhmaValidator;
        this.permissionCheckerService = permissionCheckerService;
        this.kayttooikeusryhmaDataRepository = kayttooikeusryhmaDataRepository;
        this.commonProperties = commonProperties;
        this.organisaatioClient = organisaatioClient;
        this.anomusDataRepository = anomusDataRepository;
        this.emailService = emailService;
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

        // Permission checks for declining requisition (there are separate checks for granting)
        this.notEditingOwnData(haettuKayttoOikeusRyhma.getAnomus().getHenkilo().getOidHenkilo());
        this.inSameOrParentOrganisation(haettuKayttoOikeusRyhma.getAnomus().getOrganisaatioOid());
        this.organisaatioViiteLimitationsAreValid(haettuKayttoOikeusRyhma.getKayttoOikeusRyhma().getId(),
                haettuKayttoOikeusRyhma.getAnomus().getOrganisaatioOid());
        this.kayttooikeusryhmaLimitationsAreValid(haettuKayttoOikeusRyhma.getKayttoOikeusRyhma().getId());

        // Post validation
        BindException errors = new BindException(haettuKayttoOikeusRyhma, "haettuKayttoOikeusRyhma");
        this.haettuKayttooikeusryhmaValidator.validate(haettuKayttoOikeusRyhma, errors);
        if(errors.hasErrors()) {
            throw new UnprocessableEntityException(errors);
        }

        Henkilo kasittelija = this.henkiloRepository.findByOidHenkilo(this.permissionCheckerService.getCurrentUserOid())
                .orElseThrow(() -> new NotFoundException("Kasittelija not found with oid " + this.permissionCheckerService.getCurrentUserOid()));
        haettuKayttoOikeusRyhma.getAnomus().setKasittelija(kasittelija);

        Henkilo anoja = this.henkiloRepository.findByOidHenkilo(haettuKayttoOikeusRyhma.getAnomus().getHenkilo().getOidHenkilo())
                .orElseThrow(() -> new NotFoundException("Anoja not found with oid "
                        + haettuKayttoOikeusRyhma.getAnomus().getHenkilo().getOidHenkilo()));

        this.updateHaettuKayttooikeusryhmaAndAnomus(updateHaettuKayttooikeusryhmaDto, haettuKayttoOikeusRyhma);

        // Event is created only when kayttooikeus has been granted.
        if(KayttoOikeudenTila.valueOf(updateHaettuKayttooikeusryhmaDto.getKayttoOikeudenTila()) == KayttoOikeudenTila.MYONNETTY) {
            this.grantKayttooikeusryhma(updateHaettuKayttooikeusryhmaDto.getAlkupvm(),
                    updateHaettuKayttooikeusryhmaDto.getLoppupvm(),
                    anoja.getOidHenkilo(),
                    haettuKayttoOikeusRyhma.getAnomus().getOrganisaatioOid(),
                    haettuKayttoOikeusRyhma.getKayttoOikeusRyhma(),
                    haettuKayttoOikeusRyhma.getAnomus().getTehtavanimike());
        }

        // If everything is handled on anomus send email notification to anoja.
        if(haettuKayttoOikeusRyhma.getAnomus().getMyonnettyKayttooikeusRyhmas().isEmpty()) {
            this.emailService.sendEmailAnomusAccepted(haettuKayttoOikeusRyhma.getAnomus());
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

    private void updateHaettuKayttooikeusryhmaAndAnomus(UpdateHaettuKayttooikeusryhmaDto updateHaettuKayttooikeusryhmaDto,
                                                        HaettuKayttoOikeusRyhma haettuKayttoOikeusRyhma) {
        haettuKayttoOikeusRyhma.setTyyppi(KayttoOikeudenTila.valueOf(updateHaettuKayttooikeusryhmaDto.getKayttoOikeudenTila()));
        haettuKayttoOikeusRyhma.setKasittelyPvm(DateTime.now());
        if(haettuKayttoOikeusRyhma.getAnomus().getHaettuKayttoOikeusRyhmas().size() == 1) {
            // This is the last kayttooikeus on anomus so we mark anomus as KASITELTY or HYLATTY
            haettuKayttoOikeusRyhma.getAnomus().setAnomuksenTila(haettuKayttoOikeusRyhma.getAnomus().getHaettuKayttoOikeusRyhmas()
                    .stream().map(HaettuKayttoOikeusRyhma::getTyyppi).filter(KayttoOikeudenTila.MYONNETTY::equals)
                    .map(x -> AnomuksenTila.KASITELTY).findFirst()
                    .orElse(AnomuksenTila.HYLATTY));
        }
        haettuKayttoOikeusRyhma.getAnomus().setAnomusTilaTapahtumaPvm(new DateTime());

        // Remove the currently handled kayttooikeus from anomus
        haettuKayttoOikeusRyhma.getAnomus().getHaettuKayttoOikeusRyhmas().remove(haettuKayttoOikeusRyhma);
    }

    private OrganisaatioHenkilo findOrCreateHaettuOrganisaatioHenkilo(String organisaatioOid, Henkilo anoja, String tehtavanimike) {
        return anoja.getOrganisaatioHenkilos().stream()
                .filter(organisaatioHenkilo ->
                        Objects.equals(organisaatioHenkilo.getOrganisaatioOid(), organisaatioOid))
                .findFirst().orElseGet(() ->
                anoja.addOrganisaatioHenkilo(OrganisaatioHenkilo.builder()
                        .organisaatioOid(organisaatioOid)
                        .tehtavanimike(tehtavanimike)
                        .henkilo(anoja)
                        .build()));
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
        return this.anomusDataRepository.save(anomus).getId();
    }

    // Grant kayttooikeusryhma and create event. DOES NOT CONTAIN PERMISSION CHECKS SO DONT CALL DIRECTLY
    private void grantKayttooikeusryhma(LocalDate alkupvm,
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

        this.createHistoryEvent(myonnettyKayttoOikeusRyhmaTapahtuma,
                myonnettyKayttoOikeusRyhmaTapahtuma.getId() == null
                        ? "Oikeuksien lisäys"
                        : "Oikeuksien päivitys");

        this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository.save(myonnettyKayttoOikeusRyhmaTapahtuma);

        // TODO ldap sync for henkilo
    }

    // New history event for a change on kayttooikeusryhmatapahtuma.
    private void createHistoryEvent(MyonnettyKayttoOikeusRyhmaTapahtuma myonnettyKayttoOikeusRyhmaTapahtuma, String reason) {
        this.kayttoOikeusRyhmaTapahtumaHistoriaDataRepository.save(myonnettyKayttoOikeusRyhmaTapahtuma.toHistoria(DateTime.now(), reason));
    }

    private MyonnettyKayttoOikeusRyhmaTapahtuma findOrCreateMyonnettyKayttooikeusryhmaTapahtuma(String oidHenkilo,
                                                                                                OrganisaatioHenkilo organisaatioHenkilo,
                                                                                                KayttoOikeusRyhma kayttoOikeusRyhma) {
        return this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository.findMyonnettyTapahtuma(kayttoOikeusRyhma.getId(),
                organisaatioHenkilo.getOrganisaatioOid(), oidHenkilo)
                .orElse(MyonnettyKayttoOikeusRyhmaTapahtuma.builder()
                        .kayttoOikeusRyhma(kayttoOikeusRyhma)
                        .organisaatioHenkilo(organisaatioHenkilo).build());
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
