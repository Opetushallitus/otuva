package fi.vm.sade.kayttooikeus.service.it;


import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.dto.Localizable;
import fi.vm.sade.kayttooikeus.dto.LocalizableDto;
import fi.vm.sade.kayttooikeus.dto.TextGroupDto;
import fi.vm.sade.kayttooikeus.model.TextGroup;
import fi.vm.sade.kayttooikeus.service.LocalizationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.stream.Stream;

import static fi.vm.sade.kayttooikeus.repositories.populate.TextGroupPopulator.text;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class OrikaMappingTest extends AbstractServiceIntegrationTest {

    @Autowired
    private OrikaBeanMapper mapper;

    @Test
    public void javaDateMapping() {
        Date now = new Date();
        LocalDate nowLocalDate = LocalDate.now();
        LocalDateTime nowLocalDateTime = LocalDateTime.now();

        ZonedDateTime nowZonedDateTime = ZonedDateTime.now();

        this.mapper.map(now, LocalDate.class);
        this.mapper.map(nowLocalDate, Date.class);

        this.mapper.map(now, LocalDateTime.class);
        this.mapper.map(nowLocalDateTime, Date.class);

        this.mapper.map(now, ZonedDateTime.class);
        this.mapper.map(nowZonedDateTime, Date.class);
    }
}
