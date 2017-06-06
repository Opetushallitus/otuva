package fi.vm.sade.kayttooikeus.config.mapper;

import ma.glasnost.orika.converter.builtin.PassThroughConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DateTimeConverter extends PassThroughConverter {

    public DateTimeConverter() {
        super(LocalDateTime.class);
    }

}
