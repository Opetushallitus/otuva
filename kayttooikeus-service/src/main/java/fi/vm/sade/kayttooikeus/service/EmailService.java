package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.repositories.dto.ExpiringKayttoOikeusDto;

import java.util.List;
import java.util.Set;

public interface EmailService {

    void sendExpirationReminder(String henkiloOid, List<ExpiringKayttoOikeusDto> tapahtumas);

    void sendNewRequisitionNotificationEmails(Set<Henkilo> henkilot);

}
