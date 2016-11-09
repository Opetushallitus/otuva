package fi.vm.sade.kayttooikeus.service.it;


import com.querydsl.core.types.Order;
import fi.vm.sade.kayttooikeus.dto.KutsuListDto;
import fi.vm.sade.kayttooikeus.dto.KutsunTila;
import fi.vm.sade.kayttooikeus.dto.TextGroupMapDto;
import fi.vm.sade.kayttooikeus.model.Kutsu;
import fi.vm.sade.kayttooikeus.repositories.KutsuRepository.KutsuOrganisaatioOrder;
import fi.vm.sade.kayttooikeus.repositories.OrderBy;
import fi.vm.sade.kayttooikeus.service.KutsuService;
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
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusRyhmaPopulator.kayttoOikeusRyhma;
import static fi.vm.sade.kayttooikeus.repositories.populate.KutsuOrganisaatioPopulator.kutsuOrganisaatio;
import static org.junit.Assert.assertEquals;
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
}
