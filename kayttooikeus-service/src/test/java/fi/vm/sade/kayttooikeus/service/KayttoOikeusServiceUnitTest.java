package fi.vm.sade.kayttooikeus.service;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import fi.vm.sade.kayttooikeus.model.QMyonnettyKayttoOikeusRyhmaTapahtuma;
import fi.vm.sade.kayttooikeus.model.QOrganisaatioHenkilo;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRyhmaRepository;
import fi.vm.sade.kayttooikeus.service.it.AbstractServiceIntegrationTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
public class KayttoOikeusServiceUnitTest extends AbstractServiceIntegrationTest {
    @Autowired
    private KayttoOikeusService kayttoOikeusService;

    @MockBean
    private KayttoOikeusRyhmaRepository kayttoOikeusRyhmaRepositoryMock;

    @Test
    public void findKayttooikeusryhmatAndOrganisaatioByHenkiloOidTest() {
        String orgOid = "1.0.0.102.1";
        QOrganisaatioHenkilo organisaatioHenkilo = QOrganisaatioHenkilo.organisaatioHenkilo;
        QMyonnettyKayttoOikeusRyhmaTapahtuma myonnettyKayttoOikeusRyhmaTapahtuma
                = QMyonnettyKayttoOikeusRyhmaTapahtuma.myonnettyKayttoOikeusRyhmaTapahtuma;
        Tuple x = Projections.tuple(organisaatioHenkilo.organisaatioOid, myonnettyKayttoOikeusRyhmaTapahtuma.kayttoOikeusRyhma.id)
                .newInstance(orgOid, 12001L);
        given(this.kayttoOikeusRyhmaRepositoryMock.findOrganisaatioOidAndRyhmaIdByHenkiloOid("1.2.3.4.5"))
                .willReturn(Collections.singletonList(x));
        Map<String, List<Integer>> result = kayttoOikeusService.findKayttooikeusryhmatAndOrganisaatioByHenkiloOid("1.2.3.4.5");
        assertThat(result.containsKey(orgOid)).isTrue();
        assertThat(result.get(orgOid).get(0)).isEqualTo(12001);
    }
}
