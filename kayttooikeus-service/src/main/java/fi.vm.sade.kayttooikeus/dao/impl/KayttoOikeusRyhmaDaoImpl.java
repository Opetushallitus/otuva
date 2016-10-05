package fi.vm.sade.kayttooikeus.dao.impl;

import com.mysema.query.types.OrderSpecifier;
import fi.vm.sade.kayttooikeus.dao.KayttoOikeusRyhmaDao;
import fi.vm.sade.kayttooikeus.domain.KayttoOikeusRyhma;
import org.springframework.stereotype.Repository;

import java.util.List;

import static fi.vm.sade.kayttooikeus.domain.QKayttoOikeusRyhma.kayttoOikeusRyhma;

/**
 * Created by autio on 4.10.2016.
 */
@Repository
public class KayttoOikeusRyhmaDaoImpl extends AbstractDao  implements KayttoOikeusRyhmaDao {

    @Override
    public List<KayttoOikeusRyhma> listAll() {
        return from(kayttoOikeusRyhma)
                .where(kayttoOikeusRyhma.hidden.eq(false))
                .orderBy(kayttoOikeusRyhma.id.asc())
                .distinct()
                .list(kayttoOikeusRyhma);
    }
}
