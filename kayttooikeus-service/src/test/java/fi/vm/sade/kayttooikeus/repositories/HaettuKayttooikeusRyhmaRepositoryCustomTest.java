package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.dto.KayttoOikeudenTila;
import fi.vm.sade.kayttooikeus.enumeration.KayttooikeusRooli;
import fi.vm.sade.kayttooikeus.model.HaettuKayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.repositories.criteria.AnomusCriteria;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static fi.vm.sade.kayttooikeus.repositories.populate.HaettuKayttoOikeusRyhmaPopulator.haettuKayttooikeusryhma;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusRyhmaPopulator.kayttoOikeusRyhma;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusPopulator.oikeus;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class HaettuKayttooikeusRyhmaRepositoryCustomTest extends AbstractRepositoryTest {
    @Autowired
    private HaettuKayttooikeusRyhmaRepository haettuKayttooikeusRyhmaRepository;

    @Before
    public void setup() {
        populate(haettuKayttooikeusryhma(null)
                .withRyhma(kayttoOikeusRyhma("Rekisterinpitäjä (vain OPHn käytössä)")
                        .withOikeus(oikeus("HENKILOHALLINTA", KayttooikeusRooli.VASTUUKAYTTAJAT.getGroupName()))));
        populate(haettuKayttooikeusryhma(KayttoOikeudenTila.ANOTTU)
                .withRyhma(kayttoOikeusRyhma("Pääkäyttäjä (kk)")
                        .withOikeus(oikeus("HENKILOHALLINTA", KayttooikeusRooli.VASTUUKAYTTAJAT.getGroupName()))));
        // Hidden kayttooikeusryhmas are not fetched
        populate(haettuKayttooikeusryhma(null)
                .withRyhma(kayttoOikeusRyhma("Koodiston ylläpitäjä")
                        .withOikeus(oikeus("HENKILOHALLINTA", KayttooikeusRooli.VASTUUKAYTTAJAT.getGroupName()))
                        .asHidden()));
        populate(haettuKayttooikeusryhma(KayttoOikeudenTila.MYONNETTY)
                .withRyhma(kayttoOikeusRyhma("Granted ryhmä")));
        populate(haettuKayttooikeusryhma(null)
                .withRyhma(kayttoOikeusRyhma("Some random ryhmä")));
    }

    @Test
    public void findByBasic() throws Exception {
        AnomusCriteria anomusCriteria = AnomusCriteria.builder()
                .onlyActive(true)
                .build();
        List<HaettuKayttoOikeusRyhma> haetutResult = this.haettuKayttooikeusRyhmaRepository
                .findBy(anomusCriteria, null, null, null);
        assertThat(haetutResult)
                .extracting("kayttoOikeusRyhma")
                .extracting("name")
                .containsExactlyInAnyOrder("Rekisterinpitäjä (vain OPHn käytössä)", "Pääkäyttäjä (kk)",
                        "Some random ryhmä");
    }

    @Test
    public void findByBasicWithLimitOffset() throws Exception {
        AnomusCriteria anomusCriteria = AnomusCriteria.builder()
                .onlyActive(true)
                .build();
        List<HaettuKayttoOikeusRyhma> haetutResult = this.haettuKayttooikeusRyhmaRepository
                .findBy(anomusCriteria, 2L, 1L, null);
        assertThat(haetutResult)
                .extracting("kayttoOikeusRyhma")
                .extracting("name")
                .containsExactlyInAnyOrder("Pääkäyttäjä (kk)", "Some random ryhmä");
    }

    @Test
    public void findByAdminView() throws Exception {
        AnomusCriteria anomusCriteria = AnomusCriteria.builder()
                .onlyActive(true)
                .adminView(true)
                .build();
        List<HaettuKayttoOikeusRyhma> haetutResult = this.haettuKayttooikeusRyhmaRepository
                .findBy(anomusCriteria, null, null, null);
        assertThat(haetutResult)
                .extracting("kayttoOikeusRyhma")
                .extracting("name")
                .containsExactlyInAnyOrder("Rekisterinpitäjä (vain OPHn käytössä)", "Pääkäyttäjä (kk)");
    }

}
