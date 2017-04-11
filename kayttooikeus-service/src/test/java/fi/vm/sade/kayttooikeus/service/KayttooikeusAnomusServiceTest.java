package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.config.mapper.CachedDateTimeConverter;
import fi.vm.sade.kayttooikeus.config.mapper.LocalDateConverter;
import fi.vm.sade.kayttooikeus.dto.HaettuKayttooikeusryhmaDto;
import fi.vm.sade.kayttooikeus.dto.KayttoOikeudenTila;
import fi.vm.sade.kayttooikeus.dto.LocalizableDto;
import fi.vm.sade.kayttooikeus.dto.types.AnomusTyyppi;
import fi.vm.sade.kayttooikeus.model.AnomuksenTila;
import fi.vm.sade.kayttooikeus.model.Anomus;
import fi.vm.sade.kayttooikeus.model.HaettuKayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.repositories.HaettuKayttooikeusRyhmaDataRepository;
import fi.vm.sade.kayttooikeus.service.impl.KayttooikeusAnomusServiceImpl;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = {OrikaBeanMapper.class, LocalDateConverter.class, CachedDateTimeConverter.class})
public class KayttooikeusAnomusServiceTest {
    @Autowired
    private OrikaBeanMapper orikaBeanMapper;

    @MockBean
    private HaettuKayttooikeusRyhmaDataRepository haettuKayttooikeusRyhmaDataRepository;

    @MockBean
    private LocalizationService localizationService;

    private KayttooikeusAnomusService kayttooikeusAnomusService;

    @Before
    public void setup() {
        doAnswer(returnsFirstArg()).when(this.localizationService).localize(any(LocalizableDto.class));
        this.kayttooikeusAnomusService = new KayttooikeusAnomusServiceImpl(this.haettuKayttooikeusRyhmaDataRepository,
                orikaBeanMapper, this.localizationService);
    }


    @Test
    public void getAllActiveAnomusByHenkiloOid() {
        given(this.haettuKayttooikeusRyhmaDataRepository.findByAnomusHenkiloOidHenkilo("1.2.3.4.5"))
                .willReturn(newArrayList(createHaettuKayttooikeusryhma("xmail", "kayttooikeusryhma1", "1.2.12.0.1")));

        List<HaettuKayttooikeusryhmaDto> haettuKayttooikeusryhmaDtoList = this.kayttooikeusAnomusService
                .getAllActiveAnomusByHenkiloOid("1.2.3.4.5", false);
        assertThat(haettuKayttooikeusryhmaDtoList.size()).isEqualTo(1);
        assertThat(haettuKayttooikeusryhmaDtoList.get(0).getKasittelyPvm()).isLessThanOrEqualTo(DateTime.now());
        assertThat(haettuKayttooikeusryhmaDtoList.get(0).getTyyppi()).isEqualByComparingTo(KayttoOikeudenTila.ANOTTU);
        assertThat(haettuKayttooikeusryhmaDtoList.get(0).getKayttoOikeusRyhma().getName()).isEqualTo("kayttooikeusryhma1");
        assertThat(haettuKayttooikeusryhmaDtoList.get(0).getAnomus().getOrganisaatioOid()).isEqualTo("1.2.12.0.1");
        assertThat(haettuKayttooikeusryhmaDtoList.get(0).getAnomus().getAnomusTyyppi()).isEqualByComparingTo(AnomusTyyppi.UUSI);

        verify(this.haettuKayttooikeusRyhmaDataRepository, never()).findByAnomusHenkiloOidHenkiloAndAnomusAnomuksenTila(any(), any());
        verify(this.localizationService, atLeastOnce()).localize(any(LocalizableDto.class));
    }

    @Test
    public void getAllActiveAnomusByHenkiloOidOnlyActive() {
        given(this.haettuKayttooikeusRyhmaDataRepository
                .findByAnomusHenkiloOidHenkiloAndAnomusAnomuksenTila("1.2.3.4.5", AnomuksenTila.ANOTTU))
                .willReturn(newArrayList(createHaettuKayttooikeusryhma("xmail", "kayttooikeusryhma1", "1.2.12.0.1")));

        List<HaettuKayttooikeusryhmaDto> haettuKayttooikeusryhmaDtoList = this.kayttooikeusAnomusService
                .getAllActiveAnomusByHenkiloOid("1.2.3.4.5", true);
        assertThat(haettuKayttooikeusryhmaDtoList.size()).isEqualTo(1);
        assertThat(haettuKayttooikeusryhmaDtoList.get(0).getKasittelyPvm()).isLessThanOrEqualTo(DateTime.now());
        assertThat(haettuKayttooikeusryhmaDtoList.get(0).getTyyppi()).isEqualByComparingTo(KayttoOikeudenTila.ANOTTU);
        assertThat(haettuKayttooikeusryhmaDtoList.get(0).getKayttoOikeusRyhma().getName()).isEqualTo("kayttooikeusryhma1");
        assertThat(haettuKayttooikeusryhmaDtoList.get(0).getAnomus().getOrganisaatioOid()).isEqualTo("1.2.12.0.1");
        assertThat(haettuKayttooikeusryhmaDtoList.get(0).getAnomus().getAnomusTyyppi()).isEqualByComparingTo(AnomusTyyppi.UUSI);

        verify(this.haettuKayttooikeusRyhmaDataRepository, never()).findByAnomusHenkiloOidHenkilo(any());
        verify(this.localizationService, atLeastOnce()).localize(any(LocalizableDto.class));
    }

    private static HaettuKayttoOikeusRyhma createHaettuKayttooikeusryhma(String email, String korName, String organisaatioOid) {
        Anomus anomus = Anomus.builder()
                .sahkopostiosoite(email)
                .organisaatioOid(organisaatioOid)
                .anomusTyyppi(AnomusTyyppi.UUSI)
                .build();
        KayttoOikeusRyhma kayttoOikeusRyhma = KayttoOikeusRyhma.builder()
                .name(korName)
                .hidden(false)
                .build();
        return new HaettuKayttoOikeusRyhma(anomus, kayttoOikeusRyhma, DateTime.now(), KayttoOikeudenTila.ANOTTU);
    }
}
