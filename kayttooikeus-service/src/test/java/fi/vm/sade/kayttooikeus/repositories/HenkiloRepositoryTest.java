package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.model.OrganisaatioHenkilo;
import fi.vm.sade.kayttooikeus.repositories.criteria.OrganisaatioHenkiloCriteria;
import fi.vm.sade.kayttooikeus.repositories.populate.HenkiloPopulator;
import fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusPopulator;
import fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusRyhmaPopulator;
import fi.vm.sade.kayttooikeus.repositories.populate.MyonnettyKayttooikeusRyhmaTapahtumaPopulator;
import fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloPopulator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
public class HenkiloRepositoryTest extends AbstractRepositoryTest {
    @Autowired
    private HenkiloHibernateRepository henkiloHibernateRepository;

    private void populateFindOidsStuff() {
        KayttoOikeusRyhmaPopulator kor1Populator = KayttoOikeusRyhmaPopulator.kayttoOikeusRyhma("kor1Populator");
        kor1Populator.withOikeus(KayttoOikeusPopulator.oikeus("KAYTTOOIKEUS", "READ"));
        KayttoOikeusRyhma kor1 = populate(kor1Populator);

        OrganisaatioHenkilo oh1 = populate(OrganisaatioHenkiloPopulator.organisaatioHenkilo(HenkiloPopulator.henkilo("1.2.0.0.1").withUsername("a"), "2.1.0.1"));
        populate(MyonnettyKayttooikeusRyhmaTapahtumaPopulator.kayttooikeusTapahtuma(oh1, kor1));

        OrganisaatioHenkilo oh2 = populate(OrganisaatioHenkiloPopulator.organisaatioHenkilo(HenkiloPopulator.henkilo("1.2.0.0.2").withUsername("b"), "2.1.0.1"));
        populate(MyonnettyKayttooikeusRyhmaTapahtumaPopulator.kayttooikeusTapahtuma(oh2, kor1));

        KayttoOikeusRyhmaPopulator kor2Populator = KayttoOikeusRyhmaPopulator.kayttoOikeusRyhma("kor2Populator");
        kor2Populator.withOikeus(KayttoOikeusPopulator.oikeus("KAYTTOOIKEUS", "READ"));
        KayttoOikeusRyhma kor2 = populate(kor2Populator);
        OrganisaatioHenkilo oh3 = populate(OrganisaatioHenkiloPopulator.organisaatioHenkilo(HenkiloPopulator.henkilo("1.2.0.0.3").withUsername("c"), "2.1.0.1"));
        populate(MyonnettyKayttooikeusRyhmaTapahtumaPopulator.kayttooikeusTapahtuma(oh3, kor2));
    }

    @Test
    public void findOidsByFindsHenkilosWithOrganisaatioOid() {
        populateFindOidsStuff();

        OrganisaatioHenkiloCriteria criteria = new OrganisaatioHenkiloCriteria();
        criteria.setOrganisaatioOids(Set.of("2.1.0.1"));

        Set<String> oids = this.henkiloHibernateRepository.findOidsBy(criteria);
        assertThat(oids).containsExactly("1.2.0.0.1", "1.2.0.0.2", "1.2.0.0.3");
    }

    @Test
    public void findOidsByFindsHenkilosWithOrganisaatioOidAndKayttoOikeusRyhma() {
        populateFindOidsStuff();

        OrganisaatioHenkiloCriteria criteria1 = new OrganisaatioHenkiloCriteria();
        criteria1.setOrganisaatioOids(Set.of("2.1.0.1"));
        criteria1.setKayttoOikeusRyhmaNimet(Set.of("kor1Populator"));

        Set<String> oids1 = this.henkiloHibernateRepository.findOidsBy(criteria1);
        assertThat(oids1).containsExactly("1.2.0.0.1", "1.2.0.0.2");

        OrganisaatioHenkiloCriteria criteria2 = new OrganisaatioHenkiloCriteria();
        criteria2.setOrganisaatioOids(Set.of("2.1.0.1"));
        criteria2.setKayttoOikeusRyhmaNimet(Set.of("kor2Populator"));

        Set<String> oids2 = this.henkiloHibernateRepository.findOidsBy(criteria2);
        assertThat(oids2).containsExactly("1.2.0.0.3");

        OrganisaatioHenkiloCriteria criteria3 = new OrganisaatioHenkiloCriteria();
        criteria3.setOrganisaatioOids(Set.of("2.1.0.1"));
        criteria3.setKayttoOikeusRyhmaNimet(Set.of("kor1Populator", "kor2Populator"));

        Set<String> oids3 = this.henkiloHibernateRepository.findOidsBy(criteria3);
        assertThat(oids3).containsExactly("1.2.0.0.1", "1.2.0.0.2", "1.2.0.0.3");
    }
}
