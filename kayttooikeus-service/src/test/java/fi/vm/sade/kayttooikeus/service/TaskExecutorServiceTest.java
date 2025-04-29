package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.TextGroupDto;
import fi.vm.sade.kayttooikeus.repositories.dto.ExpiringKayttoOikeusDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.Period;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
public class TaskExecutorServiceTest extends AbstractServiceTest {

    @Autowired
    private TaskExecutorService taskExecutorService;

    @MockitoBean
    private KayttoOikeusService kayttoOikeusService;

    @MockitoBean
    private EmailService emailService;

    @Test
    public void sendExpirationRemindersTest() {
        given(kayttoOikeusService.findToBeExpiringMyonnettyKayttoOikeus(LocalDate.now(),
                Period.ofWeeks(3), Period.ofWeeks(2))).willReturn(asList(
                ExpiringKayttoOikeusDto.builder()
                        .henkiloOid("1.2.3.4.5")
                        .myonnettyTapahtumaId(1L)
                        .voimassaLoppuPvm(LocalDate.now().plusWeeks(3))
                        .ryhmaDescription(new TextGroupDto())
                        .ryhmaName("RYHMA")
                        .build(),
                ExpiringKayttoOikeusDto.builder()
                        .henkiloOid("1.2.3.4.5")
                        .myonnettyTapahtumaId(1L)
                        .voimassaLoppuPvm(LocalDate.now().plusWeeks(3))
                        .ryhmaDescription(new TextGroupDto())
                        .ryhmaName("RYHMA2")
                        .build()
        ));

        int numberSent = taskExecutorService.sendExpirationReminders(Period.ofWeeks(3), Period.ofWeeks(2));
        assertEquals(1, numberSent);
    }
}
