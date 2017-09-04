package fi.vm.sade.kayttooikeus.repositories.criteria;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import fi.vm.sade.kayttooikeus.dto.KayttoOikeudenTila;
import fi.vm.sade.kayttooikeus.enumeration.KayttooikeusRooli;
import fi.vm.sade.kayttooikeus.model.*;
import lombok.*;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static fi.vm.sade.kayttooikeus.model.QOrganisaatioHenkilo.organisaatioHenkilo;

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
    private Set<Long> kayttooikeusRyhmaIds;

    public Predicate condition(QAnomus qAnomus) {
        BooleanBuilder builder = new BooleanBuilder();
        return this.condition(qAnomus, builder);
    }

    public Predicate condition(QAnomus qAnomus, QKayttoOikeus qKayttoOikeus, QHaettuKayttoOikeusRyhma qHaettuKayttoOikeusRyhma) {
        BooleanBuilder builder = new BooleanBuilder();

        builder = this.condition(qAnomus, builder);

        if(BooleanUtils.isTrue(this.adminView)) {
            builder.and(qKayttoOikeus.rooli.eq(KayttooikeusRooli.VASTUUKAYTTAJAT.getName()));
        }

        if(BooleanUtils.isTrue(this.onlyActive)) {
            builder.and(qHaettuKayttoOikeusRyhma.tyyppi.eq(KayttoOikeudenTila.ANOTTU)
                    .or(qHaettuKayttoOikeusRyhma.tyyppi.isNull()));
        }

        if(this.kayttooikeusRyhmaIds != null) {
            builder.and(qHaettuKayttoOikeusRyhma.kayttoOikeusRyhma.id.in(this.kayttooikeusRyhmaIds));
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
        if(!CollectionUtils.isEmpty(this.organisaatioOids)) {
            List<Predicate> predicates = this.organisaatioOids.stream()
                    .map(oid -> qAnomus.organisaatioCache.organisaatioOidPath.contains(oid))
                    .collect(Collectors.toList());
            builder.and(ExpressionUtils.anyOf(predicates));
        }
        if (anojaOid != null) {
            builder.and(qAnomus.henkilo.oidHenkilo.eq(anojaOid));
        }
        if (this.henkiloOidRestrictionList != null) {
            builder.and(qAnomus.henkilo.oidHenkilo.notIn(this.henkiloOidRestrictionList));
        }

        return builder;
    }

    public void addHenkiloOidRestriction(String henkiloOid) {
        if (this.henkiloOidRestrictionList == null) {
            this.henkiloOidRestrictionList = new HashSet<>();
        }
        this.henkiloOidRestrictionList.add(henkiloOid);
    }

}
