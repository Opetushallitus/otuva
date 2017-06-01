package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.service.impl.ldap.LdapSynchronizer;
import fi.vm.sade.kayttooikeus.config.properties.LdapSynchronizationProperties;
import fi.vm.sade.kayttooikeus.model.LdapSynchronizationData;
import fi.vm.sade.kayttooikeus.repositories.LdapSynchronizationDataRepository;
import fi.vm.sade.kayttooikeus.service.TimeService;
import java.util.Optional;
import org.joda.time.DateTime;
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
public class LdapSynchronizationImplTest {

    private LdapSynchronizationImpl ldapSynchronizationImpl;

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
        ldapSynchronizationImpl = new LdapSynchronizationImpl(
                ldapUpdateDataRepositoryMock,
                timeService,
                ldapSynchronizerMock,
                ldapSynchronizationDataRepositoryMock,
                ldapSynchronizationProperties
        );
    }

    @Test
    public void runSynchronizerShouldExecuteFirstTime() {
        DateTime now = DateTime.parse("2017-05-26T08:04:06");
        when(timeService.getDateTimeNow()).thenReturn(now);
        when(ldapSynchronizationDataRepositoryMock.findFirstByOrderByIdDesc()).thenReturn(Optional.empty());
        when(ldapSynchronizerMock.run(any(), anyBoolean(), anyLong(), anyLong())).thenReturn(Optional.empty());

        ldapSynchronizationImpl.runSynchronizer();

        verify(ldapSynchronizerMock).run(eq(Optional.empty()), anyBoolean(), anyLong(), anyLong());
        verify(ldapSynchronizationDataRepositoryMock).findFirstByOrderByIdDesc();
        verifyNoMoreInteractions(ldapSynchronizationDataRepositoryMock);
    }

    @Test
    public void runSynchronizerShouldExecute() {
        DateTime lastRun = DateTime.parse("2017-05-26T08:04:06");
        DateTime now = DateTime.parse("2017-05-26T08:05:07");
        when(timeService.getDateTimeNow()).thenReturn(now);
        when(ldapSynchronizationDataRepositoryMock.findFirstByOrderByIdDesc())
                .thenReturn(Optional.of(LdapSynchronizationData.builder().lastRun(lastRun).build()));
        when(ldapSynchronizerMock.run(any(), anyBoolean(), anyLong(), anyLong())).thenReturn(Optional.empty());

        ldapSynchronizationImpl.runSynchronizer();

        verify(ldapSynchronizerMock).run(any(), anyBoolean(), anyLong(), anyLong());
        verify(ldapSynchronizationDataRepositoryMock).findFirstByOrderByIdDesc();
        verifyNoMoreInteractions(ldapSynchronizationDataRepositoryMock);
    }

    @Test
    public void runSynchronizerShouldSkip() {
        DateTime lastRun = DateTime.parse("2017-05-26T08:04:06");
        DateTime now = DateTime.parse("2017-05-26T08:05:06");
        when(timeService.getDateTimeNow()).thenReturn(now);
        when(ldapSynchronizationDataRepositoryMock.findFirstByOrderByIdDesc())
                .thenReturn(Optional.of(LdapSynchronizationData.builder().lastRun(lastRun).build()));
        when(ldapSynchronizerMock.run(any(), anyBoolean(), anyLong(), anyLong())).thenReturn(Optional.empty());

        ldapSynchronizationImpl.runSynchronizer();

        verifyZeroInteractions(ldapSynchronizerMock);
        verify(ldapSynchronizationDataRepositoryMock).findFirstByOrderByIdDesc();
        verifyNoMoreInteractions(ldapSynchronizationDataRepositoryMock);
    }

}
