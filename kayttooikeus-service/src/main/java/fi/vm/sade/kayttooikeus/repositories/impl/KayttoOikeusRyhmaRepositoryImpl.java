package fi.vm.sade.kayttooikeus.repositories.impl;

import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRyhmaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import static fi.vm.sade.kayttooikeus.model.QKayttoOikeusRyhma.kayttoOikeusRyhma;

@Repository
public class KayttoOikeusRyhmaRepositoryImpl extends AbstractRepository implements KayttoOikeusRyhmaRepository {

    @Override
    public List<KayttoOikeusRyhma> listAll() {
        return jpa().from(kayttoOikeusRyhma)
                .leftJoin(kayttoOikeusRyhma.description).fetchJoin()
                .where(kayttoOikeusRyhma.hidden.eq(false))
                .orderBy(kayttoOikeusRyhma.id.asc())
                .select(kayttoOikeusRyhma).fetch();
    }

    @Override
    public List<KayttoOikeusRyhma> findByIdList(List<Long> idList) {
        return jpa().from(kayttoOikeusRyhma)
                .leftJoin(kayttoOikeusRyhma.description).fetchJoin()
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
