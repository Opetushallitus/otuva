package fi.vm.sade.kayttooikeus.config.scheduling;

import fi.vm.sade.kayttooikeus.config.properties.KayttooikeusProperties;
import fi.vm.sade.kayttooikeus.repositories.KayttajatiedotRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
public class IdentificationCleanupTaskTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private KayttooikeusProperties kayttooikeusProperties;

    @Mock
    private KayttajatiedotRepository kayttajatiedotRepository;

    @InjectMocks
    private IdentificationCleanupTask identificationCleanupTask;

    @Test
    public void execute() {
        identificationCleanupTask.execute(null, null);
        verify(kayttajatiedotRepository, times(1)).cleanObsoletedIdentifications();
    }
}
