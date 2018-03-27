package fi.vm.sade.kayttooikeus.service.it;


import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.dto.PalvelukayttajaReadDto;
import fi.vm.sade.kayttooikeus.repositories.dto.HenkilohakuResultDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;

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

    @Test
    public void palvelukayttajaHakuMapping() {
        HenkilohakuResultDto henkilohakuResult = new HenkilohakuResultDto("oid1", "teemu", "testaaja", "kayttajatunnus1");

        PalvelukayttajaReadDto palvelukayttaja = mapper.map(henkilohakuResult, PalvelukayttajaReadDto.class);

        assertThat(palvelukayttaja)
                .returns("oid1", from(PalvelukayttajaReadDto::getOid))
                .returns("testaaja", from(PalvelukayttajaReadDto::getNimi));
    }

}
