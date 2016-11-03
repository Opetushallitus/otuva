package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.model.LdapUpdateData;
import fi.vm.sade.kayttooikeus.repositories.LdapUpdaterRepository;
import fi.vm.sade.kayttooikeus.service.LdapSynchronization;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LdapSynchronizationImpl implements LdapSynchronization {
    /* These priority levels define different behavior for LDAP update process
     * since night time batch updates are run in a different way than daytime
     * batch updates
     */
    int REALTIME_PRIORITY = -1;
    int BATCH_PRIORITY = 0;
    int ASAP_PRIORITY = 1;
    int NORMAL_PRIORITY = 2;
    int NIGHT_PRIORITY = 3;

    // These status values are used to identify problematic users in the queue
    int STATUS_IN_QUEUE = 0;
    int STATUS_RETRY = 1;
    int STATUS_FAILED = 2;

    private LdapUpdaterRepository ldapUpdaterRepository;

    @Autowired
    public LdapSynchronizationImpl(LdapUpdaterRepository ldapUpdaterRepository){
        this.ldapUpdaterRepository = ldapUpdaterRepository;
    }

    @Override
    @Transactional
    public void updateAccessRightGroup(Long id) {
        if (ldapUpdaterRepository.numberOfUpdatesForKor(id).compareTo(0L) == 0) {
            LdapUpdateData newData = new LdapUpdateData();
            newData.setKorId(id);
            newData.setPriority(BATCH_PRIORITY);
            newData.setStatus(STATUS_IN_QUEUE);
            newData.setModified(DateTime.now());
            ldapUpdaterRepository.save(newData);
        }
    }
}
