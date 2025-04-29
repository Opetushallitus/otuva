package fi.vm.sade.kayttooikeus.config.scheduling;

import fi.vm.sade.kayttooikeus.model.Kutsu;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
public class DiscardExpiredInvitationsTask extends AbstractExpiringEntitiesTask<Kutsu> {}
