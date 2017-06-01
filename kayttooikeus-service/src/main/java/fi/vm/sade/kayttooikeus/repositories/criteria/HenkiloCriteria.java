package fi.vm.sade.kayttooikeus.repositories.criteria;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import fi.vm.sade.kayttooikeus.model.QHenkilo;
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
public class HenkiloCriteria implements KayttooikeusCriteria<QHenkilo> {

    private Set<String> henkiloOids;
    private String hetu;
    private Boolean passivoitu;
    private Boolean duplikaatti;
    private String nameQuery;

    public Predicate condition(QHenkilo henkilo) {
        BooleanBuilder builder = new BooleanBuilder();
        if (!CollectionUtils.isEmpty(henkiloOids)) {
            builder.and(henkilo.oidHenkilo.in(henkiloOids));
        }
        if(StringUtils.hasLength(this.nameQuery)) {
            Arrays.stream(this.nameQuery.split(" ")).forEach(queryPart ->
                    builder.and(Expressions.anyOf(
                            henkilo.etunimetCached.containsIgnoreCase(queryPart),
                            henkilo.sukunimiCached.containsIgnoreCase(queryPart)
            )));
        }
        return builder;
    }
}
