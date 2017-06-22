package fi.vm.sade.kayttooikeus.aspects;

import fi.vm.sade.auditlog.kayttooikeus.KayttoOikeusLogMessage;
import fi.vm.sade.auditlog.kayttooikeus.KayttoOikeusOperation;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloCreateDto;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloUpdateDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrganisaatioHenkiloHelper extends AuditlogAspectHelper {

    //TODO: is there any point to logging list with multiple entries? maybe log individually?
    void logCreateOrUpdateOrganisaatioHenkilo(String henkiloOid, List<OrganisaatioHenkiloUpdateDto> organisaatioHenkiloDtoList,
                                              Object result) {
        KayttoOikeusLogMessage.LogMessageBuilder logMessage = KayttoOikeusLogMessage.builder()
                .kohdeTunniste(henkiloOid)
                .lisatieto("Henkilön hakatunnisteet muutettu.")
                .setOperaatio(KayttoOikeusOperation.CREATE_OR_UPDATE_ORGANISAATIO_HENKILO);
        finishLogging(logMessage);
    }

    void logFindOrCreateOrganisaatioHenkilot(String henkiloOid, List<OrganisaatioHenkiloCreateDto> organisaatioHenkilot, Object result) {
        KayttoOikeusLogMessage.LogMessageBuilder logMessage = KayttoOikeusLogMessage.builder()
                .kohdeTunniste(henkiloOid)
                .lisatieto("Henkilön hakatunnisteet muutettu.")
                .setOperaatio(KayttoOikeusOperation.FIND_OR_CREATE_ORGANISAATIO_HENKILOT);
        finishLogging(logMessage);
    }

    void logPassivoiOrganisaatioHenkilo(String oidHenkilo, String henkiloOrganisationOid, Object result) {
        KayttoOikeusLogMessage.LogMessageBuilder logMessage = KayttoOikeusLogMessage.builder()
                .kohdeTunniste(oidHenkilo)
                .lisatieto("Henkilön hakatunnisteet muutettu.")
                .setOperaatio(KayttoOikeusOperation.PASSIVOI_ORGANISAATIO_HENKILO);
        finishLogging(logMessage);
    }

}
