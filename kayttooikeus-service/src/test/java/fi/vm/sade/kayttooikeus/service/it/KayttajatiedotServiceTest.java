package fi.vm.sade.kayttooikeus.service.it;

import fi.vm.sade.kayttooikeus.dto.KayttajatiedotCreateDto;
import fi.vm.sade.kayttooikeus.dto.KayttajatiedotReadDto;
import static fi.vm.sade.kayttooikeus.repositories.populate.HenkiloPopulator.henkilo;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttajatiedotPopulator.kayttajatiedot;

import fi.vm.sade.kayttooikeus.model.Identification;
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
    public void createShouldReturn() {
        String oid = "1.2.3.4.5";
        populate(henkilo(oid));
        KayttajatiedotCreateDto createDto = new KayttajatiedotCreateDto();
        createDto.setUsername("user1");

        KayttajatiedotReadDto readDto = kayttajatiedotService.create(oid, createDto);

        assertThat(readDto).isNotNull();
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

    @Test
//    @Ignore
    public void testValidateUsernamePassword() throws Exception {
        final String henkiloOid = "1.2.246.562.24.27470134096";
        String username = "eetu.esimerkki@geemail.fi";
        String password = "paSsword&23";
        kayttajatiedotService.changePasswordAsAdmin(henkiloOid, password);
//        Identification identification = kayttajatiedotService.validate(username, password);
//        assertThat(identification).isNotNull();
//
//        Identification identification1 = kayttajatiedotService.validate(username, "notthecorrectpassword");
//        assertThat(identification1).isNull();
    }


}
