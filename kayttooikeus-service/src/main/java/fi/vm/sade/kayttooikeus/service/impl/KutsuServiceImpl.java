package fi.vm.sade.kayttooikeus.service.impl;

import com.google.common.collect.Sets;
import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.dto.*;
import fi.vm.sade.kayttooikeus.dto.enumeration.KutsuView;
import fi.vm.sade.kayttooikeus.enumeration.KutsuOrganisaatioOrder;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.*;
import fi.vm.sade.kayttooikeus.repositories.criteria.KutsuCriteria;
import fi.vm.sade.kayttooikeus.repositories.criteria.MyontooikeusCriteria;
import fi.vm.sade.kayttooikeus.repositories.dto.HenkiloCreateByKutsuDto;
import fi.vm.sade.kayttooikeus.service.*;
import fi.vm.sade.kayttooikeus.service.exception.DataInconsistencyException;
import fi.vm.sade.kayttooikeus.service.exception.ForbiddenException;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.exception.ValidationException;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.kayttooikeus.service.validators.KutsujaValidator;
import fi.vm.sade.kayttooikeus.util.OrganisaatioMyontoPredicate;
import fi.vm.sade.kayttooikeus.util.YhteystietoUtil;
import fi.vm.sade.oppijanumerorekisteri.dto.*;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

import static fi.vm.sade.kayttooikeus.dto.KutsunTila.AVOIN;
import static fi.vm.sade.kayttooikeus.model.Identification.HAKA_AUTHENTICATION_IDP;
import static fi.vm.sade.kayttooikeus.service.impl.PermissionCheckerServiceImpl.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;

@Service
@RequiredArgsConstructor
public class KutsuServiceImpl implements KutsuService {
    private final KutsuRepository kutsuRepository;
    private final HenkiloDataRepository henkiloDataRepository;

    private final OrikaBeanMapper mapper;

    private final EmailService emailService;
    private final LocalizationService localizationService;
    private final CryptoService cryptoService;
    private final KayttajatiedotService kayttajatiedotService;
    private final KayttooikeusAnomusService kayttooikeusAnomusService;
    private final IdentificationService identificationService;
    private final PermissionCheckerService permissionCheckerService;

    private final OppijanumerorekisteriClient oppijanumerorekisteriClient;
    private final OrganisaatioClient organisaatioClient;

    private final MyonnettyKayttoOikeusRyhmaTapahtumaRepository myonnettyKayttoOikeusRyhmaTapahtumaRepository;
    private final IdentificationRepository identificationRepository;
    private final KutsujaValidator kutsujaValidator;

    private final CommonProperties commonProperties;

    @Value("${kayttooikeus.kutsu.allowlist-oids}")
    private String kutsuAllowlistOids;

    @Override
    @Transactional(readOnly = true)
    public List<KutsuReadDto> listKutsus(KutsuOrganisaatioOrder sortBy,
                                         Sort.Direction direction,
                                         KutsuCriteria kutsuCriteria,
                                         Long offset,
                                         Long amount) {
        setCriteriaByView(kutsuCriteria);
        setCriteriaByUser(kutsuCriteria);
        List<Kutsu> kutsut = kutsuRepository.listKutsuListDtos(kutsuCriteria, sortBy.getSortWithDirection(direction), offset, amount);
        List<KutsuReadDto> result = mapper.mapAsList(kutsut, KutsuReadDto.class);
        result.forEach(kutsuReadDto -> localizationService.localizeOrgs(kutsuReadDto.getOrganisaatiot()));
        return result;
    }

    private void setCriteriaByView(KutsuCriteria kutsuCriteria) {
        if (KutsuView.OPH.equals(kutsuCriteria.getView())) {
            kutsuCriteria.setKutsujaOrganisaatioOid(commonProperties.getRootOrganizationOid());
            kutsuCriteria.setSubOrganisations(false);
        } else if (KutsuView.ONLY_OWN_KUTSUS.equals(kutsuCriteria.getView())) {
            kutsuCriteria.setKutsujaOid(permissionCheckerService.getCurrentUserOid());
        } else if (KutsuView.KAYTTOOIKEUSRYHMA.equals(kutsuCriteria.getView())) {
            kutsuCriteria.setKutsujaKayttooikeusryhmaIds(myonnettyKayttoOikeusRyhmaTapahtumaRepository
                    .findValidMyonnettyKayttooikeus(permissionCheckerService.getCurrentUserOid()).stream()
                    .map(MyonnettyKayttoOikeusRyhmaTapahtuma::getKayttoOikeusRyhma)
                    .map(KayttoOikeusRyhma::getId)
                    .collect(Collectors.toSet()));
        }
    }

    private void setCriteriaByUser(KutsuCriteria kutsuCriteria) {
        if (permissionCheckerService.isCurrentUserAdmin()) {
            return;
        } else if (permissionCheckerService.isCurrentUserMiniAdmin(PALVELU_KAYTTOOIKEUS, ROLE_CRUD, ROLE_KUTSU_CRUD)) {
            kutsuCriteria.setKutsujaOrganisaatioOid(commonProperties.getRootOrganizationOid());
        } else {
            Set<String> organisaatioOidLimit;
            Map<String, List<String>> palveluRoolit = new HashMap<>();
            palveluRoolit.put(PALVELU_KAYTTOOIKEUS, asList(ROLE_CRUD, ROLE_KUTSU_CRUD));
            if (!CollectionUtils.isEmpty(kutsuCriteria.getOrganisaatioOids())) {
                organisaatioOidLimit = permissionCheckerService
                        .hasOrganisaatioInHierarchy(kutsuCriteria.getOrganisaatioOids(), palveluRoolit);
            } else {
                organisaatioOidLimit = permissionCheckerService.getCurrentUserOrgnisationsWithPalveluRole(palveluRoolit);
            }
            if (Boolean.TRUE.equals(kutsuCriteria.getSubOrganisations())) {
                organisaatioOidLimit = organisaatioOidLimit.stream()
                        .flatMap(organisaatioOid -> organisaatioClient.listWithChildOids(organisaatioOid,
                                new OrganisaatioMyontoPredicate(false)).stream())
                        .collect(Collectors.toSet());
            }
            kutsuCriteria.setOrganisaatioOids(organisaatioOidLimit);
        }
    }

    private String validateAndGetKutsujaOid(KutsuCreateDto kutsuCreateDto, String kayttajaOid) {
        Henkilo kayttaja = henkiloDataRepository.findByOidHenkilo(kayttajaOid)
                .orElseThrow(() -> new DataInconsistencyException(String.format("Käyttäjää '%s' ei löytynyt", kayttajaOid)));
        String kutsujaOid = getKutsujaOid(kayttaja, kutsuCreateDto);
        if (!Objects.equals(kayttajaOid, kutsujaOid)) {
            throwIfNotInHierarchy(kutsuCreateDto);
        }
        boolean currentUserHasGrantPrivileges = this.permissionCheckerService.isCurrentUserAdmin()
                || this.permissionCheckerService.isCurrentUserMiniAdmin(PALVELU_KAYTTOOIKEUS, ROLE_KUTSU_CRUD);
        if (!currentUserHasGrantPrivileges) {
            this.organisaatioViiteLimitationsAreValidThrows(kutsuCreateDto.getOrganisaatiot());
            this.kayttooikeusryhmaLimitationsAreValid(kutsujaOid, kutsuCreateDto.getOrganisaatiot());
        }
        if (!kutsujaValidator.isKutsujaYksiloity(kutsujaOid)) {
            throw new ForbiddenException("To create new invitation user needs to have hetu and be identified from VTJ");
        }
        return kutsujaOid;
    }

    @Override
    @Transactional
    public long createKutsu(KutsuCreateDto kutsuCreateDto) {
        final String kayttajaOid = this.permissionCheckerService.getCurrentUserOid();
        List<String> allowedKutsuOids = Arrays.asList(kutsuAllowlistOids.split(","));
        String kutsujaOid = allowedKutsuOids.contains(kayttajaOid)
            ? kutsuCreateDto.getKutsujaOid()
            : validateAndGetKutsujaOid(kutsuCreateDto, kayttajaOid);

        final Kutsu newKutsu = mapper.map(kutsuCreateDto, Kutsu.class);
        this.validateKayttooikeusryhmat(newKutsu);
        newKutsu.setId(null);
        newKutsu.setAikaleima(LocalDateTime.now());
        newKutsu.setKutsuja(kutsujaOid);
        newKutsu.setSalaisuus(UUID.randomUUID().toString());
        newKutsu.setTila(AVOIN);
        newKutsu.getOrganisaatiot().forEach(kutsuOrganisaatio -> kutsuOrganisaatio.setKutsu(newKutsu));

        Kutsu persistedNewKutsu = this.kutsuRepository.save(newKutsu);

        emailService.sendInvitationEmail(persistedNewKutsu, Optional.ofNullable(kutsuCreateDto.getKutsujaForEmail()));

        return persistedNewKutsu.getId();
    }

    private String getKutsujaOid(Henkilo kayttaja, KutsuCreateDto kutsu) {
        KayttajaTyyppi kayttajaTyyppi = kayttaja.getKayttajaTyyppi();
        if (kayttajaTyyppi == null) {
            throw new DataInconsistencyException(String.format("Käyttäjältä '%s' puuttuu tyyppi", kayttaja.getOidHenkilo()));
        }
        switch (kayttajaTyyppi) {
            case VIRKAILIJA:
                return kayttaja.getOidHenkilo();
            case PALVELU:
                return Optional.ofNullable(kutsu.getKutsujaOid())
                        .flatMap(henkiloDataRepository::findByOidHenkilo)
                        .filter(Henkilo::isVirkailija)
                        .map(Henkilo::getOidHenkilo)
                        .orElseThrow(() -> new ValidationException("Kutsuja oid on pakollinen palvelukäyttäjänä"));
            default:
                throw new ValidationException(String.format("Käyttäjätyyppiä %s ei tueta", kayttajaTyyppi));
        }
    }

    private void validateKayttooikeusryhmat(Kutsu newKutsu) {
        Set<KayttoOikeusRyhma> myonnettavatKayttooikeusryhmat = newKutsu.getOrganisaatiot().stream()
                .flatMap(kutsuOrganisaatioDto -> kutsuOrganisaatioDto.getRyhmat().stream())
                .collect(Collectors.toSet());
        Object[] passivoidutKayttooikeusryhmat = myonnettavatKayttooikeusryhmat.stream()
                .filter(KayttoOikeusRyhma::isPassivoitu)
                .map(KayttoOikeusRyhma::getId)
                .toArray();
        if (passivoidutKayttooikeusryhmat.length > 0) {
            throw new IllegalArgumentException(String.format("Passivoituihin käyttöoikeusryhmiin %s ei voi myöntää oikeuksia", Arrays.toString(passivoidutKayttooikeusryhmat)));
        }
        Object[] eiSallitutKayttooikeusryhmat = myonnettavatKayttooikeusryhmat.stream()
                .filter(kayttoOikeusRyhma -> !kayttoOikeusRyhma.isSallittuKayttajatyypilla(KayttajaTyyppi.VIRKAILIJA))
                .map(KayttoOikeusRyhma::getId)
                .toArray();
        if (eiSallitutKayttooikeusryhmat.length > 0) {
            throw new IllegalArgumentException(String.format("Käyttöoikeusryhmiä %s ei voi myöntää käyttäjätyypille %s", Arrays.toString(eiSallitutKayttooikeusryhmat), KayttajaTyyppi.VIRKAILIJA));
        }
    }

    private void throwIfNotInHierarchy(KutsuCreateDto kutsuCreateDto) {
        Set<String> organisaatioOids = kutsuCreateDto.getOrganisaatiot().stream()
                .map(KutsuCreateDto.KutsuOrganisaatioCreateDto::getOrganisaatioOid)
                .collect(Collectors.toSet());
        this.throwIfNotInHierarchy(organisaatioOids);
    }

    private void throwIfNotInHierarchy(Collection<String> organisaatioOids) {
        Map<String, List<String>> kayttooikeudet = singletonMap(PALVELU_KAYTTOOIKEUS, asList(ROLE_CRUD, ROLE_KUTSU_CRUD));
        Set<String> accessibleOrganisationOids = this.permissionCheckerService.hasOrganisaatioInHierarchy(organisaatioOids, kayttooikeudet);
        if (organisaatioOids.size() != accessibleOrganisationOids.size()) {
            organisaatioOids.removeAll(accessibleOrganisationOids);
            throw new ForbiddenException("No access through organisation hierarchy to oids "
                    + organisaatioOids.stream().collect(Collectors.joining(", ")));
        }
    }

    private void organisaatioViiteLimitationsAreValidThrows(Collection<KutsuCreateDto.KutsuOrganisaatioCreateDto> kutsuOrganisaatioDtos) {
        kutsuOrganisaatioDtos.forEach(kutsuOrganisaatioDto -> kutsuOrganisaatioDto.getKayttoOikeusRyhmat()
                .forEach(kayttoOikeusRyhmaDto -> {
                    if (!this.permissionCheckerService.organisaatioViiteLimitationsAreValid(kutsuOrganisaatioDto.getOrganisaatioOid(), kayttoOikeusRyhmaDto.getId())) {
                        throw new ForbiddenException("Target organization has invalid organization type for group "
                                + kayttoOikeusRyhmaDto.getId());
                    }
                }));
    }

    private void kayttooikeusryhmaLimitationsAreValid(String kayttajaOid, Collection<KutsuCreateDto.KutsuOrganisaatioCreateDto> kutsuOrganisaatioDtos) {
        // The granting person's limitations must be checked always since there there shouldn't be a situation where the
        // the granting person doesn't have access rights limitations (except admin users who have full access)
        kutsuOrganisaatioDtos.forEach(kutsuOrganisaatioDto -> kutsuOrganisaatioDto.getKayttoOikeusRyhmat()
                .forEach(kayttoOikeusRyhmaDto -> {
                    if (!this.permissionCheckerService.kayttooikeusMyontoviiteLimitationCheck(kayttajaOid,
                            kutsuOrganisaatioDto.getOrganisaatioOid(), kayttoOikeusRyhmaDto.getId(),
                            MyontooikeusCriteria.kutsu())) {
                        throw new ForbiddenException("User doesn't have access rights to grant this group for group "
                                + kayttoOikeusRyhmaDto.getId());
                    }
                }));
    }

    @Override
    @Transactional(readOnly = true)
    public KutsuReadDto getKutsu(Long id) {
        Kutsu kutsu = kutsuRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Kutsu not found"));
        KutsuReadDto kutsuReadDto = mapper.map(kutsu, KutsuReadDto.class);
        this.localizationService.localizeOrgs(kutsuReadDto.getOrganisaatiot());
        return kutsuReadDto;
    }

    @Override
    @Transactional
    public Optional<Kutsu> getHakaKutsu(String temporaryToken) {
        var kutsu = kutsuRepository.findByTemporaryTokenIsValidIsActive(temporaryToken);
        if (kutsu.isPresent() && kutsu.get().getHakaIdentifier() != null) {
            return kutsu;
        } else {
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public void renewKutsu(long id) {
        Kutsu kutsuToRenew = kutsuRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Kutsu not found"));
        if (!kutsuToRenew.getKutsuja().equals(this.permissionCheckerService.getCurrentUserOid())) {
            this.throwIfNormalUserOrganisationLimitedByOrganisationHierarchy(kutsuToRenew);
        }
        kutsuToRenew.setAikaleima(LocalDateTime.now());
        kutsuToRenew = kutsuRepository.save(kutsuToRenew);
        emailService.sendInvitationEmail(kutsuToRenew);
    }

    @Override
    @Transactional
    public Kutsu deleteKutsu(long id) {
        Kutsu kutsuToDelete = kutsuRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Kutsu not found"));
        if (!kutsuToDelete.getKutsuja().equals(this.permissionCheckerService.getCurrentUserOid())) {
            this.throwIfNormalUserOrganisationLimitedByOrganisationHierarchy(kutsuToDelete);
        }
        kutsuToDelete.poista(this.permissionCheckerService.getCurrentUserOid());
        return kutsuToDelete;
    }

    private void throwIfNormalUserOrganisationLimitedByOrganisationHierarchy(Kutsu deletedKutsu) {
        if (!this.permissionCheckerService.isCurrentUserAdmin()
                && !this.permissionCheckerService.isCurrentUserMiniAdmin(PALVELU_KAYTTOOIKEUS, ROLE_CRUD, ROLE_KUTSU_CRUD)) {
            this.throwIfNotInHierarchy(deletedKutsu.getOrganisaatiot().stream()
                    .map(KutsuOrganisaatio::getOrganisaatioOid)
                    .collect(Collectors.toSet()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public KutsuReadDto getByTemporaryToken(String temporaryToken) {
        Kutsu kutsuByToken = this.kutsuRepository.findByTemporaryTokenIsValidIsActive(temporaryToken)
                .orElseThrow(() -> new NotFoundException("Could not find kutsu by token " + temporaryToken + " or token is invalid"));
        KutsuReadDto kutsuReadDto = this.mapper.map(kutsuByToken, KutsuReadDto.class);
        this.localizationService.localizeOrgs(kutsuReadDto.getOrganisaatiot());
        return kutsuReadDto;
    }

    @Override
    @Transactional
    public HenkiloUpdateDto createHenkiloWithHakaIdentifier(String temporaryToken, String hakaIdentifier) {
        Kutsu kutsu = kutsuRepository.findByTemporaryTokenIsValidIsActive(temporaryToken)
                .orElseThrow(() -> new NotFoundException("Could not find kutsu by token " + temporaryToken + " or token is invalid"));
        kutsu.setHakaIdentifier(hakaIdentifier);
        Optional<HenkiloDto> henkiloByHetu = oppijanumerorekisteriClient.getHenkiloByHetu(kutsu.getHetu());
        return createHenkilo(kutsu, getHakaHenkiloCreateByKutsuDto(kutsu, hakaIdentifier), henkiloByHetu);
    }

    private HenkiloCreateByKutsuDto getHakaHenkiloCreateByKutsuDto(Kutsu kutsu, String hakaIdentifier) {
        String kutsumanimi = kutsu.getEtunimi().contains(" ") ? kutsu.getEtunimi().split(" ")[0] : kutsu.getEtunimi();
        String parsedIdentifier = kutsu.getHakaIdentifier().replaceAll("[^A-Za-z0-9]", "");
        String username = parsedIdentifier + (new Random().nextInt(900) + 100); // 100-999
        KielisyysDto kielisyys = KielisyysDto.builder().kieliKoodi(kutsu.getKieliKoodi()).build();
        return new HenkiloCreateByKutsuDto(kutsumanimi, kielisyys, username, null);
    }

    @Override
    @Transactional
    public HenkiloUpdateDto createHenkilo(String temporaryToken, HenkiloCreateByKutsuDto henkiloCreateByKutsuDto) {
        Kutsu kutsuByToken = kutsuRepository.findByTemporaryTokenIsValidIsActive(temporaryToken)
                .orElseThrow(() -> new NotFoundException("Could not find kutsu by token " + temporaryToken + " or token is invalid"));
        Optional<HenkiloDto> henkiloByHetu = oppijanumerorekisteriClient.getHenkiloByHetu(kutsuByToken.getHetu());
        cryptoService.throwIfNotStrongPassword(henkiloCreateByKutsuDto.getPassword());
        kayttajatiedotService.throwIfUsernameExists(henkiloCreateByKutsuDto.getKayttajanimi(), henkiloByHetu.map(HenkiloDto::getOidHenkilo));
        kayttajatiedotService.throwIfUsernameIsNotValid(henkiloCreateByKutsuDto.getKayttajanimi());
        return createHenkilo(kutsuByToken, henkiloCreateByKutsuDto, henkiloByHetu);
    }

    private HenkiloUpdateDto createHenkilo(Kutsu kutsuByToken, HenkiloCreateByKutsuDto henkiloCreateByKutsuDto, Optional<HenkiloDto> henkiloByHetu) {
        // Search for existing henkilo by hetu and create new if not found
        boolean isNewHenkilo = !henkiloByHetu.isPresent();
        String henkiloOid;
        if (isNewHenkilo) {
            HenkiloCreateDto henkiloCreateDto = this.getHenkiloCreateDto(henkiloCreateByKutsuDto, kutsuByToken);
            henkiloOid = this.oppijanumerorekisteriClient.createHenkilo(henkiloCreateDto);
        } else {
            henkiloOid = henkiloByHetu.get().getOidHenkilo();
        }

        // Set henkilo strongly identified
        Henkilo henkilo = this.henkiloDataRepository.findByOidHenkilo(henkiloOid)
                .orElseGet(() -> this.henkiloDataRepository.save(Henkilo.builder().oidHenkilo(henkiloOid).build()));
        henkilo.setKayttajaTyyppi(KayttajaTyyppi.VIRKAILIJA);
        henkilo.setVahvastiTunnistettu(true);

        // Create or update credentials and add privileges if hetu not same as kutsu creator
        final String kutsujaOid = kutsuByToken.getKutsuja();
        HenkiloPerustietoDto kutsuja = this.oppijanumerorekisteriClient.getHenkilonPerustiedot(kutsujaOid)
                .orElseThrow(() -> new DataInconsistencyException("Current user not found with oid " + kutsujaOid));
        if (!StringUtils.hasLength(kutsuja.getHetu()) || !kutsuByToken.getHetu().equals(kutsuja.getHetu())) {
            this.createOrUpdateCredentialsAndPrivileges(henkiloCreateByKutsuDto, kutsuByToken, henkiloOid);
        }

        // Update kutsu
        kutsuByToken.setKaytetty(LocalDateTime.now());
        kutsuByToken.setTemporaryToken(null);
        kutsuByToken.setLuotuHenkiloOid(henkiloOid);
        kutsuByToken.setTila(KutsunTila.KAYTETTY);

        // Set henkilo to VIRKAILIJA since we don't know if he was OPPIJA before
        HenkiloUpdateDto henkiloUpdateDto = new HenkiloUpdateDto();
        henkiloUpdateDto.setOidHenkilo(henkiloOid);
        henkiloUpdateDto.setPassivoitu(false);

        // In case henkilo already exists
        henkiloUpdateDto.setKutsumanimi(henkiloCreateByKutsuDto.getKutsumanimi());

        if (isNewHenkilo) {
            addEmailToNewHenkiloUpdateDto(henkiloUpdateDto, kutsuByToken.getSahkoposti());
        } else {
            addEmailToExistingHenkiloUpdateDto(henkiloOid, kutsuByToken.getSahkoposti(), henkiloUpdateDto);
        }

        return henkiloUpdateDto;
    }


    public void addEmailToExistingHenkiloUpdateDto(String henkiloOid, String kutsuSahkoposti, HenkiloUpdateDto henkiloUpdateDto) {
        HenkiloDto henkiloDto = this.oppijanumerorekisteriClient.getHenkiloByOid(henkiloOid);
        Set<YhteystiedotRyhmaDto> yhteystiedotRyhma = new HashSet<>();

        // add existing henkilos yhteystiedot to henkiloupdate
        yhteystiedotRyhma.addAll(henkiloDto.getYhteystiedotRyhma());

        boolean missingKutsusahkoposti = henkiloDto.getYhteystiedotRyhma().stream()
                .flatMap(yhteystiedotRyhmaDto -> yhteystiedotRyhmaDto.getYhteystieto().stream())
                .map(YhteystietoDto::getYhteystietoArvo)
                .noneMatch(kutsuSahkoposti::equals);

        if (missingKutsusahkoposti) { // add kutsuemail if it doesn't exist in henkilos yhteystiedot
            YhteystietoDto yhteystietoDto = new YhteystietoDto(YhteystietoTyyppi.YHTEYSTIETO_SAHKOPOSTI, kutsuSahkoposti);
            Set<YhteystietoDto> yhteystietoDtos = new HashSet<>();
            yhteystietoDtos.add(yhteystietoDto);
            yhteystiedotRyhma.add(new YhteystiedotRyhmaDto(null, YhteystietoUtil.TYOOSOITE, "alkupera6", false, yhteystietoDtos));
        }

        henkiloUpdateDto.setYhteystiedotRyhma(yhteystiedotRyhma);
    }

    public void addEmailToNewHenkiloUpdateDto(HenkiloUpdateDto henkiloUpdateDto, String kutsuSahkoposti) {
        // Initiate new YhteystiedotRyhma with email in kutsu
        YhteystietoDto yhteystietoDto = new YhteystietoDto(YhteystietoTyyppi.YHTEYSTIETO_SAHKOPOSTI, kutsuSahkoposti);
        HashSet<YhteystietoDto> yhteystietoDtos = new HashSet<>();
        yhteystietoDtos.add(yhteystietoDto);
        Set<YhteystiedotRyhmaDto> yhteystiedotRyhma = new HashSet<>();
        yhteystiedotRyhma.add(new YhteystiedotRyhmaDto(null, YhteystietoUtil.TYOOSOITE, "alkupera6", false, yhteystietoDtos));
        henkiloUpdateDto.setYhteystiedotRyhma(yhteystiedotRyhma);
    }

    private void createOrUpdateCredentialsAndPrivileges(HenkiloCreateByKutsuDto henkiloCreateByKutsuDto, Kutsu kutsuByToken, String henkiloOid) {
        // Create username/password and haka identifier if provided
        if (StringUtils.hasLength(kutsuByToken.getHakaIdentifier())) {
            // haka-tunniste tulee olla uniikki
            identificationRepository.findByidpEntityIdAndIdentifier(HAKA_AUTHENTICATION_IDP, kutsuByToken.getHakaIdentifier()).ifPresent(identification -> {
                String oidByIdentification = identification.getHenkilo().getOidHenkilo();
                if (!henkiloOid.equals(oidByIdentification)) {
                    // yhdistetään henkilöt
                    oppijanumerorekisteriClient.yhdistaHenkilot(henkiloOid, asList(oidByIdentification));

                    // poistetaan duplikaatilta haka-tunniste
                    Set<String> identifications = identificationService.getTunnisteetByHenkiloAndIdp(HAKA_AUTHENTICATION_IDP, oidByIdentification);
                    identifications.remove(kutsuByToken.getHakaIdentifier());
                    identificationService.updateTunnisteetByHenkiloAndIdp(HAKA_AUTHENTICATION_IDP, oidByIdentification, identifications);
                }
            });
            // If haka identifier is provided add it to henkilo identifiers
            Set<String> hakaIdentifiers = this.identificationService.getTunnisteetByHenkiloAndIdp(HAKA_AUTHENTICATION_IDP, henkiloOid);
            hakaIdentifiers.add(kutsuByToken.getHakaIdentifier());
            this.identificationService.updateTunnisteetByHenkiloAndIdp(HAKA_AUTHENTICATION_IDP, henkiloOid, hakaIdentifiers);
        }
        this.kayttajatiedotService.createOrUpdateUsername(henkiloOid, henkiloCreateByKutsuDto.getKayttajanimi());
        if (!StringUtils.hasLength(kutsuByToken.getHakaIdentifier())) {
            this.kayttajatiedotService.changePasswordAsAdmin(henkiloOid, henkiloCreateByKutsuDto.getPassword());
        }

        kutsuByToken.getOrganisaatiot().forEach(kutsuOrganisaatio -> {
            // Filtteröidään ei sallitut käyttöoikeusryhmät. Virkailija voi anoa tarvitsemiaan oikeuksia päästyään palveluun.
            Set<KayttoOikeusRyhma> kayttooikeusRyhmas = kutsuOrganisaatio.getRyhmat().stream()
                    .filter(kayttoOikeusRyhma -> kayttoOikeusRyhma.isSallittuKayttajatyypilla(KayttajaTyyppi.VIRKAILIJA))
                    .filter(kayttoOikeusRyhma -> !kayttoOikeusRyhma.isPassivoitu())
                    .collect(Collectors.toSet());
            this.kayttooikeusAnomusService.grantPreValidatedKayttooikeusryhma(
                    henkiloOid,
                    kutsuOrganisaatio.getOrganisaatioOid(),
                    Optional.ofNullable(kutsuOrganisaatio.getVoimassaLoppuPvm())
                            .orElseGet(() -> LocalDate.now().plusYears(1)),
                    kayttooikeusRyhmas,
                    kutsuByToken.getKutsuja());
        });

    }

    private HenkiloCreateDto getHenkiloCreateDto(HenkiloCreateByKutsuDto henkiloCreateByKutsuDto, Kutsu kutsuByToken) {
        HenkiloCreateDto henkiloCreateDto = new HenkiloCreateDto();
        henkiloCreateDto.setHetu(kutsuByToken.getHetu());
        henkiloCreateDto.setAsiointiKieli(henkiloCreateByKutsuDto.getAsiointiKieli());
        henkiloCreateDto.setEtunimet(kutsuByToken.getEtunimi());
        henkiloCreateDto.setSukunimi(kutsuByToken.getSukunimi());
        henkiloCreateDto.setKutsumanimi(henkiloCreateByKutsuDto.getKutsumanimi());
        henkiloCreateDto.setYhteystiedotRyhma(Sets.newHashSet(YhteystiedotRyhmaDto.builder()
                .ryhmaAlkuperaTieto(this.commonProperties.getYhteystiedotRyhmaAlkuperaVirkailijaUi())
                .ryhmaKuvaus(this.commonProperties.getYhteystiedotRyhmaKuvausTyoosoite())
                .yhteystieto(YhteystietoDto.builder()
                        .yhteystietoArvo(kutsuByToken.getSahkoposti())
                        .yhteystietoTyyppi(YhteystietoTyyppi.YHTEYSTIETO_SAHKOPOSTI)
                        .build()).build()));
        return henkiloCreateDto;
    }

    @Override
    @Transactional
    // Nulls are not mapped for KutsuUpdateDto -> Kutsu
    public void updateHakaIdentifierToKutsu(String temporaryToken, KutsuUpdateDto kutsuUpdateDto) {
        Kutsu kutsu = this.kutsuRepository.findByTemporaryTokenIsValidIsActive(temporaryToken)
                .orElseThrow(() -> new NotFoundException("Could not find kutsu by token " + temporaryToken + " or token is invalid"));
        this.mapper.map(kutsuUpdateDto, kutsu);
    }

    @Override
    @Transactional
    public Collection<Kutsu> findExpired(Period threshold) {
        return kutsuRepository.findExpired(threshold);
    }

    @Override
    @Transactional
    public void discard(Kutsu invitation) {
        invitation.poista(commonProperties.getAdminOid());
        kutsuRepository.save(invitation);
        emailService.sendDiscardNotification(invitation);
    }
}
