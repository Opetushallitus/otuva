package fi.vm.sade.kayttooikeus.repositories.impl;

import fi.vm.sade.kayttooikeus.repositories.DbTestRepository;
import org.springframework.stereotype.Repository;

import static fi.vm.sade.kayttooikeus.model.QHenkilo.henkilo;

@Repository
public class DbTestRepositoryImpl extends AbstractRepository implements DbTestRepository {
    
    @Override
    public Long countHenkilos() {
        return from(henkilo).where(henkilo.passivoitu.eq(false)).fetchCount();
    }
}
