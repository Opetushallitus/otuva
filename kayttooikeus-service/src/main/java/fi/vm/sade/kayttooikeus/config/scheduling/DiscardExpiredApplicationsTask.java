package fi.vm.sade.kayttooikeus.config.scheduling;

import fi.vm.sade.kayttooikeus.model.Anomus;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiscardExpiredApplicationsTask extends AbstractExpiringEntitiesTask<Anomus> {}


