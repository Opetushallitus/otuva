package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.dto.KutsuListDto;
import fi.vm.sade.kayttooikeus.dto.KutsuOrganisaatioListDto;
import fi.vm.sade.kayttooikeus.model.Kutsu;
import fi.vm.sade.kayttooikeus.dto.KutsunTila;
import fi.vm.sade.kayttooikeus.repositories.KutsuRepository.KutsuOrganisaatioOrder;
import fi.vm.sade.kayttooikeus.repositories.criteria.KutsuCriteria;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.List;

import static com.querydsl.core.types.Order.ASC;
import static com.querydsl.core.types.Order.DESC;
import static fi.vm.sade.kayttooikeus.controller.KutsuPopulator.kutsu;
import static fi.vm.sade.kayttooikeus.dto.KutsunTila.AVOIN;
import static fi.vm.sade.kayttooikeus.repositories.KutsuRepository.KutsuOrganisaatioOrder.AIKALEIMA;
import static fi.vm.sade.kayttooikeus.repositories.KutsuRepository.KutsuOrganisaatioOrder.ORGANISAATIO;
import static fi.vm.sade.kayttooikeus.repositories.KutsuRepository.KutsuOrganisaatioOrder.SAHKOPOSTI;
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

    @Test
    public void listKutsuOrganisaatioListDtosTest() {
        Kutsu kutsu1 = populate(kutsu("Aapo", "Esimerkki", "a@eaxmple.com")
                .kutsuja("1.2.3").aikaleima(LocalDateTime.of(2016,1,1,0,0,0, 0))
                .organisaatio(kutsuOrganisaatio("1.2.3.4.5")
                    .ryhma(kayttoOikeusRyhma("RYHMA1")
                        .withKuvaus(text("FI", "B")))
                )),
            kutsu2 = populate(kutsu("Essi", "Esimerkki", "b@eaxmple.com")
                .tila(KutsunTila.KAYTETTY)
                .kutsuja("1.2.4").aikaleima(LocalDateTime.of(2016,2,1,0,0,0,0))
                .organisaatio(kutsuOrganisaatio("1.2.3.4.5")
                    .ryhma(kayttoOikeusRyhma("RYHMA2")
                            .withKuvaus(text("FI", "A"))))
                .organisaatio(kutsuOrganisaatio("1.2.3.4.6")
                    .ryhma(kayttoOikeusRyhma("RYHMA3")
                            .withKuvaus(text("FI", "C"))))
            );

        List<KutsuOrganisaatioListDto> results = kutsuRepository.listKutsuOrganisaatioListDtos(new KutsuCriteria(),
                new OrderBy<KutsuOrganisaatioOrder>(null, null).byDefault(AIKALEIMA, DESC));
        assertEquals(3, results.size());
        assertEquals(kutsu2.getId(), results.get(0).getKutsuId());
        assertEquals("1.2.3.4.6", results.get(0).getOid());
        assertEquals(kutsu2.getId(), results.get(1).getKutsuId());
        assertEquals("1.2.3.4.5", results.get(1).getOid());
        assertEquals(kutsu1.getId(), results.get(2).getKutsuId());
        assertEquals("1.2.3.4.5", results.get(2).getOid());

        results = kutsuRepository.listKutsuOrganisaatioListDtos(new KutsuCriteria(), new OrderBy<>(SAHKOPOSTI, ASC));
        assertEquals(3, results.size());
        assertEquals(kutsu1.getId(), results.get(0).getKutsuId());
        assertEquals(kutsu2.getId(), results.get(1).getKutsuId());
        assertEquals(kutsu2.getId(), results.get(2).getKutsuId());
        assertEquals("1.2.3.4.6", results.get(2).getOid());

        results = kutsuRepository.listKutsuOrganisaatioListDtos(new KutsuCriteria()
                    .withTila(KutsunTila.KAYTETTY,KutsunTila.POISTETTU).withKutsuja("1.2.4"),
                new OrderBy<>(ORGANISAATIO, ASC));
        assertEquals(2, results.size());
        assertEquals(kutsu2.getId(), results.get(0).getKutsuId());
        assertEquals("1.2.3.4.5", results.get(0).getOid());
        assertEquals(kutsu2.getId(), results.get(1).getKutsuId());
        assertEquals("1.2.3.4.6", results.get(1).getOid());
    }
}
