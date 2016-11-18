package fi.vm.sade.kayttooikeus.repositories.impl;

import com.google.common.collect.Sets;
import fi.vm.sade.kayttooikeus.model.QHenkiloViite;
import fi.vm.sade.kayttooikeus.repositories.HenkiloViiteRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public class HenkiloVitteRepositoryImpl extends AbstractRepository implements HenkiloViiteRepository {

    @Override
    public Set<String> getAllOidsForSamePerson(String oid) {
        QHenkiloViite qHenkiloViite = QHenkiloViite.henkiloViite;
        String masterOid = this.jpa().select(qHenkiloViite.masterOid).from(qHenkiloViite)
                .where(qHenkiloViite.masterOid.eq(oid)
                .or(qHenkiloViite.slaveOid.eq(oid)))
                .fetchFirst();

        if(masterOid == null) {
            return Sets.newHashSet(oid);
        }
        Set<String> personOidsForSamePerson = Sets.newHashSet(masterOid);

        List<String> masterOidSlaves = this.jpa().select(qHenkiloViite.slaveOid).from(qHenkiloViite)
                .where(qHenkiloViite.masterOid.eq(masterOid))
                .fetch();

        return Stream.concat(personOidsForSamePerson.stream(), masterOidSlaves.stream()).collect(Collectors.toSet());
    }
}
