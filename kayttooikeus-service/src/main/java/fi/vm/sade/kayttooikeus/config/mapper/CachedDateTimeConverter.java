package fi.vm.sade.kayttooikeus.config.mapper;

import ma.glasnost.orika.converter.builtin.PassThroughConverter;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
public class CachedDateTimeConverter extends PassThroughConverter {

    public CachedDateTimeConverter() {
        super(ZonedDateTime.class);
    }

}
