package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.dto.KayttajaTyyppi;
import fi.vm.sade.kayttooikeus.dto.PalvelukayttajaCreateDto;
import fi.vm.sade.kayttooikeus.dto.PalvelukayttajaCriteriaDto;
import fi.vm.sade.kayttooikeus.dto.PalvelukayttajaReadDto;
import fi.vm.sade.kayttooikeus.enumeration.OrderByHenkilohaku;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.HenkiloHibernateRepository;
import fi.vm.sade.kayttooikeus.repositories.criteria.HenkiloCriteria;
import fi.vm.sade.kayttooikeus.service.PalvelukayttajaService;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloCreateDto;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.RequiredArgsConstructor;
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

    @Override
    public PalvelukayttajaReadDto create(PalvelukayttajaCreateDto createDto) {
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

        return new PalvelukayttajaReadDto(oid, createDto.getNimi(), null);
    }

}
