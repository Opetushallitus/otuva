package fi.vm.sade.kayttooikeus.repositories.criteria;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import fi.vm.sade.kayttooikeus.dto.KayttoOikeudenTila;
import fi.vm.sade.kayttooikeus.enumeration.AdminGrantsGroups;
import fi.vm.sade.kayttooikeus.model.AnomuksenTila;
import fi.vm.sade.kayttooikeus.model.QAnomus;
import fi.vm.sade.kayttooikeus.model.QHaettuKayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.model.QKayttoOikeusRyhma;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AnomusCriteria {

    private String q;
    private LocalDateTime anottuAlku;
    private LocalDateTime anottuLoppu;
    private Set<AnomuksenTila> anomuksenTilat;
    private Boolean onlyActive;
    private Set<String> organisaatioOids;
    private String anojaOid;
    private Set<String> henkiloOidRestrictionList;
    private Boolean adminView;

    final static List<String> ophConditions = AdminGrantsGroups.allValuesAsList();

    public Predicate condition(QAnomus qAnomus) {
        BooleanBuilder builder = new BooleanBuilder();
        return this.condition(qAnomus, builder);
    }

    public Predicate condition(QAnomus qAnomus, QKayttoOikeusRyhma qKayttoOikeusRyhma, QHaettuKayttoOikeusRyhma qHaettuKayttoOikeusRyhma) {
        BooleanBuilder builder = new BooleanBuilder();

        builder = this.condition(qAnomus, builder);

        if (this.adminView != null && this.adminView) {
            builder.andAnyOf(ophConditions.stream().map(condition -> qKayttoOikeusRyhma.name.contains(condition)
                    .and(qKayttoOikeusRyhma.hidden.isFalse())).toArray(BooleanExpression[]::new));
        }

        if (this.onlyActive != null) {
            builder.and(qHaettuKayttoOikeusRyhma.tyyppi.eq(KayttoOikeudenTila.ANOTTU)
                    .or(qHaettuKayttoOikeusRyhma.tyyppi.isNull()));
        }

        return builder;
    }

    private BooleanBuilder condition(QAnomus qAnomus, BooleanBuilder builder) {
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
        if (anomuksenTilat != null) {
            builder.and(qAnomus.anomuksenTila.in(anomuksenTilat));
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
