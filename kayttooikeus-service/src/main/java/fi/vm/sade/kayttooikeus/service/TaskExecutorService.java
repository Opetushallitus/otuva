package fi.vm.sade.kayttooikeus.service;

import org.joda.time.Period;

public interface TaskExecutorService {
    int sendExpirationReminders(Period...expireThresholds);
}
