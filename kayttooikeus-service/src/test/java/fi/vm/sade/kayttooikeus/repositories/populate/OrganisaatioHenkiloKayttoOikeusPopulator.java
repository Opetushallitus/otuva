package fi.vm.sade.kayttooikeus.repositories.populate;

import fi.vm.sade.kayttooikeus.model.KayttoOikeudenTila;
import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.model.MyonnettyKayttoOikeusRyhmaTapahtuma;
import fi.vm.sade.kayttooikeus.model.OrganisaatioHenkilo;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.persistence.EntityManager;

public class OrganisaatioHenkiloKayttoOikeusPopulator implements Populator<MyonnettyKayttoOikeusRyhmaTapahtuma> {
    private final Populator<OrganisaatioHenkilo> henkilo;
    private final Populator<KayttoOikeusRyhma> kayttoOikeusRyhma;

    public OrganisaatioHenkiloKayttoOikeusPopulator(Populator<OrganisaatioHenkilo> henkilo, Populator<KayttoOikeusRyhma> kayttoOikeusRyhma) {
        this.henkilo = henkilo;
        this.kayttoOikeusRyhma = kayttoOikeusRyhma;
    }
    
    public static OrganisaatioHenkiloKayttoOikeusPopulator myonnettyKayttoOikeus(Populator<OrganisaatioHenkilo> henkilo, Populator<KayttoOikeusRyhma> kayttoOikeusRyhma) {
        return new OrganisaatioHenkiloKayttoOikeusPopulator(henkilo, kayttoOikeusRyhma);
    }

    @Override
    public MyonnettyKayttoOikeusRyhmaTapahtuma apply(EntityManager entityManager) {
        MyonnettyKayttoOikeusRyhmaTapahtuma tapahtuma = new MyonnettyKayttoOikeusRyhmaTapahtuma();
        OrganisaatioHenkilo organisaatioHenkilo = henkilo.apply(entityManager);
        tapahtuma.setOrganisaatioHenkilo(henkilo.apply(entityManager));
        tapahtuma.setAikaleima(new DateTime());
        tapahtuma.setSyy("syy");
        tapahtuma.setKasittelija(organisaatioHenkilo.getHenkilo());
        tapahtuma.setTila(KayttoOikeudenTila.MYONNETTY);
        tapahtuma.setVoimassaAlkuPvm(new LocalDate());
        tapahtuma.setKayttoOikeusRyhma(kayttoOikeusRyhma.apply(entityManager));
        entityManager.persist(tapahtuma);
        
        return tapahtuma;
    }
}
