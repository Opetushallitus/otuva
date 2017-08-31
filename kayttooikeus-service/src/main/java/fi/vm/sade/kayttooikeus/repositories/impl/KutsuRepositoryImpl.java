package fi.vm.sade.kayttooikeus.repositories.impl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import fi.vm.sade.kayttooikeus.model.Kutsu;
import fi.vm.sade.kayttooikeus.model.QKutsu;
import fi.vm.sade.kayttooikeus.model.QKutsuOrganisaatio;
import fi.vm.sade.kayttooikeus.repositories.KutsuRepository;
import fi.vm.sade.kayttooikeus.repositories.criteria.KutsuCriteria;
import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class KutsuRepositoryImpl extends BaseRepositoryImpl<Kutsu> implements KutsuRepository {
    @Autowired
    private final PermissionCheckerService permissionCheckerService;

    @Override
    public List<Kutsu> listKutsuListDtos(KutsuCriteria criteria, List<OrderSpecifier> orderSpecifier) {
        return this.listKutsuListDtos(criteria, orderSpecifier, null, null);
    }

    @Override
    public List<Kutsu> listKutsuListDtos(KutsuCriteria criteria, List<OrderSpecifier> orderSpecifier, Long offset, Long amount) {
        QKutsu kutsu = QKutsu.kutsu;
        QKutsuOrganisaatio kutsuOrganisaatio = QKutsuOrganisaatio.kutsuOrganisaatio;
        JPAQuery<Kutsu> query = jpa().from(kutsuOrganisaatio)
                .rightJoin(kutsuOrganisaatio.kutsu, kutsu)
                .select(kutsu)
                .where(criteria.onCondition(kutsu, kutsuOrganisaatio, this.permissionCheckerService.getCurrentUserOid()));
        query.orderBy(orderSpecifier.toArray(new OrderSpecifier[orderSpecifier.size()]));
        if(offset != null) {
            query.offset(offset);
        }
        if(amount != null) {
            query.limit(amount);
        }
        return query.distinct().fetch();
    }
}
