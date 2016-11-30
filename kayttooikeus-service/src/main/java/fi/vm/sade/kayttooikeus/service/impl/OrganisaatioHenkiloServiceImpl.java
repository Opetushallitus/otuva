package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.dto.HenkiloTyyppi;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloDto;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloWithOrganisaatioDto;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloWithOrganisaatioDto.OrganisaatioDto;
import fi.vm.sade.kayttooikeus.dto.TextGroupMapDto;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRepository;
import fi.vm.sade.kayttooikeus.repositories.OrganisaatioHenkiloRepository;
import fi.vm.sade.kayttooikeus.service.OrganisaatioHenkiloService;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient.Mode;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioPerustieto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

import static fi.vm.sade.kayttooikeus.dto.HenkiloTyyppi.PALVELU;
import static fi.vm.sade.kayttooikeus.dto.HenkiloTyyppi.VIRKAILIJA;
import static fi.vm.sade.kayttooikeus.dto.Localizable.comparingPrimarlyBy;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Service
public class OrganisaatioHenkiloServiceImpl extends AbstractService implements OrganisaatioHenkiloService {
    private final String HENKILOHALLINTA_PALVELUNAME = "HENKILONHALLINTA";
    private final String ROOLI_OPH_REKISTERINPITAJA = "OPHREKISTERI";
    private final String ROOLI_CRUD = "CRUD";
    private final String FALLBACK_LANGUAGE = "fi";

    private OrganisaatioHenkiloRepository organisaatioHenkiloRepository;
    private KayttoOikeusRepository kayttoOikeusRepository;
    private OrganisaatioClient organisaatioClient;

    @Autowired
    public OrganisaatioHenkiloServiceImpl(OrganisaatioHenkiloRepository organisaatioHenkiloRepository,
                                          KayttoOikeusRepository kayttoOikeusRepository,
                                          OrganisaatioClient organisaatioClient) {
        this.organisaatioHenkiloRepository = organisaatioHenkiloRepository;
        this.kayttoOikeusRepository = kayttoOikeusRepository;
        this.organisaatioClient = organisaatioClient;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganisaatioHenkiloWithOrganisaatioDto> listOrganisaatioHenkilos(String henkiloOid, String compareByLang) {
        Mode organisaatioClientMode = Mode.requireCache();
        return organisaatioHenkiloRepository.findActiveOrganisaatioHenkiloListDtos(henkiloOid)
                .stream().peek(organisaatioHenkilo ->
                    organisaatioHenkilo.setOrganisaatio(
                        mapOrganisaatioDtoRecursive(
                            organisaatioClient.getOrganisaatioPerustiedot(organisaatioHenkilo.getOrganisaatio().getOid(), organisaatioClientMode),
                            compareByLang))
                ).sorted(Comparator.comparing(dto -> dto.getOrganisaatio().getNimi(),
                        comparingPrimarlyBy(ofNullable(compareByLang).orElse(FALLBACK_LANGUAGE)))).collect(toList());
    }

    protected OrganisaatioDto mapOrganisaatioDtoRecursive(OrganisaatioPerustieto perustiedot, String compareByLang) {
        OrganisaatioDto dto = new OrganisaatioDto();
        dto.setOid(perustiedot.getOid());
        dto.setNimi(new TextGroupMapDto(null, perustiedot.getNimi()));
        dto.setParentOidPath(perustiedot.getParentOidPath());
        dto.setTyypit(perustiedot.getTyypit());
        dto.setChildren(perustiedot.getChildren().stream().map(child -> mapOrganisaatioDtoRecursive(child, compareByLang))
            .sorted(Comparator.comparing(OrganisaatioDto::getNimi, comparingPrimarlyBy(ofNullable(compareByLang).orElse(FALLBACK_LANGUAGE))))
            .collect(toList()));
        return dto;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<OrganisaatioPerustieto> listOrganisaatioPerustiedotForCurrentUser() {
        return organisaatioClient.listActiveOganisaatioPerustiedotByOidRestrictionList(
                organisaatioHenkiloRepository.findDistinctOrganisaatiosForHenkiloOid(getCurrentUserOid()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HenkiloTyyppi> listPossibleHenkiloTypesAccessibleForCurrentUser() {
        if (kayttoOikeusRepository.isHenkiloMyonnettyKayttoOikeusToPalveluInRole(getCurrentUserOid(),
                HENKILOHALLINTA_PALVELUNAME, ROOLI_OPH_REKISTERINPITAJA)) {
            return asList(VIRKAILIJA, PALVELU);
        }
        if (kayttoOikeusRepository.isHenkiloMyonnettyKayttoOikeusToPalveluInRole(getCurrentUserOid(),
                HENKILOHALLINTA_PALVELUNAME, ROOLI_CRUD)) {
            return singletonList(VIRKAILIJA);
        }
        return emptyList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrganisaatioHenkiloDto findOrganisaatioHenkiloByHenkiloAndOrganisaatio(String henkiloOid, String organisaatioOid) {
        return organisaatioHenkiloRepository.findByHenkiloOidAndOrganisaatioOid(henkiloOid, organisaatioOid)
                .orElseThrow(() -> new NotFoundException("Could not find organisaatiohenkilo"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganisaatioHenkiloDto> findOrganisaatioByHenkilo(String henkiloOid) {
        return organisaatioHenkiloRepository.findOrganisaatioHenkilosForHenkilo(henkiloOid);
    }
}
