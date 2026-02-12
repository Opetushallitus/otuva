package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.controller.PalvelukayttajaController.Jarjestelmatunnus;
import fi.vm.sade.kayttooikeus.controller.PalvelukayttajaController.Kasittelija;
import fi.vm.sade.kayttooikeus.controller.PalvelukayttajaController.Oauth2ClientCredential;
import fi.vm.sade.kayttooikeus.dto.HenkilohakuCriteria;
import fi.vm.sade.kayttooikeus.dto.JarjestelmatunnushakuCriteria;
import fi.vm.sade.kayttooikeus.dto.KayttajaTyyppi;
import fi.vm.sade.kayttooikeus.dto.KayttajatiedotCreateDto;
import fi.vm.sade.kayttooikeus.dto.PalvelukayttajaCreateDto;
import fi.vm.sade.kayttooikeus.dto.PalvelukayttajaCriteriaDto;
import fi.vm.sade.kayttooikeus.dto.PalvelukayttajaReadDto;
import fi.vm.sade.kayttooikeus.enumeration.OrderByHenkilohaku;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.Kayttajatiedot;
import fi.vm.sade.kayttooikeus.model.Oauth2Client;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.HenkiloHibernateRepository;
import fi.vm.sade.kayttooikeus.repositories.KayttajatiedotRepository;
import fi.vm.sade.kayttooikeus.repositories.Oauth2ClientRepository;
import fi.vm.sade.kayttooikeus.repositories.criteria.HenkiloCriteria;
import fi.vm.sade.kayttooikeus.repositories.dto.HenkilohakuResultDto;
import fi.vm.sade.kayttooikeus.service.KayttajatiedotService;
import fi.vm.sade.kayttooikeus.service.OrganisaatioHenkiloService;
import fi.vm.sade.kayttooikeus.service.PalvelukayttajaService;
import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloCreateDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PalvelukayttajaServiceImpl implements PalvelukayttajaService {
    private final OppijanumerorekisteriClient oppijanumerorekisteriClient;
    private final HenkiloDataRepository henkiloDataRepository;
    private final HenkiloHibernateRepository henkiloHibernateRepository;
    private final OrganisaatioClient organisaatioClient;
    private final KayttajatiedotService kayttajatiedotService;
    private final OrganisaatioHenkiloService organisaatioHenkiloService;
    private final KayttajatiedotRepository kayttajatiedotRepository;
    private final Oauth2ClientRepository oauth2ClientRepository;
    private final PasswordEncoder passwordEncoder;
    private final PermissionCheckerService permissionCheckerService;

    @Override
    @Transactional(readOnly = true)
    public List<PalvelukayttajaReadDto> list(PalvelukayttajaCriteriaDto palvelukayttajaCriteriaDto) {
        Set<String> organisaatioOids = palvelukayttajaCriteriaDto.getOrganisaatioOid() != null
            ? new HashSet<>(Arrays.asList(palvelukayttajaCriteriaDto.getOrganisaatioOid()))
            : null;
        if (organisaatioOids != null && Boolean.TRUE.equals(palvelukayttajaCriteriaDto.getSubOrganisation())) {
            organisaatioOids.addAll(organisaatioClient.getChildOids(palvelukayttajaCriteriaDto.getOrganisaatioOid()));
        }

        HenkiloCriteria criteria = HenkiloCriteria.builder()
                .sukunimi(palvelukayttajaCriteriaDto.getNameQuery())
                .kayttajatunnus(palvelukayttajaCriteriaDto.getNameQuery())
                .noOrganisation(palvelukayttajaCriteriaDto.getOrganisaatioOid() == null)
                .organisaatioOids(organisaatioOids)
                .kayttajaTyyppi(KayttajaTyyppi.PALVELU)
                .passivoitu(true)
                .build();

        return henkiloHibernateRepository.findByCriteria(criteria, 0l, null, OrderByHenkilohaku.HENKILO_NIMI_ASC.getValue())
                .stream()
                .map(h -> new PalvelukayttajaReadDto(h.getOidHenkilo(), h.getSukunimi(), h.getKayttajatunnus()))
                .toList();
    }

    private String getUniqueUsername(String palvelu) {
        String stripped = palvelu.replaceAll("[^a-zA-Z0-9_-]", "");
        if (stripped.length() >= 5 && kayttajatiedotRepository.findByUsername(stripped).isEmpty()) {
            return stripped;
        }
        String numbered;
        Random r = new Random();
        do {
            int rand = r.nextInt(9000) + 1000;
            numbered = stripped + rand;
        } while (kayttajatiedotRepository.findByUsername(numbered).isPresent());
        return numbered;
    }

    @Override
    public Jarjestelmatunnus create(PalvelukayttajaCreateDto createDto) {
        HenkiloCreateDto henkiloCreateDto = new HenkiloCreateDto();
        henkiloCreateDto.setSukunimi(createDto.getNimi());
        // oppijanumerorekisteri pakottaa näiden tietojen syöttämisen
        henkiloCreateDto.setEtunimet("_");
        henkiloCreateDto.setKutsumanimi("_");

        String oid = oppijanumerorekisteriClient.createHenkilo(henkiloCreateDto);

        Henkilo henkilo = henkiloDataRepository.findByOidHenkilo(oid).orElseGet(Henkilo::new);
        henkilo.setOidHenkilo(oid);
        henkilo.setKayttajaTyyppi(KayttajaTyyppi.PALVELU);
        henkilo.setSukunimiCached(createDto.getNimi());
        henkiloDataRepository.save(henkilo);

        String username = getUniqueUsername(createDto.getNimi());
        kayttajatiedotService.create(oid, new KayttajatiedotCreateDto(username));

        return new Jarjestelmatunnus(oid, createDto.getNimi(), username, new ArrayList<>());
    }

    private String generatePassword() {
        var randomizer = RandomStringUtils.secure();
        String password = randomizer.next(4, "!#$%*_+=?")
            .concat(randomizer.nextAlphabetic(12).toLowerCase())
            .concat(randomizer.nextAlphabetic(12).toUpperCase())
            .concat(randomizer.nextNumeric(12));
        List<String> chars = Arrays.asList(password.split(""));
        Collections.shuffle(chars);
        return chars.stream().collect(Collectors.joining());
    }

    @Override
    public String createCasPassword(String oid) {
        String password = generatePassword();
        kayttajatiedotService.changePasswordAsAdmin(oid, password);
        return password;
    }

    @Override
    public String createOauth2ClientSecret(String oid) {
        String kasittelijaOid = permissionCheckerService.getCurrentUserOid();
        Henkilo kasittelija = henkiloDataRepository.findByOidHenkilo(kasittelijaOid).get();
        Kayttajatiedot kayttaja = kayttajatiedotService.getKayttajatiedotByOidHenkilo(oid)
                .orElseThrow(() -> new NotFoundException(oid));
        String secret = generatePassword();
        String hash = passwordEncoder.encode(secret);
        Oauth2Client client = oauth2ClientRepository.findById(kayttaja.getUsername())
                .orElseGet(() -> Oauth2Client.builder()
                        .id(kayttaja.getUsername())
                        .secret(hash)
                        .uuid(UUID.randomUUID())
                        .created(LocalDateTime.now())
                        .build());
        client.setSecret(hash);
        client.setKasittelija(kasittelija);
        client.setUpdated(LocalDateTime.now());
        oauth2ClientRepository.save(client);
        return secret;
    }

    @Override
    public Jarjestelmatunnus getJarjestelmatunnus(String oid) {
        Henkilo henkilo = henkiloDataRepository.findByOidHenkilo(oid)
                .orElseThrow(() -> new NotFoundException(oid));
        String username = henkilo.getKayttajatiedot().getUsername();
        List<Oauth2ClientCredential> oauth2ClientCredentials = oauth2ClientRepository.findById(username)
                .map(client -> {
                    Henkilo k = client.getKasittelija();
                    Kasittelija kasittelija = new Kasittelija(k.getOidHenkilo(), k.getEtunimetCached(), k.getSukunimiCached(), k.getKutsumanimiCached());
                    return new Oauth2ClientCredential(client.getId(), client.getCreated(), client.getUpdated(), kasittelija);
                })
                .map(client -> List.of(client))
                .orElseGet(() -> null);
        return new Jarjestelmatunnus(oid, henkilo.getSukunimiCached(), username, oauth2ClientCredentials);
    }

    @Override
    public Set<HenkilohakuResultDto> jarjestelmatunnushaku(JarjestelmatunnushakuCriteria criteria) {
        HenkilohakuCriteria henkilohakuCriteria = new HenkilohakuCriteria(null, criteria.getQuery(), null, criteria.getKayttooikeusryhmaId());
        Set<HenkilohakuResultDto> result = henkiloHibernateRepository.findHenkiloByCriteria(henkilohakuCriteria, KayttajaTyyppi.PALVELU);
        return organisaatioHenkiloService.addOrganisaatioInformation(result);
    }
}
