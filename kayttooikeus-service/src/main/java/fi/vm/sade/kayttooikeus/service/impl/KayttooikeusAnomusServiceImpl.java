package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.dto.HaettuKayttooikeusryhmaDto;
import fi.vm.sade.kayttooikeus.dto.KayttoOikeudenTila;
import fi.vm.sade.kayttooikeus.dto.UpdateHaettuKayttooikeusryhmaDto;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.*;
import fi.vm.sade.kayttooikeus.service.KayttooikeusAnomusService;
import fi.vm.sade.kayttooikeus.service.LocalizationService;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class KayttooikeusAnomusServiceImpl extends AbstractService implements KayttooikeusAnomusService {

    private final HaettuKayttooikeusRyhmaDataRepository haettuKayttooikeusRyhmaDataRepository;
    private final HenkiloRepository henkiloRepository;
    private final MyonnettyKayttoOikeusRyhmaTapahtumaDataRepository myonnettyKayttoOikeusRyhmaTapahtumaDataRepository;
    private final KayttoOikeusRyhmaMyontoViiteRepository kayttoOikeusRyhmaMyontoViiteRepository;
    private final KayttoOikeusRyhmaTapahtumaHistoriaDataRepository kayttoOikeusRyhmaTapahtumaHistoriaDataRepository;

    private final OrikaBeanMapper mapper;
    private final LocalizationService localizationService;

    private final HaettuKayttooikeusryhmaValidator haettuKayttooikeusryhmaValidator;

    @Autowired
    public KayttooikeusAnomusServiceImpl(HaettuKayttooikeusRyhmaDataRepository haettuKayttooikeusRyhmaDataRepository,
                                         HenkiloRepository henkiloRepository,
                                         MyonnettyKayttoOikeusRyhmaTapahtumaDataRepository myonnettyKayttoOikeusRyhmaTapahtumaDataRepository,
                                         KayttoOikeusRyhmaMyontoViiteRepository kayttoOikeusRyhmaMyontoViiteRepository,
                                         KayttoOikeusRyhmaTapahtumaHistoriaDataRepository kayttoOikeusRyhmaTapahtumaHistoriaDataRepository,
                                         OrikaBeanMapper orikaBeanMapper,
                                         LocalizationService localizationService,
                                         HaettuKayttooikeusryhmaValidator haettuKayttooikeusryhmaValidator) {
        this.haettuKayttooikeusRyhmaDataRepository = haettuKayttooikeusRyhmaDataRepository;
        this.henkiloRepository = henkiloRepository;
        this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository = myonnettyKayttoOikeusRyhmaTapahtumaDataRepository;
        this.kayttoOikeusRyhmaMyontoViiteRepository = kayttoOikeusRyhmaMyontoViiteRepository;
        this.kayttoOikeusRyhmaTapahtumaHistoriaDataRepository = kayttoOikeusRyhmaTapahtumaHistoriaDataRepository;
        this.mapper = orikaBeanMapper;
        this.localizationService = localizationService;
        this.haettuKayttooikeusryhmaValidator = haettuKayttooikeusryhmaValidator;
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

    // TODO kommentoi
    @Transactional
    @Override
    public void updateHaettuKayttooikeusryhma(UpdateHaettuKayttooikeusryhmaDto updateHaettuKayttooikeusryhmaDto) {
        HaettuKayttoOikeusRyhma haettuKayttoOikeusRyhma = this.haettuKayttooikeusRyhmaDataRepository
                .findOne(updateHaettuKayttooikeusryhmaDto.getId())
                .orElseThrow(() -> new NotFoundException("Haettua kayttooikeusryhmaa ei löytynyt id:llä "
                        + updateHaettuKayttooikeusryhmaDto.getId()));

        // Post validation
        BindException errors = new BindException(haettuKayttoOikeusRyhma, "haettuKayttoOikeusRyhma");
        this.haettuKayttooikeusryhmaValidator.validate(haettuKayttoOikeusRyhma, errors);
        if(errors.hasErrors()) {
            throw new UnprocessableEntityException(errors);
        }

        Henkilo kasittelija = this.henkiloRepository.findByOidHenkilo(this.getCurrentUserOid())
                .orElseThrow(() -> new NotFoundException("Kasittelija not found with oid " + this.getCurrentUserOid()));
        haettuKayttoOikeusRyhma.getAnomus().setKasittelija(kasittelija);

        Henkilo anoja = this.henkiloRepository.findByOidHenkilo(haettuKayttoOikeusRyhma.getAnomus().getHenkilo().getOidHenkilo())
                .orElseThrow(() -> new NotFoundException("Anoja not found with oid "
                        + haettuKayttoOikeusRyhma.getAnomus().getHenkilo().getOidHenkilo()));
        OrganisaatioHenkilo haettuOrganisaatioHenkilo = findOrCreateHaettuOrganisaatioHenkilo(haettuKayttoOikeusRyhma, anoja);
        List<Long> slaves = getSlaveIdsByMasterIdsForKasittelija(kasittelija);
        //  If the granting person has group limitation, those limitations reduce the possible set of requested access rights
        //  and only those which IDs match are granted, but if the granting person doesn't have any limitations, then all
        //  requested access rights are handled
        KayttoOikeusRyhma myonnettavaKayttoOikeusRyhma = Optional.ofNullable(haettuKayttoOikeusRyhma.getKayttoOikeusRyhma())
                .filter(kayttoOikeusRyhma -> slaves.isEmpty() || slaves.contains(kayttoOikeusRyhma.getId()))
                .orElseThrow(ForbiddenException::new);

        updateHaettuKayttooikeusryhmaAndAnomus(updateHaettuKayttooikeusryhmaDto, haettuKayttoOikeusRyhma);

        // Event is created only when kayttooikeus has been granted.
        if(updateHaettuKayttooikeusryhmaDto.getKayttoOikeudenTila() == KayttoOikeudenTila.MYONNETTY) {
            this.grantRequisition(updateHaettuKayttooikeusryhmaDto, kasittelija, anoja, haettuOrganisaatioHenkilo, myonnettavaKayttoOikeusRyhma);
        }
    }

    private List<Long> getSlaveIdsByMasterIdsForKasittelija(Henkilo kasittelija) {
        return this.kayttoOikeusRyhmaMyontoViiteRepository.getSlaveIdsByMasterIds(
                this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository.findValidMyonnettyKayttooikeus(kasittelija.getOidHenkilo())
                        .stream().map(MyonnettyKayttoOikeusRyhmaTapahtuma::getKayttoOikeusRyhma)
                        .map(IdentifiableAndVersionedEntity::getId)
                        .collect(Collectors.toList()));
    }

    private void updateHaettuKayttooikeusryhmaAndAnomus(UpdateHaettuKayttooikeusryhmaDto updateHaettuKayttooikeusryhmaDto, HaettuKayttoOikeusRyhma haettuKayttoOikeusRyhma) {
        haettuKayttoOikeusRyhma.setTyyppi(updateHaettuKayttooikeusryhmaDto.getKayttoOikeudenTila());
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

    private OrganisaatioHenkilo findOrCreateHaettuOrganisaatioHenkilo(HaettuKayttoOikeusRyhma haettuKayttoOikeusRyhma, Henkilo anoja) {
        return anoja.getOrganisaatioHenkilos().stream()
                .filter(organisaatioHenkilo ->
                        Objects.equals(organisaatioHenkilo.getOrganisaatioOid(), haettuKayttoOikeusRyhma.getAnomus().getOrganisaatioOid()))
                .findFirst().orElseGet(() ->
                anoja.addOrganisaatioHenkilo(OrganisaatioHenkilo.builder()
                        .organisaatioOid(haettuKayttoOikeusRyhma.getAnomus().getOrganisaatioOid())
                        .tehtavanimike(haettuKayttoOikeusRyhma.getAnomus().getTehtavanimike())
                        .build()));
    }

    // Grant kayttooikeusryhma and create event
    private void grantRequisition(UpdateHaettuKayttooikeusryhmaDto updateHaettuKayttooikeusryhmaDto,
                                  Henkilo kasittelija,
                                  Henkilo anoja,
                                  OrganisaatioHenkilo myonnettavaOrganisaatioHenkilo,
                                  KayttoOikeusRyhma myonnettavaKayttoOikeusRyhma) {

        MyonnettyKayttoOikeusRyhmaTapahtuma myonnettyKayttoOikeusRyhmaTapahtuma =
                this.findOrCreateMyonnettyKayttooikeusryhmaTapahtuma(anoja.getOidHenkilo(), myonnettavaOrganisaatioHenkilo,
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

    // New history event for change on kayttooikeusryhmatapahtuma.
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

    private List<HaettuKayttooikeusryhmaDto> localizeKayttooikeusryhma(List<HaettuKayttooikeusryhmaDto> unlocalizedDtos) {
        unlocalizedDtos
                .forEach(haettuKayttooikeusryhmaDto -> haettuKayttooikeusryhmaDto
                        .setKayttoOikeusRyhma(localizationService.localize(haettuKayttooikeusryhmaDto.getKayttoOikeusRyhma())));
        return unlocalizedDtos;
    }
}
