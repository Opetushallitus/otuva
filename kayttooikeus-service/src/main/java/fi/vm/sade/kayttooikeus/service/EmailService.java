package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.repositories.dto.ExpiringKayttoOikeusDto;

import java.util.List;

public interface EmailService {
    void sendExpirationReminder(String henkiloOid, List<ExpiringKayttoOikeusDto> tapahtumas);
}
