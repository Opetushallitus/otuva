package fi.vm.sade.kayttooikeus.repositories.impl;

import com.querydsl.core.types.Projections;
import fi.vm.sade.kayttooikeus.model.QOrganisaatioHenkilo;
import fi.vm.sade.kayttooikeus.model.Text;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRyhmaRepository;
import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.service.dto.KayttoOikeusRyhmaDto;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Set;

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
                .where(kayttoOikeusRyhma.hidden.eq(false).and(
                        kayttoOikeusRyhma.id.in(idList)))
                .distinct()
                .select(kayttoOikeusRyhma).fetch();
    }

    @Override
    public KayttoOikeusRyhma findById(Long id) {
        return from(kayttoOikeusRyhma)
                .where(kayttoOikeusRyhma.hidden.eq(false)
                        .and(kayttoOikeusRyhma.id.eq(id)))
                .distinct()
                .select(kayttoOikeusRyhma).fetchFirst();

    }

    @Override
    public Boolean ryhmaNameFiExists(String ryhmaNameFi) {
        return exists(jpa().from(kayttoOikeusRyhma)
                .where(kayttoOikeusRyhma.description.texts.any().lang.eq("FI"),
                        kayttoOikeusRyhma.description.texts.any().text.eq(ryhmaNameFi))
                .select(kayttoOikeusRyhma));
    }

    @Override
    public KayttoOikeusRyhma insert(KayttoOikeusRyhma kayttoOikeusRyhma) {
        return persist(kayttoOikeusRyhma);
    }

}
