package fi.vm.sade.kayttooikeus.service.it;

import fi.vm.sade.kayttooikeus.model.HenkiloTyyppi;
import fi.vm.sade.kayttooikeus.service.HenkiloService;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.List;

import static fi.vm.sade.kayttooikeus.repositories.populate.HenkiloPopulator.henkilo;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusRyhmaPopulator.kayttoOikeusRyhma;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloKayttoOikeusPopulator.myonnettyKayttoOikeus;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloPopulator.organisaatioHenkilo;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class HenkiloServiceTest extends AbstractServiceIntegrationTest {

    @Autowired
    private HenkiloService henkiloService;

    @Test
    @WithMockUser("1.2.3.4.5")
    public void findHenkilosTest() throws Exception {
        List<String> list = henkiloService.findHenkilos(HenkiloTyyppi.VIRKAILIJA, Collections.singletonList("3.4.5.6.7"), null);
        assertEquals(0, list.size());

        populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.5"), "3.4.5.6.7"),
                kayttoOikeusRyhma("RYHMA")
        ));

        populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.6"), "3.4.5.6.7"),
                kayttoOikeusRyhma("RYHMA")
        ));

        populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.6"), "3.4.5.6.7"),
                kayttoOikeusRyhma("RYHMA2")
        ));

        populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.7"), "3.4.5.6.7"),
                kayttoOikeusRyhma("RYHMA2")
        ).voimassaAlkaen(new LocalDate().minusMonths(1)).voimassaPaattyen(new LocalDate().plusMonths(1)));

        populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.8"), "3.4.5.6.7"),
                kayttoOikeusRyhma("RYHMA2")
        ).voimassaAlkaen(new LocalDate().minusMonths(2)).voimassaPaattyen(new LocalDate().minusMonths(1)));

        populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.9"), "3.4.5.6.7"),
                kayttoOikeusRyhma("RYHMA2")
        ).voimassaAlkaen(new LocalDate().plusMonths(1)).voimassaPaattyen(new LocalDate().plusMonths(2)));

        list = henkiloService.findHenkilos(HenkiloTyyppi.VIRKAILIJA, Collections.singletonList("1.1.1.1.1"), null);
        assertEquals(0, list.size());

        list = henkiloService.findHenkilos(HenkiloTyyppi.VIRKAILIJA, Collections.singletonList("3.4.5.6.7"), null);
        assertEquals(3, list.size());

        list = henkiloService.findHenkilos(HenkiloTyyppi.VIRKAILIJA, Collections.singletonList("3.4.5.6.7"), "RYHMA2");
        assertEquals(2, list.size());

        list = henkiloService.findHenkilos(HenkiloTyyppi.PALVELU, Collections.singletonList("3.4.5.6.7"), null);
        assertEquals(0, list.size());
    }

}
