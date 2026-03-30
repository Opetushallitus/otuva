package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.dto.*;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.Kayttajatiedot;
import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.model.Kutsu;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.HenkiloHibernateRepository;
import fi.vm.sade.kayttooikeus.repositories.KayttajatiedotRepository;
import fi.vm.sade.kayttooikeus.repositories.KutsuRepository;
import fi.vm.sade.kayttooikeus.repositories.OrganisaatioHenkiloRepository;
import fi.vm.sade.kayttooikeus.repositories.criteria.OrganisaatioHenkiloCriteria;
import fi.vm.sade.kayttooikeus.repositories.dto.HenkilohakuResultDto;
import fi.vm.sade.kayttooikeus.service.*;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.kayttooikeus.util.YhteystietoUtil;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloCreateDto;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloDto;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloHakuCriteria;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloUpdateDto;
import fi.vm.sade.oppijanumerorekisteri.dto.KielisyysDto;
import fi.vm.sade.oppijanumerorekisteri.dto.YhteystiedotRyhmaDto;
import fi.vm.sade.oppijanumerorekisteri.dto.YhteystietoDto;
import fi.vm.sade.oppijanumerorekisteri.dto.YhteystietoTyyppi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class VirkailijaServiceImpl implements VirkailijaService {
    private final PermissionCheckerService permissionCheckerService;
    private final KayttajatiedotService kayttajatiedotService;
    private final OrganisaatioHenkiloService organisaatioHenkiloService;
    private final KayttooikeusAnomusService kayttooikeusAnomusService;
    private final CryptoService cryptoService;

    private final OrganisaatioHenkiloRepository organisaatioHenkiloRepository;
    private final HenkiloHibernateRepository henkiloHibernateRepository;
    private final HenkiloDataRepository henkiloRepository;
    private final KayttajatiedotRepository kayttajatiedotRepository;
    private final KutsuRepository kutsuRepository;

    private final OppijanumerorekisteriClient oppijanumerorekisteriClient;
    private final OrganisaatioClient organisaatioClient;

    private final CommonProperties commonProperties;
    private final OrikaBeanMapper mapper;

    private static final String USERNAME_CHARS = "abcdefghijklmnopqrstuvwxyz1234567890";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String create(VirkailijaCreateDto createDto) {
        String kayttajatunnus = createDto.getKayttajatunnus();
        String salasana = createDto.getSalasana();

        // validointi (suoritettava ennen kuin oid luodaan oppijanumerorekisteriin)
        kayttajatiedotService.throwIfUsernameIsNotValid(kayttajatunnus);
        kayttajatiedotService.throwIfUsernameExists(kayttajatunnus);
        cryptoService.throwIfNotStrongPassword(salasana);

        // luodaan oid oppijanumerorekisteriin
        HenkiloCreateDto henkiloCreateDto = mapper.map(createDto, HenkiloCreateDto.class);
        String oid = oppijanumerorekisteriClient.createHenkilo(henkiloCreateDto);

        // tallennetaan virkailijaksi käyttöoikeuspalveluun
        Henkilo entity = henkiloRepository.findByOidHenkilo(oid).orElseGet(() -> new Henkilo(oid));
        mapper.map(createDto, entity);
        entity.setKayttajaTyyppi(KayttajaTyyppi.VIRKAILIJA);
        Kayttajatiedot kayttajatiedot = new Kayttajatiedot();
        kayttajatiedot.setUsername(kayttajatunnus);
        String salt = cryptoService.generateSalt();
        String hash = cryptoService.getSaltedHash(salasana, salt);
        kayttajatiedot.setSalt(salt);
        kayttajatiedot.setPassword(hash);
        kayttajatiedot.setHenkilo(entity);
        entity.setKayttajatiedot(kayttajatiedot);
        henkiloRepository.save(entity);

        return oid;
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<KayttajaReadDto> list(VirkailijaCriteriaDto criteria) {
        log.info("Haetaan käyttäjät {}", criteria);

        if (criteria.getOrganisaatioOids() == null && criteria.getKayttooikeudet() == null && criteria.getKayttoOikeusRyhmaNimet() == null) {
            throw new IllegalArgumentException("Pakollinen hakuehto (organisaatioOids, kayttooikeudet tai kayttoOikeusRyhmaNimet) puuttuu");
        }

        Set<String> henkiloOids = getHenkiloOids(criteria);
        if (henkiloOids.isEmpty()) {
            return emptyList();
        }

        HenkiloHakuCriteria oppijanumerorekisteriCriteria = mapper.map(criteria, HenkiloHakuCriteria.class);
        oppijanumerorekisteriCriteria.setHenkiloOids(henkiloOids);
        return oppijanumerorekisteriClient.listYhteystiedot(oppijanumerorekisteriCriteria).stream()
                .map(henkilo -> mapper.map(henkilo, KayttajaReadDto.class))
                .collect(toList());
    }

    private Set<String> getHenkiloOids(VirkailijaCriteriaDto kayttajaCriteria) {
        OrganisaatioHenkiloCriteria organisaatioHenkiloCriteria = mapper.map(kayttajaCriteria, OrganisaatioHenkiloCriteria.class);

        String kayttajaOid = permissionCheckerService.getCurrentUserOid();
        List<String> organisaatioOids = organisaatioHenkiloRepository.findUsersOrganisaatioHenkilosByPalveluRoolis(
                kayttajaOid, PalveluRooliGroup.KAYTTAJAHAKU);
        if (!organisaatioOids.contains(commonProperties.getRootOrganizationOid())) {
            organisaatioHenkiloCriteria.setOrRetainOrganisaatioOids(organisaatioOids);
        }

        return henkiloHibernateRepository.findOidsBy(organisaatioHenkiloCriteria);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<HenkilohakuResultDto> virkailijahaku(HenkilohakuCriteria criteria) {
        criteria.setOrganisaatioOids(getOrganisaatioOids(criteria));
        Set<HenkilohakuResultDto> result = henkiloHibernateRepository.findHenkiloByCriteria(criteria, KayttajaTyyppi.VIRKAILIJA);
        return organisaatioHenkiloService.addOrganisaatioInformation(result);
    }

    private Set<String> getOrganisaatioOids(HenkilohakuCriteria criteria) {
        List<String> userOrganisaatioOids = organisaatioHenkiloRepository
                .findUsersOrganisaatioHenkilosByPalveluRoolis(permissionCheckerService.getCurrentUserOid(), PalveluRooliGroup.HENKILOHAKU);
        return userOrganisaatioOids.contains(commonProperties.getRootOrganizationOid())
            ? getOrganisaatioOidsForOphUser(criteria)
            : getOrganisaatioOidsForNonOphUser(criteria, userOrganisaatioOids);
    }

    private Set<String> getOrganisaatioOidsForOphUser(HenkilohakuCriteria criteria) {
        if (criteria.getOrganisaatioOids() == null) {
            // OPH user searches from all organisations including henkilos without organisation
            return null;
        }
        if (criteria.getOrganisaatioOids().contains(commonProperties.getRootOrganizationOid())
                && Boolean.TRUE.equals(criteria.getSubOrganisation())) {
            // this case is for performance optimization. this will find henkilos without organisation also which is kind of unintuitive.
            return null;
        }
        if (Boolean.TRUE.equals(criteria.getSubOrganisation())) {
            return criteria.getOrganisaatioOids().stream()
                .flatMap(organisaatioOid -> Stream.concat(
                        Stream.of(organisaatioOid),
                        organisaatioClient.getChildOids(organisaatioOid).stream()
                ))
                .collect(toSet());
        }
        return criteria.getOrganisaatioOids();
    }

    private Set<String> getOrganisaatioOidsForNonOphUser(HenkilohakuCriteria criteria, List<String> userOrganisaatioOids) {
        Set<String> organisaatioOids = criteria.getOrganisaatioOids() != null
            ? criteria.getOrganisaatioOids()
            : new HashSet<>(userOrganisaatioOids);
        if (Boolean.TRUE.equals(criteria.getSubOrganisation())) {
            organisaatioOids.addAll(
                organisaatioOids.stream()
                    .flatMap(organisaatioOid -> organisaatioClient.getChildOids(organisaatioOid).stream())
                    .collect(toSet()));
        }
        Set<String> kayttajaOrganisaatioOids = userOrganisaatioOids.stream()
                .flatMap(organisaatioOid -> Stream.concat(
                        Stream.of(organisaatioOid),
                        organisaatioClient.getChildOids(organisaatioOid).stream()))
                .collect(toSet());
        organisaatioOids.retainAll(kayttajaOrganisaatioOids);
        return organisaatioOids;
    }

    @Override
    public String register(VirkailijaRegistration dto) {
        var validSince = LocalDateTime.now().minusMonths(1);
        Kutsu kutsu = kutsuRepository
                .findBySalaisuusAndAikaleimaGreaterThanAndTila(dto.getToken(), validSince, KutsunTila.AVOIN)
                .orElseThrow(() -> new NotFoundException("Could not find kutsu by token " + dto.getToken()));
        Optional<HenkiloDto> existingHenkilo = oppijanumerorekisteriClient.getHenkiloByHetu(dto.getHetu());
        String henkiloOid = existingHenkilo.isPresent()
            ? existingHenkilo.get().getOidHenkilo()
            : createHenkilo(dto, kutsu);
        createVirkailija(henkiloOid, kutsu, dto);
        updateRegistrationKutsu(henkiloOid, kutsu);
        existingHenkilo.ifPresent((h) -> updateExistingHenkilo(h, kutsu));
        return henkiloOid;
    }

    private void createVirkailija(String henkiloOid, Kutsu kutsu, VirkailijaRegistration dto) {
        Henkilo henkilo = henkiloRepository.findByOidHenkilo(henkiloOid)
                .orElseGet(() -> henkiloRepository.save(
                    Henkilo.builder()
                        .oidHenkilo(henkiloOid)
                        .etunimetCached(dto.getEtunimet())
                        .sukunimiCached(dto.getSukunimi())
                        .hetuCached(dto.getHetu())
                        .build()));
        henkilo.setKayttajaTyyppi(KayttajaTyyppi.VIRKAILIJA);
        henkilo.setPassivoituCached(false);
        henkilo.setVahvastiTunnistettu(true);
        var kayttaja = kayttajatiedotRepository.findByHenkiloOidHenkilo(henkiloOid);
        if (kayttaja.isEmpty() || kayttaja.get().getUsername() == null) {
            kayttajatiedotService.createOrUpdateUsername(henkiloOid, generateRandomUsername());
        }
        kutsu.getOrganisaatiot().forEach(kutsuOrganisaatio -> {
            Set<KayttoOikeusRyhma> kayttooikeusRyhmas = kutsuOrganisaatio.getRyhmat().stream()
                    .filter(kayttoOikeusRyhma -> kayttoOikeusRyhma.isSallittuKayttajatyypilla(KayttajaTyyppi.VIRKAILIJA))
                    .filter(kayttoOikeusRyhma -> !kayttoOikeusRyhma.isPassivoitu())
                    .collect(toSet());
            kayttooikeusAnomusService.grantPreValidatedKayttooikeusryhma(
                    henkiloOid,
                    kutsuOrganisaatio.getOrganisaatioOid(),
                    Optional.ofNullable(kutsuOrganisaatio.getVoimassaLoppuPvm())
                            .orElseGet(() -> LocalDate.now().plusYears(1)),
                    kayttooikeusRyhmas,
                    kutsu.getKutsuja());
        });
    }

    private String generateRandomUsername() {
        String username;
        do {
            username = "user-" + randomString(10);
        } while (kayttajatiedotRepository.findOidByUsername(username).isPresent());
        return username;
    }

    private String randomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(USERNAME_CHARS.length());
            sb.append(USERNAME_CHARS.charAt(index));
        }
        return sb.toString();
    }

    private void updateRegistrationKutsu(String henkiloOid, Kutsu kutsu) {
        kutsu.setKaytetty(LocalDateTime.now());
        kutsu.setTemporaryToken(null);
        kutsu.setLuotuHenkiloOid(henkiloOid);
        kutsu.setTila(KutsunTila.KAYTETTY);
    }

    private String createHenkilo(VirkailijaRegistration dto, Kutsu kutsu) {
        HenkiloCreateDto henkiloCreateDto = new HenkiloCreateDto();
        henkiloCreateDto.setHetu(dto.getHetu());
        henkiloCreateDto.setAsiointiKieli(new KielisyysDto(kutsu.getKieliKoodi(), kutsu.getKieliKoodi()));
        henkiloCreateDto.setEtunimet(dto.getEtunimet());
        henkiloCreateDto.setSukunimi(dto.getSukunimi());
        henkiloCreateDto.setKutsumanimi(dto.getEtunimet().split(" ")[0]);
        henkiloCreateDto.setYhteystiedotRyhma(Set.of(YhteystiedotRyhmaDto.builder()
                .ryhmaAlkuperaTieto(commonProperties.getYhteystiedotRyhmaAlkuperaVirkailijaUi())
                .ryhmaKuvaus(commonProperties.getYhteystiedotRyhmaKuvausTyoosoite())
                .yhteystieto(YhteystietoDto.builder()
                        .yhteystietoArvo(kutsu.getSahkoposti())
                        .yhteystietoTyyppi(YhteystietoTyyppi.YHTEYSTIETO_SAHKOPOSTI)
                        .build()).build()));
        return oppijanumerorekisteriClient.createHenkilo(henkiloCreateDto);
    }

    private void updateExistingHenkilo(HenkiloDto henkilo, Kutsu kutsu) {
        HenkiloUpdateDto henkiloUpdateDto = new HenkiloUpdateDto();
        henkiloUpdateDto.setOidHenkilo(henkilo.getOidHenkilo());
        henkiloUpdateDto.setPassivoitu(false);
        addEmailToHenkiloUpdate(henkiloUpdateDto, kutsu.getSahkoposti(), henkilo);
        oppijanumerorekisteriClient.updateHenkilo(henkiloUpdateDto);
    }

    public void addEmailToHenkiloUpdate(HenkiloUpdateDto henkiloUpdateDto, String kutsuSahkoposti, HenkiloDto henkilo) {
        Set<YhteystiedotRyhmaDto> yhteystiedotRyhma = new HashSet<>();
        if (henkilo.getYhteystiedotRyhma() != null) {
            yhteystiedotRyhma.addAll(henkilo.getYhteystiedotRyhma());
        }
        boolean missingKutsuEmail = yhteystiedotRyhma.stream()
                .flatMap(yhteystiedotRyhmaDto -> yhteystiedotRyhmaDto.getYhteystieto().stream())
                .map(YhteystietoDto::getYhteystietoArvo)
                .noneMatch(kutsuSahkoposti::equals);
        if (missingKutsuEmail) {
            YhteystietoDto yhteystietoDto = new YhteystietoDto(YhteystietoTyyppi.YHTEYSTIETO_SAHKOPOSTI, kutsuSahkoposti);
            Set<YhteystietoDto> yhteystietoDtos = new HashSet<>();
            yhteystietoDtos.add(yhteystietoDto);
            yhteystiedotRyhma.add(new YhteystiedotRyhmaDto(null, YhteystietoUtil.TYOOSOITE, "alkupera6", false, yhteystietoDtos));
        }
        henkiloUpdateDto.setYhteystiedotRyhma(yhteystiedotRyhma);
    }
}
