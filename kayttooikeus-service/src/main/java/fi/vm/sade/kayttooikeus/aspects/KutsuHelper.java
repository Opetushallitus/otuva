package fi.vm.sade.kayttooikeus.aspects;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.auditlog.Audit;
import fi.vm.sade.auditlog.kayttooikeus.KayttoOikeusLogMessage;
import fi.vm.sade.auditlog.kayttooikeus.KayttoOikeusOperation;
import fi.vm.sade.kayttooikeus.dto.KutsuCreateDto;
import fi.vm.sade.kayttooikeus.model.Kutsu;
import org.springframework.stereotype.Component;

@Component
public class KutsuHelper extends AbstractAuditlogAspectHelper {

    public KutsuHelper(Audit audit, ObjectMapper mapper) {
        super(audit, mapper);
    }

    void logCreateKutsu(KutsuCreateDto dto, Object result) {
        KayttoOikeusLogMessage.LogMessageBuilder logMessage = KayttoOikeusLogMessage.builder()
                .kohdeTunniste(dto.getSahkoposti())
                .lisatieto("Luotu kutsu virkailijalle.")
                .setOperaatio(KayttoOikeusOperation.CREATE_KUTSU);
        finishLogging(logMessage);
    }

    void logDeleteKutsu(Long id, Object result) {
        String email = "";
        if (result instanceof Kutsu) {
            email = ((Kutsu)result).getSahkoposti();
        }

        KayttoOikeusLogMessage.LogMessageBuilder logMessage = KayttoOikeusLogMessage.builder()
                .kohdeTunniste(email)
                .lisatieto("Poistettu virkailijan kutsu.")
                .setOperaatio(KayttoOikeusOperation.DELETE_KUTSU);
        finishLogging(logMessage);
    }

}
