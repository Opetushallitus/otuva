package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.service.TimeService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TimeServiceImpl implements TimeService {

    @Override
    public long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public LocalDateTime getDateTimeNow() {
        return LocalDateTime.now();
    }

}
