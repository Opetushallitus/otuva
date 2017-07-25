package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.dto.KutsuListDto;
import fi.vm.sade.kayttooikeus.dto.KutsunTila;
import fi.vm.sade.kayttooikeus.model.Kutsu;
import fi.vm.sade.kayttooikeus.repositories.criteria.KutsuCriteria;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.List;

import static fi.vm.sade.kayttooikeus.controller.KutsuPopulator.kutsu;
import static fi.vm.sade.kayttooikeus.dto.KutsunTila.AVOIN;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusRyhmaPopulator.kayttoOikeusRyhma;
import static fi.vm.sade.kayttooikeus.repositories.populate.KutsuOrganisaatioPopulator.kutsuOrganisaatio;
import static fi.vm.sade.kayttooikeus.repositories.populate.TextGroupPopulator.text;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class KutsuRepositoryTest extends AbstractRepositoryTest {
    @Autowired
    private KutsuRepository kutsuRepository;

    @Test
    public void listKutsuListDtosTest() {
        Kutsu kutsu = populate(kutsu("Aapo", "Esimerkki", "a@eaxmple.com")
            .kutsuja("1.2.3").aikaleima(LocalDateTime.of(2016,1,1,0,0,0, 0))
            .organisaatio(kutsuOrganisaatio("1.2.3.4.5")
                .ryhma(kayttoOikeusRyhma("RYHMA")
                    .withKuvaus(text("FI", "Kuvaus")))
            )
        );

        List<KutsuListDto> results = kutsuRepository.listKutsuListDtos(new KutsuCriteria().withKutsuja("1.2.3")
                .withTila(KutsunTila.AVOIN));
        assertEquals(1, results.size());
        KutsuListDto dto = results.get(0);
        assertEquals(LocalDateTime.of(2016,1,1,0,0,0, 0), dto.getAikaleima());
        assertEquals("a@eaxmple.com", dto.getSahkoposti());
        assertEquals(AVOIN, dto.getTila());
        assertEquals(kutsu.getId(), dto.getId());

        results = kutsuRepository.listKutsuListDtos(new KutsuCriteria().withKutsuja("1.2.3")
                .withTila(KutsunTila.POISTETTU, KutsunTila.KAYTETTY));
        assertEquals(0, results.size());

        results = kutsuRepository.listKutsuListDtos(new KutsuCriteria());
        assertEquals(1, results.size());
    }

}
