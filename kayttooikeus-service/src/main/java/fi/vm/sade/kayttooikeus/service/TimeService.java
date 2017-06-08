package fi.vm.sade.kayttooikeus.service;


import java.time.LocalDateTime;

public interface TimeService {

    long getCurrentTimeMillis();

    LocalDateTime getDateTimeNow();

}
