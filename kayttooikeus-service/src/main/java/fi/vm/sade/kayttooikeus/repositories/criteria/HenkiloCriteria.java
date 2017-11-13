package fi.vm.sade.kayttooikeus.repositories.criteria;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.QHenkilo;
import fi.vm.sade.kayttooikeus.model.QMyonnettyKayttoOikeusRyhmaTapahtuma;
import fi.vm.sade.kayttooikeus.model.QOrganisaatioHenkilo;
import lombok.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Set;

/**
 * Henkilöiden hakemiseen oppijanumerorekisteristä henkilön perustietojen
 * perusteella.
 */
@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class HenkiloCriteria {
    // Henkilo
    private String hetu;
    private Boolean passivoitu;
    private Boolean duplikaatti;
    private String nameQuery;

    // Organisaatiohenkilo
    private Boolean noOrganisation;
    private Set<String> organisaatioOids;
    private Long kayttooikeusryhmaId;

    public Predicate condition(QHenkilo henkilo,
                                QOrganisaatioHenkilo organisaatioHenkilo,
                                QMyonnettyKayttoOikeusRyhmaTapahtuma myonnettyKayttoOikeusRyhmaTapahtuma) {
        BooleanBuilder builder = new BooleanBuilder();
        // Henkilo
        if (this.passivoitu != null && !this.passivoitu) {
            builder.and(henkilo.passivoituCached.eq(false));
        }
        if (this.duplikaatti != null && !this.duplikaatti) {
            builder.and(henkilo.duplicateCached.eq(false));
        }
        if (StringUtils.hasLength(this.nameQuery)) {
            BooleanBuilder predicate = new BooleanBuilder();
            Arrays.stream(this.nameQuery.toLowerCase().split(" ")).forEach(queryPart ->
                    predicate.or(Expressions.anyOf(
                            // By using contains query can't use B-tree index
                            henkilo.etunimetCached.startsWithIgnoreCase(queryPart),
                            henkilo.sukunimiCached.startsWithIgnoreCase(queryPart)
                    )));
            predicate.or(henkilo.oidHenkilo.eq(this.nameQuery));
            builder.and(predicate);
        }
        // Organisaatiohenkilo
        if (this.noOrganisation != null && !this.noOrganisation) {
            QOrganisaatioHenkilo subOrganisaatioHenkilo = new QOrganisaatioHenkilo("subOrganisaatioHenkilo");
            JPQLQuery<Henkilo> subquery = JPAExpressions.select(subOrganisaatioHenkilo.henkilo)
                    .from(subOrganisaatioHenkilo)
                    .where(subOrganisaatioHenkilo.passivoitu.isFalse()
                            .and(subOrganisaatioHenkilo.henkilo.eq(henkilo)));
            builder.and(subquery.exists());
        }
        if (!CollectionUtils.isEmpty(this.organisaatioOids)) {
            builder.and(organisaatioHenkilo.organisaatioOid.in(this.organisaatioOids));
            builder.and(organisaatioHenkilo.passivoitu.isFalse());
        }
        // Kayttooikeus
        if (this.kayttooikeusryhmaId != null) {
            builder.and(myonnettyKayttoOikeusRyhmaTapahtuma.kayttoOikeusRyhma.id.eq(this.kayttooikeusryhmaId));
        }

        return builder;
    }
}
