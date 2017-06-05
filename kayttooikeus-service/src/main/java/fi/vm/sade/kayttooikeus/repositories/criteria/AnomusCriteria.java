package fi.vm.sade.kayttooikeus.repositories.criteria;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import fi.vm.sade.kayttooikeus.model.AnomuksenTila;
import fi.vm.sade.kayttooikeus.model.QAnomus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
@ToString
public class AnomusCriteria implements KayttooikeusCriteria<QAnomus> {

    private ZonedDateTime anottuAlku;
    private ZonedDateTime anottuLoppu;
    private AnomuksenTila tila;

    public Predicate condition(QAnomus qAnomus) {
        BooleanBuilder builder = new BooleanBuilder();

        if (anottuAlku != null || anottuLoppu != null) {
            builder.and(qAnomus.anottuPvm.between(anottuAlku, anottuLoppu));
        }
        if (tila != null) {
            builder.and(qAnomus.anomuksenTila.eq(tila));
        }

        return builder;
    }

}
