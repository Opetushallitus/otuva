package fi.vm.sade.kayttooikeus.repositories.impl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import fi.vm.sade.kayttooikeus.model.Kutsu;
import fi.vm.sade.kayttooikeus.model.QKayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.model.QKutsu;
import fi.vm.sade.kayttooikeus.model.QKutsuOrganisaatio;
import fi.vm.sade.kayttooikeus.repositories.KutsuRepositoryCustom;
import fi.vm.sade.kayttooikeus.repositories.criteria.KutsuCriteria;
import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public class KutsuRepositoryImpl implements KutsuRepositoryCustom {

    private final PermissionCheckerService permissionCheckerService;

    private final EntityManager em;

    @Autowired
    public KutsuRepositoryImpl(JpaContext context, PermissionCheckerService permissionCheckerService) {
        this.em = context.getEntityManagerByManagedType(Kutsu.class);
        this.permissionCheckerService = permissionCheckerService;
    }

    @Override
    public List<Kutsu> listKutsuListDtos(KutsuCriteria criteria, List<OrderSpecifier> orderSpecifier, Long offset, Long amount) {
        QKutsu kutsu = QKutsu.kutsu;
        QKutsuOrganisaatio kutsuOrganisaatio = QKutsuOrganisaatio.kutsuOrganisaatio;
        QKayttoOikeusRyhma kayttoOikeusRyhma = QKayttoOikeusRyhma.kayttoOikeusRyhma;
        JPAQuery<Kutsu> query = new JPAQueryFactory(this.em)
                .from(kutsuOrganisaatio)
                .rightJoin(kutsuOrganisaatio.kutsu, kutsu)
                .leftJoin(kutsuOrganisaatio.ryhmat, kayttoOikeusRyhma)
                .select(kutsu)
                .where(criteria.onCondition(this.permissionCheckerService.getCurrentUserOid()));
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
