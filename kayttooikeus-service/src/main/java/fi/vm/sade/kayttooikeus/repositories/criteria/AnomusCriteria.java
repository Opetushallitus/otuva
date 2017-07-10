package fi.vm.sade.kayttooikeus.repositories.criteria;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import fi.vm.sade.kayttooikeus.model.AnomuksenTila;
import fi.vm.sade.kayttooikeus.model.QAnomus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AnomusCriteria implements KayttooikeusCriteria<QAnomus> {

    private String q;
    private LocalDateTime anottuAlku;
    private LocalDateTime anottuLoppu;
    private Set<AnomuksenTila> tilat;
    private Set<String> organisaatioOids;
    private String anojaOid;
    private Set<String> henkiloOidRestrictionList;

    public Predicate condition(QAnomus qAnomus) {
        BooleanBuilder builder = new BooleanBuilder();

        if (q != null) {
            builder.andAnyOf(
                    qAnomus.henkilo.oidHenkilo.eq(q),
                    qAnomus.henkilo.etunimetCached.containsIgnoreCase(q),
                    qAnomus.henkilo.sukunimiCached.containsIgnoreCase(q),
                    qAnomus.henkilo.kayttajatiedot.username.containsIgnoreCase(q)
            );
        }
        if (anottuAlku != null || anottuLoppu != null) {
            builder.and(qAnomus.anottuPvm.between(anottuAlku, anottuLoppu));
        }
        if (tilat != null) {
            builder.and(qAnomus.anomuksenTila.in(tilat));
        }
        if (organisaatioOids != null) {
            builder.and(qAnomus.organisaatioOid.in(organisaatioOids));
        }
        if (anojaOid != null) {
            builder.and(qAnomus.henkilo.oidHenkilo.eq(anojaOid));
        }
        if (this.henkiloOidRestrictionList != null) {
            builder.and(qAnomus.henkilo.oidHenkilo.notIn(this.henkiloOidRestrictionList));
        }

        return builder;
    }

}
