package fi.vm.sade.kayttooikeus.repositories.impl;

import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRyhmaRepository;
import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import java.util.List;

import static fi.vm.sade.kayttooikeus.model.QKayttoOikeusRyhma.kayttoOikeusRyhma;

@Repository
public class KayttoOikeusRyhmaRepositoryImpl extends AbstractRepository implements KayttoOikeusRyhmaRepository {

    @Override
    public List<KayttoOikeusRyhma> listAll() {
        return from(kayttoOikeusRyhma)
                .where(kayttoOikeusRyhma.hidden.eq(false))
                .orderBy(kayttoOikeusRyhma.id.asc())
                .distinct()
                .select(kayttoOikeusRyhma).fetch();
    }

    @Override
    public List<KayttoOikeusRyhma> findByIdList(List<Long> idList) {
        return from(kayttoOikeusRyhma)
                .where(kayttoOikeusRyhma.hidden.eq(false))
                .where(kayttoOikeusRyhma.id.in(idList))
                .distinct()
                .select(kayttoOikeusRyhma).fetch();
    }
}
