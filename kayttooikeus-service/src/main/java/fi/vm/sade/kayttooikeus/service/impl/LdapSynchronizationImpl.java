package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.model.LdapUpdateData;
import fi.vm.sade.kayttooikeus.repositories.LdapUpdaterRepository;
import fi.vm.sade.kayttooikeus.service.LdapSynchronization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
public class LdapSynchronizationImpl implements LdapSynchronization {

    // These status values are used to identify problematic users in the queue
    private static final int STATUS_IN_QUEUE = 0;
    private static final int STATUS_RETRY = 1;
    private static final int STATUS_FAILED = 2;

    private LdapUpdaterRepository ldapUpdaterRepository;

    @Autowired
    public LdapSynchronizationImpl(LdapUpdaterRepository ldapUpdaterRepository) {
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
            newData.setModified(ZonedDateTime.now());
            ldapUpdaterRepository.save(newData);
        }
    }

    @Override
    @Transactional
    public void updateHenkilo(String henkiloOid, int priority) {
        LdapUpdateData existingData = ldapUpdaterRepository.findByHenkiloOid(henkiloOid);
        // If there's existing data in the database and its priority is not ASAP or
        // if existing data update has failed, then its value can be updated,
        // but if the existing data has been set to ASAP priority, then that must be
        // kept as ASAP so no down-grading can be done!
        if (existingData != null && !existingData.getHenkiloOid().equals(RUN_ALL_BATCH)
                && (existingData.getPriority() != ASAP_PRIORITY || existingData.getStatus() == STATUS_FAILED)
                && priority != BATCH_PRIORITY) {
            existingData.setStatus(STATUS_IN_QUEUE);
            existingData.setPriority(priority);
            existingData.setModified(ZonedDateTime.now());
        }
        // If the update has failed for some reason, it can be re-queued since
        // the failure could have been caused by a simple timeout or other
        // problem in the LDAP connection, so the status must be able to reset
        else if (existingData == null) {
            LdapUpdateData newData = new LdapUpdateData();
            newData.setHenkiloOid(henkiloOid);
            if (henkiloOid.equals(RUN_ALL_BATCH)) {
                newData.setPriority(NIGHT_PRIORITY);
            } else {
                newData.setPriority(priority);
            }
            newData.setStatus(STATUS_IN_QUEUE);
            newData.setModified(ZonedDateTime.now());
            ldapUpdaterRepository.save(newData);
        }
    }
}
