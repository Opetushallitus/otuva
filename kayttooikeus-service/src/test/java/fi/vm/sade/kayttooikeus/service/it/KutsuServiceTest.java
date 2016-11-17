package fi.vm.sade.kayttooikeus.service.it;


import com.querydsl.core.types.Order;
import fi.vm.sade.kayttooikeus.dto.KutsuListDto;
import fi.vm.sade.kayttooikeus.dto.KutsunTila;
import fi.vm.sade.kayttooikeus.dto.TextGroupMapDto;
import fi.vm.sade.kayttooikeus.model.Kutsu;
import fi.vm.sade.kayttooikeus.repositories.KutsuRepository.KutsuOrganisaatioOrder;
import fi.vm.sade.kayttooikeus.repositories.OrderBy;
import fi.vm.sade.kayttooikeus.service.KutsuService;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static fi.vm.sade.kayttooikeus.controller.KutsuPopulator.kutsu;
import fi.vm.sade.kayttooikeus.dto.KutsuCreateDto;
import fi.vm.sade.kayttooikeus.dto.KutsuReadDto;
import fi.vm.sade.kayttooikeus.model.MyonnettyKayttoOikeusRyhmaTapahtuma;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusRyhmaPopulator.kayttoOikeusRyhma;
import static fi.vm.sade.kayttooikeus.repositories.populate.KutsuOrganisaatioPopulator.kutsuOrganisaatio;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloKayttoOikeusPopulator.myonnettyKayttoOikeus;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloPopulator.organisaatioHenkilo;
import static fi.vm.sade.kayttooikeus.repositories.populate.TextGroupPopulator.text;
import java.util.AbstractMap;
import java.util.LinkedHashSet;
import java.util.Set;
import static java.util.stream.Collectors.toSet;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
public class KutsuServiceTest extends AbstractServiceIntegrationTest {
    @Autowired
    private KutsuService kutsuService;
    
    @MockBean
    private OrganisaatioClient organisaatioClient;

    @Test
    @WithMockUser(username = "1.2.4", authorities = "ROLE_APP_HENKILONHALLINTA_CRUD")
    public void listAvoinKutsus() {
        Kutsu kutsu1 = populate(kutsu("a@eaxmple.com")
                .kutsuja("1.2.3").aikaleima(new DateTime(2016,1,1,0,0,0))
                .organisaatio(kutsuOrganisaatio("1.2.3.4.5")
                        .ryhma(kayttoOikeusRyhma("RYHMA1"))
                )),
            kutsu2 = populate(kutsu("b@eaxmple.com")
                .kutsuja("1.2.4").aikaleima(new DateTime(2016,2,1,0,0,0))
                .organisaatio(kutsuOrganisaatio("1.2.3.4.5")
                        .ryhma(kayttoOikeusRyhma("RYHMA2")))
                .organisaatio(kutsuOrganisaatio("1.2.3.4.6")
                        .ryhma(kayttoOikeusRyhma("RYHMA3")))
            ),
            kutsu3 = populate(kutsu("a@eaxmple.com")
                .tila(KutsunTila.POISTETTU)
                .kutsuja("1.2.4").aikaleima(new DateTime(2016,1,1,0,0,0))
                .organisaatio(kutsuOrganisaatio("1.2.3.4.5").ryhma(kayttoOikeusRyhma("RYHMA1"))
            ));

        OrganisaatioRDTO org1 = new OrganisaatioRDTO();
        org1.setOid("1.2.3.4.5");
        org1.setNimi(new TextGroupMapDto().put("FI", "Nimi2").asMap());
        OrganisaatioRDTO org2 = new OrganisaatioRDTO();
        org2.setOid("1.2.3.4.6");
        org2.setNimi(new TextGroupMapDto().put("FI", "Nimi1").asMap());
        given(this.organisaatioClient.getOrganisaatioPerustiedot("1.2.3.4.5")).willReturn(org1);
        given(this.organisaatioClient.getOrganisaatioPerustiedot("1.2.3.4.6")).willReturn(org2);
        
        List<KutsuListDto> kutsus = kutsuService.listAvoinKutsus(new OrderBy<>(KutsuOrganisaatioOrder.ORGANISAATIO, Order.ASC));
        assertEquals(1, kutsus.size());
        assertEquals(new DateTime(2016,2,1,0,0,0), kutsus.get(0).getAikaleima());
        assertEquals(kutsu2.getId(), kutsus.get(0).getId());
        assertEquals("b@eaxmple.com", kutsus.get(0).getSahkoposti());
        assertEquals(2, kutsus.get(0).getOrganisaatiot().size());
        assertEquals("1.2.3.4.6", kutsus.get(0).getOrganisaatiot().get(0).getOid());
        assertEquals("Nimi1", kutsus.get(0).getOrganisaatiot().get(0).getNimi().get("FI"));
        assertEquals("Nimi2", kutsus.get(0).getOrganisaatiot().get(1).getNimi().get("FI"));
    }

    @Test
    @WithMockUser(username = "1.2.4", authorities = "ROLE_APP_HENKILONHALLINTA_CRUD")
    public void createKutsuTest() {
        MyonnettyKayttoOikeusRyhmaTapahtuma populate = populate(myonnettyKayttoOikeus(
                organisaatioHenkilo("1.2.4", "1.2.246.562.10.00000000001"),
                kayttoOikeusRyhma("kayttoOikeusRyhma").withKuvaus(text("fi", "Käyttöoikeusryhmä"))));
        Long kayttoOikeusRyhmaId = populate.getKayttoOikeusRyhma().getId();
        KutsuCreateDto.KayttoOikeusRyhmaDto kutsuKayttoOikeusRyhma = new KutsuCreateDto.KayttoOikeusRyhmaDto();
        kutsuKayttoOikeusRyhma.setId(kayttoOikeusRyhmaId);

        KutsuCreateDto kutsu = new KutsuCreateDto();
        kutsu.setSahkoposti("example@example.com");
        kutsu.setAsiointikieli("fi");
        kutsu.setOrganisaatiot(new LinkedHashSet<>());
        KutsuCreateDto.KutsuOrganisaatioDto kutsuOrganisaatio = new KutsuCreateDto.KutsuOrganisaatioDto();
        kutsuOrganisaatio.setOrganisaatioOid("1.2.246.562.10.00000000001");
        kutsuOrganisaatio.setKayttoOikeusRyhmat(Stream.of(kutsuKayttoOikeusRyhma).collect(toSet()));
        kutsu.getOrganisaatiot().add(kutsuOrganisaatio);

        long id = kutsuService.createKutsu(kutsu);
        KutsuReadDto tallennettu = kutsuService.getKutsu(id);

        assertThat(tallennettu.getAsiointikieli()).isEqualTo("fi");
        Set<KutsuReadDto.KutsuOrganisaatioDto> organisaatiot = tallennettu.getOrganisaatiot();
        assertThat(organisaatiot).hasSize(1);
        KutsuReadDto.KutsuOrganisaatioDto tallennettuKutsuOrganisaatio = organisaatiot.iterator().next();
        Set<KutsuReadDto.KayttoOikeusRyhmaDto> kayttoOikeusRyhmat = tallennettuKutsuOrganisaatio.getKayttoOikeusRyhmat();
        assertThat(kayttoOikeusRyhmat).hasSize(1);
        KutsuReadDto.KayttoOikeusRyhmaDto tallennettuKutsuKayttoOikeusRyhma = kayttoOikeusRyhmat.iterator().next();
        assertThat(tallennettuKutsuKayttoOikeusRyhma.getNimi().getTexts()).containsExactly(new AbstractMap.SimpleEntry<>("fi", "Käyttöoikeusryhmä"));
    }

    @Test
    @WithMockUser(username = "1.2.4", authorities = "ROLE_APP_HENKILONHALLINTA_CRUD")
    public void deleteKutsuTest() {
        Kutsu kutsu = populate(kutsu("b@eaxmple.com")
                .kutsuja("1.2.4")
                .organisaatio(kutsuOrganisaatio("1.2.3.4.5")
                        .ryhma(kayttoOikeusRyhma("RYHMA2")))
        );
        kutsuService.deleteKutsu(kutsu.getId());
        em.flush();
        assertEquals(KutsunTila.POISTETTU, kutsu.getTila());
        assertEquals("1.2.4", kutsu.getPoistaja());
        assertNotNull(kutsu.getPoistettu());
    }


    @Test(expected = NotFoundException.class)
    @WithMockUser(username = "1.2.4", authorities = "ROLE_APP_HENKILONHALLINTA_CRUD")
    public void deleteKutsuOtherKutsujaFailsTest() {
        Kutsu kutsu = populate(kutsu("b@eaxmple.com")
                .kutsuja("1.2.5")
                .organisaatio(kutsuOrganisaatio("1.2.3.4.5")
                        .ryhma(kayttoOikeusRyhma("RYHMA2")))
        );
        kutsuService.deleteKutsu(kutsu.getId());
    }
}
