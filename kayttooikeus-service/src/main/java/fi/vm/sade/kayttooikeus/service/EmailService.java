package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.UpdateHaettuKayttooikeusryhmaDto;
import fi.vm.sade.kayttooikeus.model.Anomus;
import fi.vm.sade.kayttooikeus.model.Kutsu;
import fi.vm.sade.kayttooikeus.repositories.dto.ExpiringKayttoOikeusDto;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloDto;
import java.time.Duration;

import java.util.List;
import java.util.Set;

public interface EmailService {

    void sendEmailAnomusKasitelty(Anomus anomus, UpdateHaettuKayttooikeusryhmaDto updateHaettuKayttooikeusryhmaDto);

    void sendExpirationReminder(String henkiloOid, List<ExpiringKayttoOikeusDto> tapahtumas);

    void sendNewRequisitionNotificationEmails(Set<String> henkiloOids);

    void sendInvitationEmail(Kutsu kutsu);

    void sendEmailReset(HenkiloDto henkilo, String sahkoposti, String poletti, Duration voimassa);

}
