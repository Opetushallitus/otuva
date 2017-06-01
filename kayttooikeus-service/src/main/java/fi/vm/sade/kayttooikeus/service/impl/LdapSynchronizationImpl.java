package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.service.impl.ldap.LdapSynchronizer;
import fi.vm.sade.kayttooikeus.config.properties.LdapSynchronizationProperties;
import fi.vm.sade.kayttooikeus.model.LdapPriorityType;
import fi.vm.sade.kayttooikeus.model.LdapStatusType;
import fi.vm.sade.kayttooikeus.model.LdapSynchronizationData;
import fi.vm.sade.kayttooikeus.model.LdapUpdateData;
import fi.vm.sade.kayttooikeus.repositories.LdapSynchronizationDataRepository;
import fi.vm.sade.kayttooikeus.service.LdapSynchronization;
import fi.vm.sade.kayttooikeus.service.TimeService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import fi.vm.sade.kayttooikeus.repositories.LdapUpdateDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class LdapSynchronizationImpl implements LdapSynchronization {

    private static final Logger LOGGER = LoggerFactory.getLogger(LdapSynchronizationImpl.class);

    private final LdapUpdateDataRepository ldapUpdateDataRepository;
    private final TimeService timeService;
    private final LdapSynchronizer ldapSynchronizer;
    private final LdapSynchronizationDataRepository ldapSynchronizationDataRepository;
    private final LdapSynchronizationProperties ldapSynchronizationProperties;

    @Override
    @Transactional
    public void updateAllAtNight() {
        LdapUpdateData existingData = ldapUpdateDataRepository.findByHenkiloOid(LdapSynchronizer.RUN_ALL_BATCH);
        if (existingData == null) {
            LdapUpdateData newData = new LdapUpdateData();
            newData.setHenkiloOid(LdapSynchronizer.RUN_ALL_BATCH);
            newData.setPriority(LdapPriorityType.NIGHT);
            newData.setStatus(LdapStatusType.IN_QUEUE);
            newData.setModified(timeService.getDateTimeNow());
            ldapUpdateDataRepository.save(newData);
        }
    }

    @Override
    @Transactional
    public void updateKayttoOikeusRyhma(Long kayttoOikeusRyhmaId) {
        if (ldapUpdateDataRepository.countByKayttoOikeusRyhmaId(kayttoOikeusRyhmaId).compareTo(0L) == 0) {
            LdapUpdateData newData = new LdapUpdateData();
            newData.setKayttoOikeusRyhmaId(kayttoOikeusRyhmaId);
            newData.setPriority(LdapPriorityType.BATCH);
            newData.setStatus(LdapStatusType.IN_QUEUE);
            newData.setModified(timeService.getDateTimeNow());
            ldapUpdateDataRepository.save(newData);
        }
    }

    @Override
    @Transactional
    public void updateHenkilo(String henkiloOid) {
        updateHenkilo(henkiloOid, LdapPriorityType.NORMAL);
    }

    @Override
    @Transactional
    public void updateHenkiloAsap(String henkiloOid) {
        updateHenkilo(henkiloOid, LdapPriorityType.ASAP);
    }

    private void updateHenkilo(String henkiloOid, LdapPriorityType priority) {
        if (LdapSynchronizer.RUN_ALL_BATCH.equals(henkiloOid)) {
            updateAllAtNight();
            return;
        }
        LdapUpdateData existingData = ldapUpdateDataRepository.findByHenkiloOid(henkiloOid);
        if (existingData != null) {
            if (existingData.getPriority() != LdapPriorityType.ASAP) {
                existingData.setPriority(priority);
                existingData.setModified(timeService.getDateTimeNow());
            }
            if (existingData.getStatus() == LdapStatusType.FAILED) {
                existingData.setStatus(LdapStatusType.IN_QUEUE);
                existingData.setModified(timeService.getDateTimeNow());
            }
        } else {
            LdapUpdateData newData = new LdapUpdateData();
            newData.setHenkiloOid(henkiloOid);
            newData.setPriority(priority);
            newData.setStatus(LdapStatusType.IN_QUEUE);
            newData.setModified(timeService.getDateTimeNow());
            ldapUpdateDataRepository.save(newData);
        }
    }

    @Override
    @Transactional
    public void updateHenkiloNow(String henkiloOid) {
        ldapSynchronizer.run(henkiloOid);
    }

    @Override
    @Transactional
    public synchronized void runSynchronizer() {
        LOGGER.info("LDAP-synkronointi aloitetaan");
        long start = timeService.getCurrentTimeMillis();

        DateTime now = timeService.getDateTimeNow();
        boolean nightTime = ldapSynchronizationProperties.isNightTime(now.getHourOfDay());
        LdapSynchronizationProperties.Timed properties = ldapSynchronizationProperties.getTimedProperties(nightTime);
        DateTime dateTime = now.minusMinutes(properties.getIntervalInMinutes());

        Optional<LdapSynchronizationData> previous = ldapSynchronizationDataRepository.findFirstByOrderByIdDesc();
        if (previous.map(t -> t.getLastRun().isBefore(dateTime)).orElse(true)) {
            Optional<LdapSynchronizationData> next = ldapSynchronizer.run(previous, nightTime,
                    properties.getBatchSize(), properties.getLoadThresholdInSeconds());
            next.ifPresent(this::saveStatistics);
        }

        LOGGER.info("LDAP-synkronointi päättyy, kesto: {}ms", timeService.getCurrentTimeMillis() - start);
    }

    private void saveStatistics(LdapSynchronizationData next) {
        List<LdapSynchronizationData> datas = ldapSynchronizationDataRepository.findByOrderByIdAsc();
        if (datas.size() > ldapSynchronizationProperties.getStatisticsSize()) {
            LdapSynchronizationData oldest = datas.get(0);
            ldapSynchronizationDataRepository.delete(oldest);
        }
        ldapSynchronizationDataRepository.save(next);
    }

}
