package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.model.HenkiloTyyppi;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRepository;
import fi.vm.sade.kayttooikeus.repositories.OrganisaatioHenkiloRepository;
import fi.vm.sade.kayttooikeus.service.OrganisaatioHenkiloService;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static fi.vm.sade.kayttooikeus.model.HenkiloTyyppi.PALVELU;
import static fi.vm.sade.kayttooikeus.model.HenkiloTyyppi.VIRKAILIJA;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * User: tommiratamaa
 * Date: 12/10/2016
 * Time: 14.38
 */
@Service
public class OrganisaatioHenkiloServiceImpl extends AbstractService implements OrganisaatioHenkiloService {
    private final String HENKILOHALLINTA_PALVELUNAME = "HENKILONHALLINTA";
    private final String ROOLI_OPH_REKISTERINPITAJA = "OPHREKISTERI";
    private final String ROOLI_CRUD = "CRUD";
    
    @Autowired
    private OrganisaatioHenkiloRepository organisaatioHenkiloRepository;

    @Autowired
    private KayttoOikeusRepository kayttoOikeusRepository;

    @Autowired
    private OrganisaatioClient organisaatioClient;

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
}
