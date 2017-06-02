package fi.vm.sade.kayttooikeus.repositories.criteria;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
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
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class HenkiloCriteria {
    // Henkilo
    private Set<String> henkiloOids;
    private String hetu;
    private Boolean passivoitu;
    private Boolean duplikaatti;
    private String nameQuery;

    // Organisaatiohenkilo
    private Boolean noOrganisation;
    private Boolean subOrganisation;
    private String organisaatioOid;
    private Long kayttooikeusryhmaId;

    public Predicate condition(QHenkilo henkilo,
                               QOrganisaatioHenkilo organisaatioHenkilo,
                               QMyonnettyKayttoOikeusRyhmaTapahtuma myonnettyKayttoOikeusRyhmaTapahtuma) {
        BooleanBuilder builder = new BooleanBuilder();
        // Henkilo
        if (!CollectionUtils.isEmpty(henkiloOids)) {
            builder.and(henkilo.oidHenkilo.in(henkiloOids));
        }
        if(this.passivoitu != null) {
            builder.and(henkilo.passivoituCached.eq(this.passivoitu));
        }
        if(this.duplikaatti != null) {
            builder.and(henkilo.duplicateCached.eq(this.duplikaatti));
        }
        if(StringUtils.hasLength(this.nameQuery)) {
            Arrays.stream(this.nameQuery.split(" ")).forEach(queryPart ->
                    builder.and(Expressions.anyOf(
                            henkilo.etunimetCached.containsIgnoreCase(queryPart),
                            henkilo.sukunimiCached.containsIgnoreCase(queryPart)
                    )));
        }
        // Organisaatiohenkilo
        if(this.noOrganisation != null && this.noOrganisation) {
            builder.and(henkilo.organisaatioHenkilos.isEmpty());
        }
        if(this.subOrganisation != null) {
//            builder.and()
        }
        if(StringUtils.hasLength(this.organisaatioOid)) {
            builder.and(organisaatioHenkilo.organisaatioOid.eq(this.organisaatioOid));
        }
        // Kayttooikeus
        if(this.kayttooikeusryhmaId != null) {
            builder.and(myonnettyKayttoOikeusRyhmaTapahtuma.id.eq(this.kayttooikeusryhmaId));
        }

        return builder;
    }
}
