package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.LdapUpdateData;
import java.util.List;

public interface LdapUpdateDataRepositoryCustom {

    List<LdapUpdateData> findBy(LdapUpdateDataCriteria criteria, Long limit);

}
