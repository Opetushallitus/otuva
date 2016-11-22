package fi.vm.sade.kayttooikeus.repositories;

import com.querydsl.core.BooleanBuilder;
import fi.vm.sade.kayttooikeus.dto.KutsunTila;
import fi.vm.sade.kayttooikeus.model.QKutsu;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class KutsuCriteria extends BaseCriteria {
    private List<KutsunTila> tilas;
    private String kutsujaOid;
    private String sahkoposti;

    public BooleanBuilder builder(QKutsu kutsu) {
        BooleanBuilder builder = new BooleanBuilder();
        if (used(tilas)) {
            builder.and(kutsu.tila.in(tilas));
        }
        if (used(kutsujaOid)) {
            builder.and(kutsu.kutsuja.eq(kutsujaOid));
        }
        if (used(sahkoposti)) {
            builder.and(kutsu.sahkoposti.eq(sahkoposti));
        }
        return builder;
    }
    
    public KutsuCriteria withTila(KutsunTila...tila) {
        this.tilas = params(tila);
        return this;
    }
    
    public KutsuCriteria withKutsuja(String kutsujaOid) {
        this.kutsujaOid = kutsujaOid;
        return this;
    }
    
    public KutsuCriteria withSahkoposti(String sahkoposti) {
        this.sahkoposti = sahkoposti;
        return this;
    }
}
