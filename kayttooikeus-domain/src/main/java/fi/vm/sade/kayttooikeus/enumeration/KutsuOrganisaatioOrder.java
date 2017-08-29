package fi.vm.sade.kayttooikeus.enumeration;

import com.google.common.collect.Lists;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fi.vm.sade.kayttooikeus.model.QKutsu.kutsu;

public enum KutsuOrganisaatioOrder {
    NIMI(Lists.newArrayList(kutsu.sukunimi, kutsu.etunimi)),
    SAHKOPOSTI(Lists.newArrayList(kutsu.sahkoposti)),
    AIKALEIMA(Lists.newArrayList(kutsu.aikaleima)),
    ;

    private List<ComparableExpressionBase> orders;

    KutsuOrganisaatioOrder(List<ComparableExpressionBase> orders) {
        this.orders = orders;
    }

    public List<OrderSpecifier> getSortWithDirection() {
        return this.getSortWithDirection(Sort.DEFAULT_DIRECTION);
    }

    public List<OrderSpecifier> getSortWithDirection(Sort.Direction direction) {
        Function<ComparableExpressionBase, OrderSpecifier> directionFunc = direction.isAscending()
                ? ComparableExpressionBase::asc
                : ComparableExpressionBase::desc;
        return this.orders.stream().map(directionFunc::apply).collect(Collectors.toList());
    }
}
