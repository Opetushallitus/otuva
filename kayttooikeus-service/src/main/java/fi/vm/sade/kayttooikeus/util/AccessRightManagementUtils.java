package fi.vm.sade.kayttooikeus.util;

import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.model.MyonnettyKayttoOikeusRyhmaTapahtuma;
import fi.vm.sade.kayttooikeus.model.OrganisaatioViite;
import fi.vm.sade.kayttooikeus.model.Text;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRyhmaMyontoViiteRepository;
import fi.vm.sade.kayttooikeus.repositories.MyonnettyKayttoOikeusRyhmaTapahtumaRepository;
import fi.vm.sade.kayttooikeus.repositories.OrganisaatioViiteRepository;
import fi.vm.sade.kayttooikeus.service.dto.MyonnettyKayttoOikeusDTO;
import fi.vm.sade.kayttooikeus.service.dto.TextDto;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Service
public class AccessRightManagementUtils{

    private OrganisaatioViiteRepository organisaatioViiteRepository;

    private OrganisaatioClient organisaatioClient;

    private MyonnettyKayttoOikeusRyhmaTapahtumaRepository myonnettyKayttoOikeusRyhmaTapahtumaRepository;

    private KayttoOikeusRyhmaMyontoViiteRepository kayttoOikeusRyhmaMyontoViiteRepository;

    private static final String OPH_ORGANIZATION_OID = "1.2.246.562.10.00000000001";

    private static final String GROUP_ORGANIZATION_ID = "1.2.246.562.28";

    @Autowired
    public AccessRightManagementUtils(OrganisaatioViiteRepository organisaatioViiteRepository,
                                      OrganisaatioClient organisaatioClient,
                                      MyonnettyKayttoOikeusRyhmaTapahtumaRepository myonnettyKayttoOikeusRyhmaTapahtumaRepository,
                                      KayttoOikeusRyhmaMyontoViiteRepository kayttoOikeusRyhmaMyontoViiteRepository){
        this.organisaatioViiteRepository = organisaatioViiteRepository;
        this.organisaatioClient = organisaatioClient;
        this.myonnettyKayttoOikeusRyhmaTapahtumaRepository = myonnettyKayttoOikeusRyhmaTapahtumaRepository;
        this.kayttoOikeusRyhmaMyontoViiteRepository = kayttoOikeusRyhmaMyontoViiteRepository;
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
        List<MyonnettyKayttoOikeusRyhmaTapahtuma> kasittelijaMKORTs = myonnettyKayttoOikeusRyhmaTapahtumaRepository.findValidByHenkiloOid(myontajaOid);
        List<Long> masterIds = new ArrayList<>();

        for (MyonnettyKayttoOikeusRyhmaTapahtuma mkort : kasittelijaMKORTs) {
            masterIds.add(mkort.getKayttoOikeusRyhma().getId());
        }

        return kayttoOikeusRyhmaMyontoViiteRepository.getSlaveIdsByMasterIds(masterIds);
    }

    public List<MyonnettyKayttoOikeusDTO> createMyonnettyKayttoOikeusDTO(List<KayttoOikeusRyhma> allRyhmas,
                                                                         List<MyonnettyKayttoOikeusRyhmaTapahtuma> henkilosKORs) {
            List<MyonnettyKayttoOikeusDTO> results = new ArrayList<MyonnettyKayttoOikeusDTO>();

            for (KayttoOikeusRyhma ko : allRyhmas) {
                MyonnettyKayttoOikeusDTO mkDTO = new MyonnettyKayttoOikeusDTO();
                mkDTO.setRyhmaId(ko.getId());
                if (ko.getDescription() != null) {
                    copyTexts(mkDTO, ko);
                }
                mkDTO.setSelected(false);
                // Henkilo's all granted access rights must be checked to see
                // if they contain same IDs as these need to be shown in the UI
                for (MyonnettyKayttoOikeusRyhmaTapahtuma mkort : henkilosKORs) {
                    if (mkort.getKayttoOikeusRyhma().getId().equals(ko.getId())) {
                        mkDTO.setMyonnettyTapahtumaId(mkort.getId());
                        mkDTO.setAlkuPvm(mkort.getVoimassaAlkuPvm());
                        mkDTO.setVoimassaPvm(mkort.getVoimassaLoppuPvm());
                        mkDTO.setSelected(true);
                        break;
                    }
                }
                results.add(mkDTO);
            }

            return results;
    }

    private void copyTexts(MyonnettyKayttoOikeusDTO mkDTO, KayttoOikeusRyhma kor) {
        for (Text t : kor.getDescription().getTexts()) {
            TextDto ryhmaText = new TextDto();
            ryhmaText.setLang(t.getLang());
            ryhmaText.setText(t.getText());
            mkDTO.getRyhmaNames().add(ryhmaText);
        }
    }

}
