package fi.vm.sade.kayttooikeus.repositories.populate;

import com.google.common.collect.Sets;
import fi.vm.sade.kayttooikeus.dto.KayttoOikeudenTila;
import fi.vm.sade.kayttooikeus.model.*;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.persistence.EntityManager;

import java.util.Date;
import java.util.Optional;

import static fi.vm.sade.kayttooikeus.repositories.populate.PalveluPopulator.palvelu;

public class MyonnettyKayttooikeusRyhmaTapahtumaPopulator implements Populator<MyonnettyKayttoOikeusRyhmaTapahtuma> {
    private final OrganisaatioHenkilo organisaatioHenkilo;
    private final KayttoOikeusRyhma kayttoOikeusRyhma;
    private Optional<KayttoOikeudenTila> kayttoOikeudenTila;
    private Optional<DateTime> aikaleima;
    private Optional<LocalDate> voimassaAlkuPvm;

    private MyonnettyKayttooikeusRyhmaTapahtumaPopulator(OrganisaatioHenkilo organisaatioHenkilo, KayttoOikeusRyhma kayttoOikeusRyhma) {
        this.organisaatioHenkilo = organisaatioHenkilo;
        this.kayttoOikeusRyhma = kayttoOikeusRyhma;
        this.kayttoOikeudenTila = Optional.empty();
        this.aikaleima = Optional.empty();
        this.voimassaAlkuPvm = Optional.empty();
    }
    
    public static MyonnettyKayttooikeusRyhmaTapahtumaPopulator kayttooikeusTapahtuma(OrganisaatioHenkilo organisaatioHenkilo,
                                                                                     KayttoOikeusRyhma kayttoOikeusRyhma) {
        return new MyonnettyKayttooikeusRyhmaTapahtumaPopulator(organisaatioHenkilo, kayttoOikeusRyhma);
    }

    public MyonnettyKayttooikeusRyhmaTapahtumaPopulator withTila(KayttoOikeudenTila kayttoOikeudenTila) {
        this.kayttoOikeudenTila = Optional.ofNullable(kayttoOikeudenTila);
        return this;
    }

    public MyonnettyKayttooikeusRyhmaTapahtumaPopulator withAikaleima(DateTime aikaleima) {
        this.aikaleima = Optional.ofNullable(aikaleima);
        return this;
    }

    public MyonnettyKayttooikeusRyhmaTapahtumaPopulator withAlkupvm(LocalDate voimassaAlkupvm) {
        this.voimassaAlkuPvm = Optional.ofNullable(voimassaAlkupvm);
        return this;
    }

    @Override
    public MyonnettyKayttoOikeusRyhmaTapahtuma apply(EntityManager entityManager) {
        MyonnettyKayttoOikeusRyhmaTapahtuma mkrt = new MyonnettyKayttoOikeusRyhmaTapahtuma();
        mkrt.setOrganisaatioHenkilo(this.organisaatioHenkilo);
        mkrt.getOrganisaatioHenkilo().setMyonnettyKayttoOikeusRyhmas(Sets.newHashSet(mkrt));
        mkrt.setKayttoOikeusRyhma(this.kayttoOikeusRyhma);
        mkrt.setTila(this.kayttoOikeudenTila.orElse(KayttoOikeudenTila.MYONNETTY));
        mkrt.setAikaleima(this.aikaleima.orElse(DateTime.now()));
        mkrt.setVoimassaAlkuPvm(this.voimassaAlkuPvm.orElse(LocalDate.now()));
        entityManager.persist(mkrt);
        return mkrt;
    }
}
