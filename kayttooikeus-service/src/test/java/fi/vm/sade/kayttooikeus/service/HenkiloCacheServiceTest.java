package fi.vm.sade.kayttooikeus.service;

import com.google.common.collect.Lists;
import fi.vm.sade.kayttooikeus.config.scheduling.UpdateHenkiloNimiCacheTask;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.ScheduleTimestamps;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.ScheduleTimestampsDataRepository;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloHakuPerustietoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
public class HenkiloCacheServiceTest extends AbstractServiceTest {

    @MockitoBean
    private OppijanumerorekisteriClient oppijanumerorekisteriClient;

    @MockitoBean
    private HenkiloDataRepository henkiloDataRepository;

    @MockitoBean
    private ScheduleTimestampsDataRepository scheduleTimestampsDataRepository;

    @Autowired
    private HenkiloCacheService henkiloCacheService;

    private UpdateHenkiloNimiCacheTask scheduledTasks;

    @BeforeEach
    public void setup() {
        this.scheduledTasks = new UpdateHenkiloNimiCacheTask(
                this.henkiloDataRepository,
                this.henkiloCacheService,
                this.scheduleTimestampsDataRepository,
                this.oppijanumerorekisteriClient);
    }

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = {"ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA", "ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA_1.2.246.562.10.00000000001"})
    public void updateHenkiloCache() throws Exception {
        Henkilo henkilo = Henkilo.builder().oidHenkilo("1.2.3.4.5").build();
        LocalDateTime timestamp = LocalDateTime.now().minusDays(1);
        Optional<ScheduleTimestamps> henkiloCacheModified = Optional.of(new ScheduleTimestamps(timestamp, "henkilocache"));
        given(this.scheduleTimestampsDataRepository.findFirstByIdentifier("henkilocache"))
                .willReturn(henkiloCacheModified);
        given(this.oppijanumerorekisteriClient.getModifiedSince(timestamp, 0L, 2000L))
                .willReturn(Lists.newArrayList("1.2.3.4.5"));
        given(this.oppijanumerorekisteriClient.getAllByOids(eq(0L), eq(2000L), anyList()))
                .willReturn(Lists.newArrayList(new HenkiloHakuPerustietoDto("1.2.3.4.5", "fakehetu",
                        "arpa arpa2", "arpa", "kuutio", true, false, false, false)));
        given(this.henkiloDataRepository.findByOidHenkiloIn(anyList()))
                .willReturn(Lists.newArrayList(henkilo));
        given(this.henkiloDataRepository.countByEtunimetCachedNotNull()).willReturn(1L);

        this.scheduledTasks.execute();

        assertThat(henkilo.getEtunimetCached()).isEqualTo("arpa arpa2");
        assertThat(henkilo.getSukunimiCached()).isEqualTo("kuutio");
        assertThat(henkilo.getPassivoituCached()).isFalse();
        assertThat(henkilo.getDuplicateCached()).isFalse();

        assertThat(henkiloCacheModified.get().getModified()).isNotEqualTo(timestamp);
    }

}