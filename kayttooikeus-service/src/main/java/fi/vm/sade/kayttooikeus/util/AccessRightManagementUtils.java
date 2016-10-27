package fi.vm.sade.kayttooikeus.util;

import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.*;
import fi.vm.sade.kayttooikeus.service.dto.LocaleTextDto;
import fi.vm.sade.kayttooikeus.service.dto.PalveluRoooliDto;
import fi.vm.sade.kayttooikeus.service.exception.InvalidKayttoOikeusException;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;


@Service
public class AccessRightManagementUtils{

    private OrganisaatioViiteRepository organisaatioViiteRepository;

    private OrganisaatioClient organisaatioClient;

    private MyonnettyKayttoOikeusRyhmaTapahtumaRepository myonnettyKayttoOikeusRyhmaTapahtumaRepository;

    private KayttoOikeusRyhmaMyontoViiteRepository kayttoOikeusRyhmaMyontoViiteRepository;

    private KayttoOikeusRepository kayttoOikeusRepository;

    private PalveluRepository palveluRepository;

    private static final String OPH_ORGANIZATION_OID = "1.2.246.562.10.00000000001";

    private static final String GROUP_ORGANIZATION_ID = "1.2.246.562.28";

    @Autowired
    public AccessRightManagementUtils(OrganisaatioViiteRepository organisaatioViiteRepository,
                                      OrganisaatioClient organisaatioClient,
                                      MyonnettyKayttoOikeusRyhmaTapahtumaRepository myonnettyKayttoOikeusRyhmaTapahtumaRepository,
                                      KayttoOikeusRyhmaMyontoViiteRepository kayttoOikeusRyhmaMyontoViiteRepository,
                                      KayttoOikeusRepository kayttoOikeusRepository,
                                      PalveluRepository palveluRepository){
        this.organisaatioViiteRepository = organisaatioViiteRepository;
        this.organisaatioClient = organisaatioClient;
        this.myonnettyKayttoOikeusRyhmaTapahtumaRepository = myonnettyKayttoOikeusRyhmaTapahtumaRepository;
        this.kayttoOikeusRyhmaMyontoViiteRepository = kayttoOikeusRyhmaMyontoViiteRepository;
        this.kayttoOikeusRepository = kayttoOikeusRepository;
        this.palveluRepository = palveluRepository;
    }

    /*
        Remove ryhma limitations (done because UI can't properly handle them)
     */
    public void parseRyhmaLimitationsBasedOnOrgOid(String organisaatioOid, List<KayttoOikeusRyhma> allRyhmas) {
        List<KayttoOikeusRyhma> toBeRemoved = new ArrayList<>();
        for (KayttoOikeusRyhma orgCheck : allRyhmas) {
            List<OrganisaatioViite> viites = organisaatioViiteRepository.findByKayttoOikeusRyhmaId(orgCheck.getId());

            /* If group doesn't have any institution types specified,
             * then it's restricted only to OPH organization
             */
            if (CollectionUtils.isEmpty(viites) && !organisaatioOid.equals(OPH_ORGANIZATION_OID)) {
                toBeRemoved.add(orgCheck);
            }
            // OPH organization can always see all groups regardless of restrictions
            else if (!CollectionUtils.isEmpty(viites) && !organisaatioOid.equals(OPH_ORGANIZATION_OID)) {
                boolean found = checkOrganizationLimitations(organisaatioOid, viites);
                if (!found) {
                    toBeRemoved.add(orgCheck);
                }
            }
        }
        // The original set of groups is modified instead of returning a new group set
        if (!toBeRemoved.isEmpty()) {
            allRyhmas.removeAll(toBeRemoved);
        }
    }

    private boolean checkOrganizationLimitations(String organisaatioOid, List<OrganisaatioViite> viites) {
        boolean orgLimitOk = false;
        /* Group organizations have to match only as a general set since
         * they're not separated by type or by individual groups
         */
        if (organisaatioOid.startsWith(GROUP_ORGANIZATION_ID)) {
            orgLimitOk = oidIsFoundInViites(GROUP_ORGANIZATION_ID, viites);
        }
        // Otherwise normal organization limitations must be checked
        else {
            // If restrictions are found, then organization types must be checked
            List<OrganisaatioPerustieto> hakuTulos = organisaatioClient.listActiveOganisaatioPerustiedot(Collections.singletonList(organisaatioOid));

            for (OrganisaatioPerustieto opt : hakuTulos) {
                /* Organization must have child items in it, so that the
                 * institution type can be fetched and verified
                 */
                if (!CollectionUtils.isEmpty(opt.getChildren())) {
                    orgLimitOk = orgTypeMatchesOrOidIsFoundInViites(organisaatioOid, viites, opt);
                }
                /* But if the organization doesn't have child items, then it must be a top
                 * level organization or some other type organization in which case the
                 * target organization OID must match the allowed-to-organization OID
                 */
                else {
                    orgLimitOk = oidIsFoundInViites(organisaatioOid, viites);
                }
                if (orgLimitOk) {
                    break;
                }
            }
        }

        return orgLimitOk;
    }

    private boolean oidIsFoundInViites(String organisaatioOid, List<OrganisaatioViite> viites) {
        return viites.stream().anyMatch(organisaatioViite -> organisaatioViite.getOrganisaatioTyyppi().equals(organisaatioOid));
    }

    private boolean orgTypeMatchesOrOidIsFoundInViites(String organisaatioOid, List<OrganisaatioViite> viites, OrganisaatioPerustieto opt) {
        for (OrganisaatioPerustieto childOpt : opt.getChildren()) {
            // getOppilaitostyyppi() = "oppilaitostyyppi_11#1"
            String laitosTyyppi = StringUtils.isNotBlank(childOpt.getOppilaitostyyppi()) ? childOpt.getOppilaitostyyppi().substring(17, 19) : null;

            // Organization reference might be institution type or a single organization
            List<String> oids = Arrays.asList(laitosTyyppi, organisaatioOid);
            if( viites.stream().anyMatch(organisaatioViite ->  oids.contains(organisaatioViite.getOrganisaatioTyyppi())) ){
                return true;
            }
        }
        return false;
    }

    public List<Long> getGrantableKayttooikeusRyhmas(String myontajaOid) {
        List<Long> masterIds = myonnettyKayttoOikeusRyhmaTapahtumaRepository.findMasterIdsByHenkilo(myontajaOid);
        return kayttoOikeusRyhmaMyontoViiteRepository.getSlaveIdsByMasterIds(masterIds);
    }

    public List<PalveluRoooliDto> createPalveluRooliDTO(List<Palvelu> palvelus) {
        List<PalveluRoooliDto> prDTOt = new ArrayList<PalveluRoooliDto>();

        for (Palvelu p : palvelus) {
            for (KayttoOikeus ko : p.getKayttoOikeus()) {
                PalveluRoooliDto prdto = new PalveluRoooliDto();
                prdto.setPalveluName(p.getName());
                p.getDescription().getTexts().forEach(text -> prdto.getPalveluTexts().add(new LocaleTextDto(text.getText(), text.getLang())));
                prdto.setRooli(ko.getRooli());
                ko.getTextGroup().getTexts().forEach(text -> prdto.getRooliTexts().add(new LocaleTextDto(text.getText(), text.getLang())));
                prDTOt.add(prdto);
            }
        }

        return prDTOt;
    }

    public Set<KayttoOikeus> bindToNonTransientInstances(KayttoOikeusRyhma kayttoOikeusRyhma) {
        HashSet<KayttoOikeus> kayttoOikeusHashSet = new HashSet<KayttoOikeus>();
        for (KayttoOikeus kayttoOikeus : kayttoOikeusRyhma.getKayttoOikeus()) {
            if (kayttoOikeus.getRooli() == null) {
                throw new InvalidKayttoOikeusException("Rooli may not be null");
            }
            KayttoOikeus byRooliAndPalvelu;
            byRooliAndPalvelu = kayttoOikeusRepository.findByRooliAndPalvelu(kayttoOikeus);
            if (byRooliAndPalvelu == null) {
                byRooliAndPalvelu = createKayttoOikeus(kayttoOikeus);
            }

            kayttoOikeusHashSet.add(byRooliAndPalvelu);
        }
        return kayttoOikeusHashSet;
    }

    public KayttoOikeus createKayttoOikeus(KayttoOikeus kayttoOikeus) {
        List<Palvelu> palvelus = palveluRepository.findByName(kayttoOikeus.getPalvelu().getName());
        if (palvelus.isEmpty()) {
            throw new NotFoundException("palvelu not found by name : " + kayttoOikeus.getPalvelu().getName());
        }

        final Palvelu palvelu = palvelus.get(0);
        kayttoOikeus.setPalvelu(palvelu);
        palvelu.getKayttoOikeus().add(kayttoOikeus);
        return kayttoOikeusRepository.insert(kayttoOikeus);
    }
}
