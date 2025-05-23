package fi.vm.sade.kayttooikeus.service.it;


import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.dto.VirkailijaCriteriaDto;
import fi.vm.sade.kayttooikeus.repositories.criteria.OrganisaatioHenkiloCriteria;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.stream.Stream;

import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;

@ExtendWith(SpringExtension.class)
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
    public void kayttajaCriteriaDtoToOrganisaatioHenkiloCriteriaWithoutKayttooikeudet() {
        VirkailijaCriteriaDto kayttajaCriteriaDto = new VirkailijaCriteriaDto();
        kayttajaCriteriaDto.setOrganisaatioOids(Stream.of("oid1", "oid2").collect(toSet()));
        kayttajaCriteriaDto.setKayttooikeudet(null);

        OrganisaatioHenkiloCriteria organisaatioHenkiloCriteria = mapper.map(kayttajaCriteriaDto, OrganisaatioHenkiloCriteria.class);

        assertThat(organisaatioHenkiloCriteria)
                .returns(Stream.of("oid1", "oid2").collect(toSet()), from(OrganisaatioHenkiloCriteria::getOrganisaatioOids))
                .returns(null, from(OrganisaatioHenkiloCriteria::getKayttooikeudet));
    }

    @Test
    public void kayttajaCriteriaDtoToOrganisaatioHenkiloCriteriaWithKayttooikeudet() {
        VirkailijaCriteriaDto kayttajaCriteriaDto = new VirkailijaCriteriaDto();
        kayttajaCriteriaDto.setOrganisaatioOids(null);
        kayttajaCriteriaDto.setKayttooikeudet(singletonMap("KAYTTOOIKEUS", Stream.of("CRUD", "READ").collect(toCollection(LinkedHashSet::new))));

        OrganisaatioHenkiloCriteria organisaatioHenkiloCriteria = mapper.map(kayttajaCriteriaDto, OrganisaatioHenkiloCriteria.class);

        assertThat(organisaatioHenkiloCriteria)
                .returns(null, from(OrganisaatioHenkiloCriteria::getOrganisaatioOids))
                .returns(Stream.of("KAYTTOOIKEUS_CRUD", "KAYTTOOIKEUS_READ").collect(toList()), from(OrganisaatioHenkiloCriteria::getKayttooikeudet));
    }

}
