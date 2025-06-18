package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.controller.PalvelukayttajaController.Jarjestelmatunnus;
import fi.vm.sade.kayttooikeus.dto.KayttajaTyyppi;
import fi.vm.sade.kayttooikeus.dto.KayttajatiedotCreateDto;
import fi.vm.sade.kayttooikeus.dto.PalvelukayttajaCreateDto;
import fi.vm.sade.kayttooikeus.dto.PalvelukayttajaCriteriaDto;
import fi.vm.sade.kayttooikeus.dto.PalvelukayttajaReadDto;
import fi.vm.sade.kayttooikeus.enumeration.OrderByHenkilohaku;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.HenkiloHibernateRepository;
import fi.vm.sade.kayttooikeus.repositories.KayttajatiedotRepository;
import fi.vm.sade.kayttooikeus.repositories.criteria.HenkiloCriteria;
import fi.vm.sade.kayttooikeus.service.KayttajatiedotService;
import fi.vm.sade.kayttooikeus.service.PalvelukayttajaService;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloCreateDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.RandomStringUtils;
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
    private final KayttajatiedotRepository kayttajatiedotRepository;

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

}
