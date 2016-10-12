package fi.vm.sade.kayttooikeus.repositories.impl;

import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRyhmaDao;
import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import org.springframework.stereotype.Repository;

import java.util.List;

import static fi.vm.sade.kayttooikeus.model.QKayttoOikeusRyhma.kayttoOikeusRyhma;

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
                .select(kayttoOikeusRyhma).fetch();
    }
}
