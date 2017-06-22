package fi.vm.sade.kayttooikeus.aspects;

import fi.vm.sade.auditlog.kayttooikeus.KayttoOikeusLogMessage;
import fi.vm.sade.auditlog.kayttooikeus.KayttoOikeusOperation;
import fi.vm.sade.kayttooikeus.repositories.dto.ExpiringKayttoOikeusDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KayttoOikeusHelper extends AuditlogAspectHelper {

    /* Käyttöoikeus */
    void logSendKayttooikeusReminder(String henkiloOid, List<ExpiringKayttoOikeusDto> tapahtumas, Object result) {
        KayttoOikeusLogMessage.LogMessageBuilder logMessage = KayttoOikeusLogMessage.builder()
                .kohdeTunniste(henkiloOid)
                .lisatieto("Lähetetty muistutus käyttöoikeuksien vanhenemisesta.")
                .setOperaatio(KayttoOikeusOperation.SEND_KAYTTOOIKEUS_EXPIRATION_REMINDER);
        finishLogging(logMessage);
    }

    /* Myönnetty Kayttooikeus */
    void logRemoveExpiredKayttooikeudet(String kasittelijaOid, Object result) {
        KayttoOikeusLogMessage.LogMessageBuilder logMessage = KayttoOikeusLogMessage.builder()
                .kohdeTunniste(kasittelijaOid)
                .lisatieto("Poistettu vanhentuneet käyttöoikeudet.")
                .setOperaatio(KayttoOikeusOperation.REMOVE_EXPIRED_KAYTTOOIKEUDET);
        finishLogging(logMessage);
    }

}
