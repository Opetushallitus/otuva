package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.service.TimeService;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

@Service
public class TimeServiceImpl implements TimeService {

    @Override
    public long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public DateTime getDateTimeNow() {
        return DateTime.now();
    }

}
