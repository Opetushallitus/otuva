package fi.vm.sade.kayttooikeus.aspects;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.auditlog.Audit;
import fi.vm.sade.auditlog.kayttooikeus.KayttoOikeusLogMessage;
import fi.vm.sade.auditlog.kayttooikeus.KayttoOikeusOperation;
import fi.vm.sade.kayttooikeus.dto.GrantKayttooikeusryhmaDto;
import fi.vm.sade.kayttooikeus.dto.KayttooikeusAnomusDto;
import fi.vm.sade.kayttooikeus.dto.UpdateHaettuKayttooikeusryhmaDto;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KayttoOikeusAnomusHelper extends AbstractAuditlogAspectHelper {

    public KayttoOikeusAnomusHelper(Audit audit, ObjectMapper mapper) {
        super(audit, mapper);
    }

    void logApproveOrRejectKayttooikeusAnomus(UpdateHaettuKayttooikeusryhmaDto kayttooikeusryhma, Object result) {
        KayttoOikeusLogMessage.LogMessageBuilder logMessage = KayttoOikeusLogMessage.builder()
                .kohdeTunniste(String.valueOf(kayttooikeusryhma.getId()))
                .newValue(kayttooikeusryhma.getKayttoOikeudenTila())
                .lisatieto("Haettu käyttöoikeusryhmä käsitelty.")
                .setOperaatio(KayttoOikeusOperation.APPROVE_OR_REJECT_KAYTTOOIKEUSANOMUS);
        finishLogging(logMessage);
    }

    void logSendKayttooikeusAnomusNotification(LocalDate anottuPvm, Object result) {
        KayttoOikeusLogMessage.LogMessageBuilder logMessage = KayttoOikeusLogMessage.builder()
                .lisatieto("Avoimista käyttöoikeusanomuksista lähetetty muistutukset hyväksyjille.")
                .setOperaatio(KayttoOikeusOperation.SEND_KAYTTOOIKEUSANOMUS_NOTIFICATION);
        finishLogging(logMessage);
    }

    void logCancelKayttooikeusAnomus(Long kayttooikeusRyhmaId, Object result) {
        KayttoOikeusLogMessage.LogMessageBuilder logMessage = KayttoOikeusLogMessage.builder()
                .kohdeTunniste(String.valueOf(kayttooikeusRyhmaId))
                .lisatieto("Henkilön käyttöoikeushakemus peruttu.")
                .setOperaatio(KayttoOikeusOperation.REMOVE_USER_FROM_KAYTTOOIKEUSANOMUS);
        finishLogging(logMessage);
    }

    void logCreateKayttooikeusAnomus(String anojaOid, KayttooikeusAnomusDto kayttooikeusAnomusDto, Object result) {
        KayttoOikeusLogMessage.LogMessageBuilder logMessage = KayttoOikeusLogMessage.builder()
                .kohdeTunniste(anojaOid)
                .newValue(kayttooikeusAnomusDto.getOrganisaatioOrRyhmaOid())
                .lisatieto("Luotu uusi käyttöoikeusanomus.")
                .setOperaatio(KayttoOikeusOperation.CREATE_KAYTTOOIKEUSANOMUS);
        finishLogging(logMessage);
    }

    void logGrantKayttooikeusryhma(String anojaOid, String organisaatioOid, List<GrantKayttooikeusryhmaDto> updateHaettuKayttooikeusryhmaDtoList, Object result) {
        KayttoOikeusLogMessage.LogMessageBuilder logMessage = KayttoOikeusLogMessage.builder()
                .kohdeTunniste(anojaOid)
                .newValue(organisaatioOid)
                .lisatieto("Myönnetty halutut käyttöoikeusryhmät henkilölle.")
                .setOperaatio(KayttoOikeusOperation.ADD_KAYTTOOIKEUSRYHMA_TO_HENKILO);
        finishLogging(logMessage);
    }

}
