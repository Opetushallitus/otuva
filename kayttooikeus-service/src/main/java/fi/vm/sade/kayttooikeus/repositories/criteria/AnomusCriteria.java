package fi.vm.sade.kayttooikeus.repositories.criteria;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import fi.vm.sade.kayttooikeus.dto.KayttoOikeudenTila;
import fi.vm.sade.kayttooikeus.enumeration.KayttooikeusRooli;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import lombok.*;
import org.apache.commons.lang.BooleanUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private Set<KayttoOikeudenTila> kayttoOikeudenTilas;
    private Boolean onlyActive;
    private Set<String> organisaatioOids;
    private String anojaOid;
    private Set<String> henkiloOidRestrictionList;
    private Boolean adminView;
    private Set<Long> kayttooikeusRyhmaIds;

    @FunctionalInterface
    public interface AnomusCriteriaFunction<QAnomus, QKayttoOikeus, QHaettuKayttoOikeusRyhma> {
        Predicate apply(QAnomus qAnomus, QKayttoOikeus qKayttoOikeus, QHaettuKayttoOikeusRyhma qHaettuKayttoOikeusRyhma);
    }

    public Function<QAnomus, Predicate> createBasicCondition(OrganisaatioClient organisaatioClient) {
        return (QAnomus qAnomus) -> {
            BooleanBuilder builder = new BooleanBuilder();
            return this.condition(qAnomus, builder, this.getPredicates(organisaatioClient, qAnomus));
        };
    }

    public AnomusCriteriaFunction<QAnomus, QKayttoOikeus, QHaettuKayttoOikeusRyhma> createExtendedCondition(OrganisaatioClient organisaatioClient) {
        return (qAnomus, qKayttoOikeus, qHaettuKayttoOikeusRyhma) -> {
            BooleanBuilder builder = new BooleanBuilder();

            builder = this.condition(qAnomus, builder, this.getPredicates(organisaatioClient, qAnomus));

            if (BooleanUtils.isTrue(this.adminView)) {
                builder.and(qKayttoOikeus.rooli.eq(KayttooikeusRooli.VASTUUKAYTTAJAT.getName()));
            }

            if (BooleanUtils.isTrue(this.onlyActive)) {
                builder.and(qHaettuKayttoOikeusRyhma.tyyppi.eq(KayttoOikeudenTila.ANOTTU)
                        .or(qHaettuKayttoOikeusRyhma.tyyppi.isNull()));
            }
            if(this.kayttoOikeudenTilas != null) {
                // Behaviour from old authentication-service
                if(this.kayttoOikeudenTilas.size() == 1 && this.kayttoOikeudenTilas.iterator().next().equals(KayttoOikeudenTila.ANOTTU)) {
                    builder.and(qHaettuKayttoOikeusRyhma.tyyppi.isNull());
                }
                else {
                    builder.and(qHaettuKayttoOikeusRyhma.tyyppi.in(this.kayttoOikeudenTilas));
                }
            }
            if(this.kayttooikeusRyhmaIds != null) {
                builder.and(qHaettuKayttoOikeusRyhma.kayttoOikeusRyhma.id.in(this.kayttooikeusRyhmaIds));
            }

            return builder;
        };
    }

    @Nullable
    private List<Predicate> getPredicates(OrganisaatioClient organisaatioClient, QAnomus qAnomus) {
        List<Predicate> predicates = null;
        if(!CollectionUtils.isEmpty(this.organisaatioOids)) {
            predicates = this.organisaatioOids.stream()
                    .map(oid -> qAnomus.organisaatioOid.in(organisaatioClient.getActiveParentOids(oid)))
                    .collect(Collectors.toList());
        }
        return predicates;
    }

    private BooleanBuilder condition(QAnomus qAnomus, BooleanBuilder builder, List<Predicate> organisaatioConditions) {
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
            builder.and(ExpressionUtils.anyOf(organisaatioConditions));
        }
        if (StringUtils.hasLength(anojaOid)) {
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
