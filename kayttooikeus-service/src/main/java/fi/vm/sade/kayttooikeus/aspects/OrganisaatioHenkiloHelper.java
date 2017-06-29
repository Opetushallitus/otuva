package fi.vm.sade.kayttooikeus.aspects;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.auditlog.Audit;
import fi.vm.sade.auditlog.kayttooikeus.KayttoOikeusLogMessage;
import fi.vm.sade.auditlog.kayttooikeus.KayttoOikeusOperation;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloCreateDto;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloUpdateDto;
import fi.vm.sade.kayttooikeus.service.OmatTiedotService;
import org.springframework.stereotype.Component;

import java.util.List;
import static java.util.stream.Collectors.toList;

@Component
public class OrganisaatioHenkiloHelper extends AuditlogAspectHelper {

    public OrganisaatioHenkiloHelper(OmatTiedotService omatTiedotService, Audit audit, ObjectMapper mapper) {
        super(omatTiedotService, audit, mapper);
    }

    void logCreateOrUpdateOrganisaatioHenkilo(String henkiloOid, List<OrganisaatioHenkiloUpdateDto> organisaatioHenkiloDtoList,
                                              Object result) {
        List<String> organisaatioOids = organisaatioHenkiloDtoList.stream()
                .map(OrganisaatioHenkiloUpdateDto::getOrganisaatioOid)
                .collect(toList());
        KayttoOikeusLogMessage.LogMessageBuilder logMessage = KayttoOikeusLogMessage.builder()
                .kohdeTunniste(henkiloOid)
                .lisatieto(String.format("Päivitetty henkilölle organisaatiot %s.", organisaatioOids))
                .setOperaatio(KayttoOikeusOperation.CREATE_OR_UPDATE_ORGANISAATIO_HENKILO);
        finishLogging(logMessage);
    }

    void logFindOrCreateOrganisaatioHenkilot(String henkiloOid, List<OrganisaatioHenkiloCreateDto> organisaatioHenkilot, Object result) {
        List<String> organisaatioOids = organisaatioHenkilot.stream()
                .map(OrganisaatioHenkiloCreateDto::getOrganisaatioOid)
                .collect(toList());
        KayttoOikeusLogMessage.LogMessageBuilder logMessage = KayttoOikeusLogMessage.builder()
                .kohdeTunniste(henkiloOid)
                .lisatieto(String.format("Lisätty henkilölle organisaatiot %s.", organisaatioOids))
                .setOperaatio(KayttoOikeusOperation.FIND_OR_CREATE_ORGANISAATIO_HENKILOT);
        finishLogging(logMessage);
    }

    void logPassivoiOrganisaatioHenkilo(String oidHenkilo, String henkiloOrganisationOid, Object result) {
        KayttoOikeusLogMessage.LogMessageBuilder logMessage = KayttoOikeusLogMessage.builder()
                .kohdeTunniste(oidHenkilo)
                .lisatieto(String.format("Henkilön organisaatio %s passivoitu.", henkiloOrganisationOid))
                .setOperaatio(KayttoOikeusOperation.PASSIVOI_ORGANISAATIO_HENKILO);
        finishLogging(logMessage);
    }

}
