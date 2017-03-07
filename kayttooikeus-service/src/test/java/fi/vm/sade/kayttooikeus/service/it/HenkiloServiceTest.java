package fi.vm.sade.kayttooikeus.service.it;

import com.google.common.collect.Sets;
import fi.vm.sade.kayttooikeus.dto.HenkiloTyyppi;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioOidsSearchDto;
import fi.vm.sade.kayttooikeus.model.MyonnettyKayttoOikeusRyhmaTapahtuma;
import fi.vm.sade.kayttooikeus.model.OrganisaatioHenkilo;
import fi.vm.sade.kayttooikeus.repositories.MyonnettyKayttoOikeusRyhmaTapahtumaDataRepository;
import fi.vm.sade.kayttooikeus.repositories.OrganisaatioHenkiloDataRepository;
import fi.vm.sade.kayttooikeus.service.HenkiloService;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static fi.vm.sade.kayttooikeus.repositories.populate.HenkiloPopulator.henkilo;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusPopulator.oikeus;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusRyhmaPopulator.kayttoOikeusRyhma;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloKayttoOikeusPopulator.myonnettyKayttoOikeus;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloPopulator.organisaatioHenkilo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
public class HenkiloServiceTest extends AbstractServiceIntegrationTest {

    @Autowired
    private HenkiloService henkiloService;

    @Autowired
    private OrganisaatioHenkiloDataRepository organisaatioHenkiloDataRepository;

    @Autowired
    private MyonnettyKayttoOikeusRyhmaTapahtumaDataRepository myonnettyKayttoOikeusRyhmaTapahtumaDataRepository;

    @Test
    @WithMockUser(value = "1.2.3.4.5", authorities = "ROLE_APP_HENKILONHALLINTA_OPHREKISTERI")
    public void findHenkilosTest() throws Exception {
        List<String> list = henkiloService.findHenkilos(new OrganisaatioOidsSearchDto(HenkiloTyyppi.VIRKAILIJA, Collections.singletonList("3.4.5.6.7"), null));
        assertEquals(0, list.size());

        populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.5"), "3.4.5.6.7"),
                kayttoOikeusRyhma("RYHMA")
        ).voimassaAlkaen(new LocalDate().minusMonths(1)).voimassaPaattyen(new LocalDate().plusMonths(1)));

        populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.6"), "3.4.5.6.7"),
                kayttoOikeusRyhma("RYHMA")
        ).voimassaAlkaen(new LocalDate().minusMonths(1)).voimassaPaattyen(new LocalDate().plusMonths(1)));

        populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.6"), "3.4.5.6.7"),
                kayttoOikeusRyhma("RYHMA2")
        ).voimassaAlkaen(new LocalDate().minusMonths(1)).voimassaPaattyen(new LocalDate().plusMonths(1)));

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

        list = henkiloService.findHenkilos(new OrganisaatioOidsSearchDto(HenkiloTyyppi.VIRKAILIJA, Collections.singletonList("1.1.1.1.1"), null));
        assertEquals(0, list.size());

        list = henkiloService.findHenkilos(new OrganisaatioOidsSearchDto(HenkiloTyyppi.VIRKAILIJA, Collections.singletonList("3.4.5.6.7"), null));
        assertEquals(3, list.size());
        assertTrue(list.containsAll(Arrays.asList("1.2.3.4.5", "1.2.3.4.6", "1.2.3.4.7")));

        list = henkiloService.findHenkilos(new OrganisaatioOidsSearchDto(HenkiloTyyppi.VIRKAILIJA, Collections.singletonList("3.4.5.6.7"), "RYHMA2"));
        assertEquals(2, list.size());
        assertTrue(list.containsAll(Arrays.asList("1.2.3.4.6", "1.2.3.4.7")));

        list = henkiloService.findHenkilos(new OrganisaatioOidsSearchDto(HenkiloTyyppi.PALVELU, Collections.singletonList("3.4.5.6.7"), null));
        assertEquals(0, list.size());
    }

    @Test
    @Transactional
    @WithMockUser(value = "1.2.3.4.1", authorities = "ROLE_APP_HENKILONHALLINTA_OPHREKISTERI")
    public void passivoiHenkiloOrganisationsAndKayttooikeus() {
        // Käsittelijä
        populate(henkilo("1.2.3.4.1"));
        // Passivoitava
        String oidHenkilo = "1.2.3.4.5";
        MyonnettyKayttoOikeusRyhmaTapahtuma myonnettyKayttoOikeusRyhmaTapahtuma = populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo(oidHenkilo)
                        .withPassivoitu(false), "4.5.6.7.8")
                        .tehtavanimike("testaaja"),
                kayttoOikeusRyhma("RYHMA2")
                        .withOikeus(oikeus("KOODISTO", "WRITE")))
                .voimassaAlkaen(new LocalDate().minusMonths(1)).voimassaPaattyen(new LocalDate().plusMonths(1)));
        myonnettyKayttoOikeusRyhmaTapahtuma.getOrganisaatioHenkilo().setMyonnettyKayttoOikeusRyhmas(Sets.newHashSet(myonnettyKayttoOikeusRyhmaTapahtuma));
        this.em.persist(myonnettyKayttoOikeusRyhmaTapahtuma);
        this.henkiloService.passivoiHenkiloOrganisationsAndKayttooikeus("1.2.3.4.5", "1.2.3.4.1");

        List<OrganisaatioHenkilo> henkilo = this.organisaatioHenkiloDataRepository.findByHenkiloOidHenkilo(oidHenkilo);
        assertThat(henkilo.size()).isEqualTo(1);
        assertThat(henkilo.get(0).getMyonnettyKayttoOikeusRyhmas()).isEmpty();
        MyonnettyKayttoOikeusRyhmaTapahtuma mkrt = this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository.findOne(myonnettyKayttoOikeusRyhmaTapahtuma.getId());
        assertThat(mkrt).isNull();
    }
}
