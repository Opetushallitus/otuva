package fi.vm.sade.kayttooikeus.service;

import com.google.common.collect.Lists;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.HenkiloCacheModified;
import fi.vm.sade.kayttooikeus.repositories.HenkiloCacheModifiedDataRepository;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloHakuPerustietoDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
public class HenkiloCacheServiceTest extends AbstractServiceTest {

    @MockBean
    private OppijanumerorekisteriClient oppijanumerorekisteriClient;

    @MockBean
    private HenkiloDataRepository henkiloDataRepository;

    @MockBean
    private HenkiloCacheModifiedDataRepository henkiloCacheModifiedDataRepository;

    @Autowired
    private HenkiloCacheService henkiloCacheService;

    @Test
    public void updateHenkiloCache() throws Exception {
        Henkilo henkilo = Henkilo.builder().oidHenkilo("1.2.3.4.5").build();
        LocalDateTime timestamp = LocalDateTime.now().minusDays(1);
        Optional<HenkiloCacheModified> henkiloCacheModified = Optional.of(new HenkiloCacheModified(timestamp));
        given(this.henkiloCacheModifiedDataRepository.findFirstBy())
                .willReturn(henkiloCacheModified);
        given(this.oppijanumerorekisteriClient.getModifiedSince(timestamp, 0L, 1000L))
                .willReturn(Lists.newArrayList("1.2.3.4.5"));
        given(this.oppijanumerorekisteriClient.getAllByOids(eq(0L), eq(1000L), anyListOf(String.class)))
                .willReturn(Lists.newArrayList(new HenkiloHakuPerustietoDto("1.2.3.4.5", "fakehetu",
                        "arpa arpa2", "arpa", "kuutio", true, false, false, false)));
        given(this.henkiloDataRepository.findByOidHenkiloIn(anyListOf(String.class)))
                .willReturn(Lists.newArrayList(henkilo));

        this.henkiloCacheService.updateHenkiloCache();

        assertThat(henkilo.getEtunimetCached()).isEqualTo("arpa arpa2");
        assertThat(henkilo.getSukunimiCached()).isEqualTo("kuutio");
        assertThat(henkilo.getPassivoituCached()).isFalse();
        assertThat(henkilo.getDuplicateCached()).isFalse();

        assertThat(henkiloCacheModified.get().getModified()).isNotEqualByComparingTo(timestamp);
    }

    @Test
    public void forceCleanUpdateHenkiloCache() throws Exception {
        Henkilo henkilo = Henkilo.builder().oidHenkilo("1.2.3.4.5").build();
        LocalDateTime timestamp = LocalDateTime.now().minusDays(1);
        Optional<HenkiloCacheModified> henkiloCacheModified = Optional.of(new HenkiloCacheModified(timestamp));
        given(this.oppijanumerorekisteriClient.getAllByOids(eq(0L), eq(1000L), anyListOf(String.class)))
        .willReturn(Lists.newArrayList(new HenkiloHakuPerustietoDto("1.2.3.4.5", "fakehetu",
                "arpa arpa2", "arpa", "kuutio", true, false, false, false)));
        given(this.henkiloDataRepository.findByOidHenkiloIn(anyListOf(String.class)))
                .willReturn(Lists.newArrayList(henkilo));
        given(this.henkiloCacheModifiedDataRepository.findFirstBy())
                .willReturn(henkiloCacheModified);

        this.henkiloCacheService.forceCleanUpdateHenkiloCache();

        assertThat(henkilo.getEtunimetCached()).isEqualTo("arpa arpa2");
        assertThat(henkilo.getSukunimiCached()).isEqualTo("kuutio");
        assertThat(henkilo.getPassivoituCached()).isFalse();
        assertThat(henkilo.getDuplicateCached()).isFalse();

        assertThat(henkiloCacheModified.get().getModified()).isNotEqualByComparingTo(timestamp);
    }

    @Test
    public void forceCleanUpdateHenkiloCacheHenkiloNotExist() throws Exception {
        LocalDateTime timestamp = LocalDateTime.now().minusDays(1);
        Optional<HenkiloCacheModified> henkiloCacheModified = Optional.of(new HenkiloCacheModified(timestamp));
        given(this.oppijanumerorekisteriClient.getAllByOids(eq(0L), eq(1000L), anyListOf(String.class)))
        .willReturn(Lists.newArrayList(new HenkiloHakuPerustietoDto("1.2.3.4.5", "fakehetu",
                "arpa arpa2", "arpa", "kuutio", true, false, false, false)));
        given(this.henkiloDataRepository.findByOidHenkiloIn(anyListOf(String.class)))
                .willReturn(Lists.newArrayList());
        given(this.henkiloCacheModifiedDataRepository.findFirstBy())
                .willReturn(henkiloCacheModified);
        doAnswer(returnsFirstArg()).when(this.henkiloDataRepository).save(any(Henkilo.class));
        this.henkiloCacheService.forceCleanUpdateHenkiloCache();

        ArgumentCaptor<Henkilo> henkiloArgumentCaptor = ArgumentCaptor.forClass(Henkilo.class);
        verify(this.henkiloDataRepository, times(1)).save(henkiloArgumentCaptor.capture());
        Henkilo henkilo = henkiloArgumentCaptor.getValue();

        assertThat(henkilo.getEtunimetCached()).isEqualTo("arpa arpa2");
        assertThat(henkilo.getSukunimiCached()).isEqualTo("kuutio");
        assertThat(henkilo.getPassivoituCached()).isFalse();
        assertThat(henkilo.getDuplicateCached()).isFalse();

        assertThat(henkiloCacheModified.get().getModified()).isNotEqualByComparingTo(timestamp);
    }

}