package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.TextGroupDto;
import fi.vm.sade.kayttooikeus.repositories.dto.ExpiringKayttoOikeusDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static java.util.Arrays.asList;
import static org.joda.time.LocalDate.now;
import static org.joda.time.Period.weeks;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
public class TaskExecutorServiceTest extends AbstractServiceTest {
    @Autowired
    private TaskExecutorService taskExecutorService;
    
    @MockBean
    private KayttoOikeusService kayttoOikeusService;
    
    @MockBean
    private EmailService emailService;

    @Test
    public void sendExpirationRemindersTest() {
        given(kayttoOikeusService.findToBeExpiringMyonnettyKayttoOikeus(now(),
                    weeks(3), weeks(2))).willReturn(asList(
            ExpiringKayttoOikeusDto.builder()
                .henkiloOid("1.2.3.4.5")
                .myonnettyTapahtumaId(1L)
                .voimassaLoppuPvm(now().plus(weeks(3)))
                .ryhmaDescription(new TextGroupDto())
                .ryhmaName("RYHMA")
            .build(),
            ExpiringKayttoOikeusDto.builder()
                .henkiloOid("1.2.3.4.5")
                .myonnettyTapahtumaId(1L)
                .voimassaLoppuPvm(now().plus(weeks(3)))
                .ryhmaDescription(new TextGroupDto())
                .ryhmaName("RYHMA2")
            .build()
        ));
        
        int numberSent = taskExecutorService.sendExpirationReminders(weeks(3), weeks(2));
        assertEquals(1, numberSent);
    }
}