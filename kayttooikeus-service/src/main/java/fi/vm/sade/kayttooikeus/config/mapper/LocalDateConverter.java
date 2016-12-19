package fi.vm.sade.kayttooikeus.config.mapper;

import ma.glasnost.orika.converter.builtin.PassThroughConverter;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class LocalDateConverter extends PassThroughConverter {

    public LocalDateConverter() {
        super(LocalDate.class);
    }

}
