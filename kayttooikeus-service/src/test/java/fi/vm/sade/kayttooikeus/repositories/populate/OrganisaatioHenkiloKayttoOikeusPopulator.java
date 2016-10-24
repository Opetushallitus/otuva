package fi.vm.sade.kayttooikeus.repositories.populate;

import fi.vm.sade.kayttooikeus.dto.KayttoOikeudenTila;
import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.model.MyonnettyKayttoOikeusRyhmaTapahtuma;
import fi.vm.sade.kayttooikeus.model.OrganisaatioHenkilo;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.persistence.EntityManager;

public class OrganisaatioHenkiloKayttoOikeusPopulator implements Populator<MyonnettyKayttoOikeusRyhmaTapahtuma> {
    private final Populator<OrganisaatioHenkilo> henkilo;
    private final Populator<KayttoOikeusRyhma> kayttoOikeusRyhma;
    private LocalDate voimassaAlkaen = new LocalDate();
    private LocalDate voimassaPaattyen;

    public OrganisaatioHenkiloKayttoOikeusPopulator(Populator<OrganisaatioHenkilo> henkilo, Populator<KayttoOikeusRyhma> kayttoOikeusRyhma) {
        this.henkilo = henkilo;
        this.kayttoOikeusRyhma = kayttoOikeusRyhma;
    }
    
    public static OrganisaatioHenkiloKayttoOikeusPopulator myonnettyKayttoOikeus(Populator<OrganisaatioHenkilo> henkilo, Populator<KayttoOikeusRyhma> kayttoOikeusRyhma) {
        return new OrganisaatioHenkiloKayttoOikeusPopulator(henkilo, kayttoOikeusRyhma);
    }
    
    public OrganisaatioHenkiloKayttoOikeusPopulator voimassaAlkaen(LocalDate alkaen) {
        this.voimassaAlkaen = alkaen;
        return this;
    }
    
    public OrganisaatioHenkiloKayttoOikeusPopulator voimassaPaattyen(LocalDate voimassaPaattyen) {
        this.voimassaPaattyen = voimassaPaattyen;
        return this;
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
        tapahtuma.setVoimassaAlkuPvm(voimassaAlkaen);
        tapahtuma.setVoimassaLoppuPvm(voimassaPaattyen);
        tapahtuma.setKayttoOikeusRyhma(kayttoOikeusRyhma.apply(entityManager));
        entityManager.persist(tapahtuma);
        return tapahtuma;
    }
}
