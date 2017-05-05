package fi.vm.sade.kayttooikeus.service.impl;

import com.google.common.collect.Lists;
import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.dto.HaettuKayttooikeusryhmaDto;
import fi.vm.sade.kayttooikeus.dto.KayttoOikeudenTila;
import fi.vm.sade.kayttooikeus.dto.UpdateHaettuKayttooikeusryhmaDto;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.*;
import fi.vm.sade.kayttooikeus.service.KayttooikeusAnomusService;
import fi.vm.sade.kayttooikeus.service.LocalizationService;
import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;
import fi.vm.sade.kayttooikeus.service.exception.ForbiddenException;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.exception.UnprocessableEntityException;
import fi.vm.sade.kayttooikeus.service.validators.HaettuKayttooikeusryhmaValidator;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class KayttooikeusAnomusServiceImpl extends AbstractService implements KayttooikeusAnomusService {

    private final HaettuKayttooikeusRyhmaDataRepository haettuKayttooikeusRyhmaDataRepository;
    private final HenkiloRepository henkiloRepository;
    private final MyonnettyKayttoOikeusRyhmaTapahtumaDataRepository myonnettyKayttoOikeusRyhmaTapahtumaDataRepository;
    private final KayttoOikeusRyhmaMyontoViiteRepository kayttoOikeusRyhmaMyontoViiteRepository;
    private final KayttoOikeusRyhmaTapahtumaHistoriaDataRepository kayttoOikeusRyhmaTapahtumaHistoriaDataRepository;
    private final KayttooikeusryhmaDataRepository kayttooikeusryhmaDataRepository;

    private final OrikaBeanMapper mapper;
    private final LocalizationService localizationService;

    private final HaettuKayttooikeusryhmaValidator haettuKayttooikeusryhmaValidator;
    private final PermissionCheckerService permissionCheckerService;

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
                                         KayttooikeusryhmaDataRepository kayttooikeusryhmaDataRepository) {
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
                .findOne(updateHaettuKayttooikeusryhmaDto.getId())
                .orElseThrow(() -> new NotFoundException("Haettua kayttooikeusryhmaa ei löytynyt id:llä "
                        + updateHaettuKayttooikeusryhmaDto.getId()));

        // Permission checks for declining requisition (there are separate checks for granting)
        this.inSameOrParentOrganisation(haettuKayttoOikeusRyhma.getAnomus().getOrganisaatioOid());
        this.organisaatioViiteLimitationsAreValid();
        this.kayttooikeusryhmaLimitationsAreValid();

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
            this.grantKayttooikeusryhma(updateHaettuKayttooikeusryhmaDto,
                    anoja.getOidHenkilo(),
                    haettuKayttoOikeusRyhma.getAnomus().getOrganisaatioOid(),
                    haettuKayttoOikeusRyhma.getKayttoOikeusRyhma(),
                    haettuKayttoOikeusRyhma.getAnomus().getTehtavanimike());
        }
    }

    // TODO maybe move to permissionchecker as *withThrow() methods
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

    private void organisaatioViiteLimitationsAreValid() {
        // Organisaatiohenkilo limitations are valid
        // TODO organisaatioviite check
    }

    private void kayttooikeusryhmaLimitationsAreValid() {
        // Kayttooikeusryhma limitations are valid
        // TODO kayttooikeus myontoviite check
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
        haettuKayttoOikeusRyhma.getAnomus().setAnomusTilaTapahtumaPvm(new Date());

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
                                       List<UpdateHaettuKayttooikeusryhmaDto> updateHaettuKayttooikeusryhmaDtoList) {
        // Permission checks
        this.notEditingOwnData(anojaOid);
        this.inSameOrParentOrganisation(organisaatioOid);
        this.organisaatioViiteLimitationsAreValid();
        this.kayttooikeusryhmaLimitationsAreValid();

        updateHaettuKayttooikeusryhmaDtoList.forEach(haettuKayttooikeusryhmaDto ->
                this.grantKayttooikeusryhma(haettuKayttooikeusryhmaDto, anojaOid, organisaatioOid,
                        this.kayttooikeusryhmaDataRepository.findOne(haettuKayttooikeusryhmaDto.getId())
                        .orElseThrow(() -> new NotFoundException("Kayttooikeusryhma not found with id " + haettuKayttooikeusryhmaDto.getId())),
                        ""));
    }

    // Grant kayttooikeusryhma and create event. DOES NOT CONTAIN PERMISSION CHECKS SO DONT CALL DIRECTLY
    private void grantKayttooikeusryhma(UpdateHaettuKayttooikeusryhmaDto updateHaettuKayttooikeusryhmaDto,
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

        myonnettyKayttoOikeusRyhmaTapahtuma.setVoimassaAlkuPvm(updateHaettuKayttooikeusryhmaDto.getAlkupvm());
        myonnettyKayttoOikeusRyhmaTapahtuma.setVoimassaLoppuPvm(updateHaettuKayttooikeusryhmaDto.getLoppupvm());
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
        this.kayttoOikeusRyhmaTapahtumaHistoriaDataRepository.save(new KayttoOikeusRyhmaTapahtumaHistoria(
                myonnettyKayttoOikeusRyhmaTapahtuma.getKayttoOikeusRyhma(),
                myonnettyKayttoOikeusRyhmaTapahtuma.getOrganisaatioHenkilo(),
                reason,
                myonnettyKayttoOikeusRyhmaTapahtuma.getTila(),
                myonnettyKayttoOikeusRyhmaTapahtuma.getKasittelija(),
                DateTime.now()
        ));
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
}
