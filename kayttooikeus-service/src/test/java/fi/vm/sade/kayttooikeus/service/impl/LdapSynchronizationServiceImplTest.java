package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.service.impl.ldap.LdapSynchronizer;
import fi.vm.sade.kayttooikeus.config.properties.LdapSynchronizationProperties;
import fi.vm.sade.kayttooikeus.model.LdapSynchronizationData;
import fi.vm.sade.kayttooikeus.repositories.LdapSynchronizationDataRepository;
import fi.vm.sade.kayttooikeus.service.TimeService;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import org.mockito.runners.MockitoJUnitRunner;
import fi.vm.sade.kayttooikeus.repositories.LdapUpdateDataRepository;
import static org.mockito.Matchers.anyLong;

@RunWith(MockitoJUnitRunner.class)
public class LdapSynchronizationServiceImplTest {

    private LdapSynchronizationServiceImpl ldapSynchronizationServiceImpl;

    @Mock
    private LdapUpdateDataRepository ldapUpdateDataRepositoryMock;
    @Mock
    private TimeService timeService;
    @Mock
    private LdapSynchronizer ldapSynchronizerMock;
    @Mock
    private LdapSynchronizationDataRepository ldapSynchronizationDataRepositoryMock;

    @Before
    public void setup() {
        LdapSynchronizationProperties ldapSynchronizationProperties = new LdapSynchronizationProperties();
        ldapSynchronizationServiceImpl = new LdapSynchronizationServiceImpl(
                ldapUpdateDataRepositoryMock,
                timeService,
                ldapSynchronizerMock,
                ldapSynchronizationDataRepositoryMock,
                ldapSynchronizationProperties
        );
    }

    @Test
    public void runSynchronizerShouldExecuteFirstTime() {
        LocalDateTime now = LocalDateTime.parse("2017-05-26T08:04:06");
        when(timeService.getDateTimeNow()).thenReturn(now);
        when(ldapSynchronizationDataRepositoryMock.findFirstByOrderByIdDesc()).thenReturn(Optional.empty());
        when(ldapSynchronizerMock.run(any(), anyBoolean(), anyLong(), anyLong())).thenReturn(Optional.empty());

        ldapSynchronizationServiceImpl.runSynchronizer();

        verify(ldapSynchronizerMock).run(eq(Optional.empty()), anyBoolean(), anyLong(), anyLong());
        verify(ldapSynchronizationDataRepositoryMock).findFirstByOrderByIdDesc();
        verifyNoMoreInteractions(ldapSynchronizationDataRepositoryMock);
    }

    @Test
    public void runSynchronizerShouldExecute() {
        LocalDateTime lastRun = LocalDateTime.parse("2017-05-26T08:04:06");
        LocalDateTime now = LocalDateTime.parse("2017-05-26T08:05:07");
        when(timeService.getDateTimeNow()).thenReturn(now);
        when(ldapSynchronizationDataRepositoryMock.findFirstByOrderByIdDesc())
                .thenReturn(Optional.of(LdapSynchronizationData.builder().lastRun(lastRun).build()));
        when(ldapSynchronizerMock.run(any(), anyBoolean(), anyLong(), anyLong())).thenReturn(Optional.empty());

        ldapSynchronizationServiceImpl.runSynchronizer();

        verify(ldapSynchronizerMock).run(any(), anyBoolean(), anyLong(), anyLong());
        verify(ldapSynchronizationDataRepositoryMock).findFirstByOrderByIdDesc();
        verifyNoMoreInteractions(ldapSynchronizationDataRepositoryMock);
    }

    @Test
    public void runSynchronizerShouldSkip() {
        LocalDateTime lastRun = LocalDateTime.parse("2017-05-26T08:04:06");
        LocalDateTime now = LocalDateTime.parse("2017-05-26T08:05:06");
        when(timeService.getDateTimeNow()).thenReturn(now);
        when(ldapSynchronizationDataRepositoryMock.findFirstByOrderByIdDesc())
                .thenReturn(Optional.of(LdapSynchronizationData.builder().lastRun(lastRun).build()));

        ldapSynchronizationServiceImpl.runSynchronizer();

        verifyZeroInteractions(ldapSynchronizerMock);
        verify(ldapSynchronizationDataRepositoryMock).findFirstByOrderByIdDesc();
        verifyNoMoreInteractions(ldapSynchronizationDataRepositoryMock);
    }

}
