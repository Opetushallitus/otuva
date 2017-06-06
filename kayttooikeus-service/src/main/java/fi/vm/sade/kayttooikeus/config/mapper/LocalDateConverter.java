package fi.vm.sade.kayttooikeus.config.mapper;

import ma.glasnost.orika.converter.builtin.PassThroughConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class LocalDateConverter extends PassThroughConverter {

    public LocalDateConverter() {
        super(LocalDate.class);
    }

}
