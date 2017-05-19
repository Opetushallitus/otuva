package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.model.Anomus;
import fi.vm.sade.kayttooikeus.model.Kutsu;
import fi.vm.sade.kayttooikeus.repositories.dto.ExpiringKayttoOikeusDto;

import java.util.List;

public interface EmailService {
    void sendEmailAnomusAccepted(Anomus anomus);

    void sendExpirationReminder(String henkiloOid, List<ExpiringKayttoOikeusDto> tapahtumas);

    void sendInvitationEmail(Kutsu kutsu);
}
