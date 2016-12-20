package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.dto.HenkiloTyyppi;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloDto;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRepository;
import fi.vm.sade.kayttooikeus.repositories.OrganisaatioHenkiloRepository;
import fi.vm.sade.kayttooikeus.service.OrganisaatioHenkiloService;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static fi.vm.sade.kayttooikeus.dto.HenkiloTyyppi.PALVELU;
import static fi.vm.sade.kayttooikeus.dto.HenkiloTyyppi.VIRKAILIJA;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloCreateDto;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.OrganisaatioHenkilo;
import fi.vm.sade.kayttooikeus.repositories.HenkiloRepository;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@Service
public class OrganisaatioHenkiloServiceImpl extends AbstractService implements OrganisaatioHenkiloService {
    private final String HENKILOHALLINTA_PALVELUNAME = "HENKILONHALLINTA";
    private final String ROOLI_OPH_REKISTERINPITAJA = "OPHREKISTERI";
    private final String ROOLI_CRUD = "CRUD";
    
    private OrganisaatioHenkiloRepository organisaatioHenkiloRepository;
    private KayttoOikeusRepository kayttoOikeusRepository;
    private HenkiloRepository henkiloRepository;
    private OrikaBeanMapper mapper;
    private OrganisaatioClient organisaatioClient;

    @Autowired
    public OrganisaatioHenkiloServiceImpl(OrganisaatioHenkiloRepository organisaatioHenkiloRepository,
                                          KayttoOikeusRepository kayttoOikeusRepository,
                                          HenkiloRepository henkiloRepository,
                                          OrikaBeanMapper mapper,
                                          OrganisaatioClient organisaatioClient) {
        this.organisaatioHenkiloRepository = organisaatioHenkiloRepository;
        this.kayttoOikeusRepository = kayttoOikeusRepository;
        this.henkiloRepository = henkiloRepository;
        this.mapper = mapper;
        this.organisaatioClient = organisaatioClient;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganisaatioPerustieto> listOrganisaatioPerustiedotForCurrentUser() {
        return organisaatioClient.listOganisaatioPerustiedot(
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

    @Override
    @Transactional(readOnly = false)
    public List<OrganisaatioHenkiloDto> addOrganisaatioHenkilot(String henkiloOid, List<OrganisaatioHenkiloCreateDto> organisaatioHenkilot) {
        Henkilo henkilo = henkiloRepository.findByOidHenkilo(henkiloOid)
                .orElseThrow(() -> new NotFoundException("Henkilöä ei löytynyt OID:lla " + henkiloOid));

        organisaatioHenkilot.stream()
                .filter((OrganisaatioHenkiloCreateDto t) -> {
                    return henkilo.getOrganisaatioHenkilos().stream()
                            .noneMatch((OrganisaatioHenkilo u) -> u.getOrganisaatioOid().equals(t.getOrganisaatioOid()));
                })
                .forEach((OrganisaatioHenkiloCreateDto t) -> {
                    OrganisaatioHenkilo organisaatioHenkilo = mapper.map(t, OrganisaatioHenkilo.class);
                    organisaatioHenkilo.setHenkilo(henkilo);
                    henkilo.getOrganisaatioHenkilos().add(organisaatioHenkiloRepository.persist(organisaatioHenkilo));
                });

        return mapper.mapAsList(henkilo.getOrganisaatioHenkilos(), OrganisaatioHenkiloDto.class);
    }
}
