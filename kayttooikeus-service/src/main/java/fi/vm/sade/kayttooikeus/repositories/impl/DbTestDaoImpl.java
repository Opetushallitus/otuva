package fi.vm.sade.kayttooikeus.repositories.impl;

import fi.vm.sade.kayttooikeus.repositories.DbTestDao;
import org.springframework.stereotype.Repository;

import static fi.vm.sade.kayttooikeus.model.QHenkilo.henkilo;

/**
 * User: tommiratamaa
 * Date: 2.9.2016
 * Time: 16.58
 */
@Repository
public class DbTestDaoImpl extends AbstractDao implements DbTestDao {
    
    @Override
    public Long countHenkilos() {
        return from(henkilo).where(henkilo.passivoitu.eq(false)).count();
    }
}
