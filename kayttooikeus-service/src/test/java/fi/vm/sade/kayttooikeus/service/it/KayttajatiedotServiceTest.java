package fi.vm.sade.kayttooikeus.service.it;

import fi.vm.sade.kayttooikeus.dto.KayttajatiedotCreateDto;
import fi.vm.sade.kayttooikeus.dto.KayttajatiedotReadDto;
import static fi.vm.sade.kayttooikeus.repositories.populate.HenkiloPopulator.henkilo;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttajatiedotPopulator.kayttajatiedot;
import fi.vm.sade.kayttooikeus.service.KayttajatiedotService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class KayttajatiedotServiceTest extends AbstractServiceIntegrationTest {

    @Autowired
    private KayttajatiedotService kayttajatiedotService;

    @Test
    public void createShouldReturnWithId() {
        String oid = "1.2.3.4.5";
        populate(henkilo(oid));
        KayttajatiedotCreateDto createDto = new KayttajatiedotCreateDto();
        createDto.setUsername("user1");

        KayttajatiedotReadDto readDto = kayttajatiedotService.create(oid, createDto);

        assertThat(readDto.getId()).isNotNull();
    }

    @Test
    public void createShouldThrowOnDuplicateUsername() {
        String oid = "1.2.3.4.5";
        populate(henkilo(oid));
        populate(kayttajatiedot(henkilo("toinen"), "user1"));
        KayttajatiedotCreateDto createDto = new KayttajatiedotCreateDto();
        createDto.setUsername("user1");

        Throwable throwable = catchThrowable(() -> kayttajatiedotService.create(oid, createDto));

        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("on jo käytössä");
    }

}
