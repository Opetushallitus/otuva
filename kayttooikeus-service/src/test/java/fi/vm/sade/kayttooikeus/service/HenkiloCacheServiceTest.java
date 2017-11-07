package fi.vm.sade.kayttooikeus.service;

import com.google.common.collect.Lists;
import fi.vm.sade.kayttooikeus.config.scheduling.ScheduledTasks;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.ScheduleTimestamps;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.ScheduleTimestampsDataRepository;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloHakuPerustietoDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;

@RunWith(SpringRunner.class)
@TestPropertySource(properties = {"kayttooikeus.scheduling.enabled=TRUE"})
public class HenkiloCacheServiceTest extends AbstractServiceTest {

    @MockBean
    private OppijanumerorekisteriClient oppijanumerorekisteriClient;

    @MockBean
    private HenkiloDataRepository henkiloDataRepository;

    @MockBean
    private ScheduleTimestampsDataRepository scheduleTimestampsDataRepository;

    @Autowired
    private HenkiloCacheService henkiloCacheService;

    @Autowired
    private ScheduledTasks scheduledTasks;

    @Test
    @WithMockUser(username = "1.2.3.4.5", authorities = {"ROLE_APP_HENKILONHALLINTA_OPHREKISTERI", "ROLE_APP_HENKILONHALLINTA_OPHREKISTERI_1.2.246.562.10.00000000001"})
    public void updateHenkiloCache() throws Exception {
        Henkilo henkilo = Henkilo.builder().oidHenkilo("1.2.3.4.5").build();
        LocalDateTime timestamp = LocalDateTime.now().minusDays(1);
        Optional<ScheduleTimestamps> henkiloCacheModified = Optional.of(new ScheduleTimestamps(timestamp, "henkilocache"));
        given(this.scheduleTimestampsDataRepository.findFirstByIdentifier("henkilocache"))
                .willReturn(henkiloCacheModified);
        given(this.oppijanumerorekisteriClient.getModifiedSince(timestamp, 0L, 1000L))
                .willReturn(Lists.newArrayList("1.2.3.4.5"));
        given(this.oppijanumerorekisteriClient.getAllByOids(eq(0L), eq(1000L), anyList()))
                .willReturn(Lists.newArrayList(new HenkiloHakuPerustietoDto("1.2.3.4.5", "fakehetu",
                        "arpa arpa2", "arpa", "kuutio", true, false, false, false)));
        given(this.henkiloDataRepository.findByOidHenkiloIn(anyList()))
                .willReturn(Lists.newArrayList(henkilo));
        given(this.henkiloDataRepository.countByEtunimetCachedNotNull()).willReturn(1L);

        this.scheduledTasks.updateHenkiloNimiCache();

        assertThat(henkilo.getEtunimetCached()).isEqualTo("arpa arpa2");
        assertThat(henkilo.getSukunimiCached()).isEqualTo("kuutio");
        assertThat(henkilo.getPassivoituCached()).isFalse();
        assertThat(henkilo.getDuplicateCached()).isFalse();

        assertThat(henkiloCacheModified.get().getModified()).isNotEqualByComparingTo(timestamp);
    }

}