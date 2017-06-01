package fi.vm.sade.kayttooikeus.service;

import org.joda.time.DateTime;

public interface TimeService {

    long getCurrentTimeMillis();

    DateTime getDateTimeNow();

}
