package fi.vm.sade.kayttooikeus.service;

import com.google.common.collect.Lists;
import fi.vm.sade.kayttooikeus.config.OrikaBeanMapper;
import fi.vm.sade.kayttooikeus.config.mapper.CachedDateTimeConverter;
import fi.vm.sade.kayttooikeus.config.mapper.LocalDateConverter;
import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.dto.*;
import fi.vm.sade.kayttooikeus.dto.types.AnomusTyyppi;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.*;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import fi.vm.sade.kayttooikeus.service.impl.KayttooikeusAnomusServiceImpl;
import fi.vm.sade.kayttooikeus.service.validators.HaettuKayttooikeusryhmaValidator;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static fi.vm.sade.kayttooikeus.util.CreateUtil.*;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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

    @MockBean
    private HenkiloRepository henkiloRepository;

    @MockBean
    private MyonnettyKayttoOikeusRyhmaTapahtumaDataRepository myonnettyKayttoOikeusRyhmaTapahtumaDataRepository;

    @MockBean
    private KayttoOikeusRyhmaMyontoViiteRepository kayttoOikeusRyhmaMyontoViiteRepository;

    @MockBean
    private KayttoOikeusRyhmaTapahtumaHistoriaDataRepository kayttoOikeusRyhmaTapahtumaHistoriaDataRepository;

    @MockBean
    private HaettuKayttooikeusryhmaValidator haettuKayttooikeusryhmaValidator;

    @MockBean
    private PermissionCheckerService permissionCheckerService;

    @MockBean
    private KayttooikeusryhmaDataRepository kayttooikeusryhmaDataRepository;

    @MockBean
    private OrganisaatioClient organisaatioClient;

    @MockBean
    private AnomusDataRepository anomusDataRepository;

    @MockBean
    private EmailService emailService;

    private KayttooikeusAnomusService kayttooikeusAnomusService;

    @Before
    public void setup() {
        doAnswer(returnsFirstArg()).when(this.localizationService).localize(any(LocalizableDto.class));
        this.kayttooikeusAnomusService = spy(new KayttooikeusAnomusServiceImpl(
                this.haettuKayttooikeusRyhmaDataRepository,
                this.henkiloRepository,
                this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository,
                this.kayttoOikeusRyhmaMyontoViiteRepository,
                this.kayttoOikeusRyhmaTapahtumaHistoriaDataRepository,
                this.orikaBeanMapper,
                this.localizationService,
                this.haettuKayttooikeusryhmaValidator,
                this.permissionCheckerService,
                this.kayttooikeusryhmaDataRepository,
                new CommonProperties(),
                this.organisaatioClient,
                this.anomusDataRepository,
                this.emailService)
        );
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

    @Test
    public void grantKayttooikeusryhma() {
        given(this.permissionCheckerService.notOwnData(anyString())).willReturn(true);
        given(this.permissionCheckerService.checkRoleForOrganisation(any(), any())).willReturn(true);
        given(this.kayttooikeusryhmaDataRepository.findById(2001L)).willReturn(Optional.of(createKayttoOikeusRyhma(2001L)));
        given(this.henkiloRepository.findByOidHenkilo("1.2.3.4.5")).willReturn(Optional.of(createHenkilo("1.2.3.4.5")));
        given(this.henkiloRepository.findByOidHenkilo("1.2.3.4.1")).willReturn(Optional.of(createHenkilo("1.2.3.4.5")));
        given(this.permissionCheckerService.getCurrentUserOid()).willReturn("1.2.3.4.1");
        given(this.permissionCheckerService.organisaatioLimitationCheck(anyString(), anySetOf(OrganisaatioViite.class))).willReturn(true);
        given(this.permissionCheckerService.kayttooikeusMyontoviiteLimitationCheck(2001L)).willReturn(true);
        given(this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository.findMyonnettyTapahtuma(2001L,
                "1.2.0.0.1", "1.2.3.4.5"))
                .willReturn(Optional.empty());

        GrantKayttooikeusryhmaDto grantKayttooikeusryhmaDto = createGrantKayttooikeusryhmaDto(2001L,
                LocalDate.now().plusYears(1));
        this.kayttooikeusAnomusService.grantKayttooikeusryhma("1.2.3.4.5", "1.2.0.0.1",
                Lists.newArrayList(grantKayttooikeusryhmaDto));

        ArgumentCaptor<MyonnettyKayttoOikeusRyhmaTapahtuma> myonnettyKayttoOikeusRyhmaTapahtumaArgumentCaptor =
                ArgumentCaptor.forClass(MyonnettyKayttoOikeusRyhmaTapahtuma.class);
        verify(this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository, times(1))
                .save(myonnettyKayttoOikeusRyhmaTapahtumaArgumentCaptor.capture());
        MyonnettyKayttoOikeusRyhmaTapahtuma myonnettyKayttoOikeusRyhmaTapahtuma = myonnettyKayttoOikeusRyhmaTapahtumaArgumentCaptor.getValue();
        ArgumentCaptor<KayttoOikeusRyhmaTapahtumaHistoria> kayttoOikeusRyhmaTapahtumaHistoriaArgumentCaptor =
                ArgumentCaptor.forClass(KayttoOikeusRyhmaTapahtumaHistoria.class);
        verify(this.kayttoOikeusRyhmaTapahtumaHistoriaDataRepository, times(1))
                .save(kayttoOikeusRyhmaTapahtumaHistoriaArgumentCaptor.capture());
        KayttoOikeusRyhmaTapahtumaHistoria kayttoOikeusRyhmaTapahtumaHistoria = kayttoOikeusRyhmaTapahtumaHistoriaArgumentCaptor.getValue();
        assertThat(kayttoOikeusRyhmaTapahtumaHistoria.getTila())
                .isEqualByComparingTo(myonnettyKayttoOikeusRyhmaTapahtuma.getTila())
                .isEqualByComparingTo(KayttoOikeudenTila.MYONNETTY);
        assertThat(kayttoOikeusRyhmaTapahtumaHistoria.getOrganisaatioHenkilo().getOrganisaatioOid())
                .isEqualTo(myonnettyKayttoOikeusRyhmaTapahtuma.getOrganisaatioHenkilo().getOrganisaatioOid())
                .isEqualTo("1.2.0.0.1");
        assertThat(kayttoOikeusRyhmaTapahtumaHistoria.getKayttoOikeusRyhma().getId())
                .isEqualTo(myonnettyKayttoOikeusRyhmaTapahtuma.getKayttoOikeusRyhma().getId())
                .isEqualTo(2001L);
        assertThat(kayttoOikeusRyhmaTapahtumaHistoria.getKayttoOikeusRyhma().getName())
                .isEqualTo(myonnettyKayttoOikeusRyhmaTapahtuma.getKayttoOikeusRyhma().getName())
                .isEqualTo("Kayttooikeusryhma x");
        assertThat(kayttoOikeusRyhmaTapahtumaHistoria.getKayttoOikeusRyhma().getRooliRajoite())
                .isEqualTo(myonnettyKayttoOikeusRyhmaTapahtuma.getKayttoOikeusRyhma().getRooliRajoite())
                .isEqualTo("10");

        assertThat(kayttoOikeusRyhmaTapahtumaHistoria.getSyy()).isEqualTo("Oikeuksien lisäys");

    }

    // MyonnettyKayttooikeusryhmaTapahtuma already exists
    @Test
    public void grantKayttooikeusryhmaUusittu() {
        given(this.permissionCheckerService.notOwnData(anyString())).willReturn(true);
        given(this.permissionCheckerService.checkRoleForOrganisation(any(), any())).willReturn(true);
        given(this.kayttooikeusryhmaDataRepository.findById(2001L)).willReturn(Optional.of(createKayttoOikeusRyhma(2001L)));
        given(this.henkiloRepository.findByOidHenkilo("1.2.3.4.5")).willReturn(Optional.of(createHenkilo("1.2.3.4.5")));
        given(this.henkiloRepository.findByOidHenkilo("1.2.3.4.1")).willReturn(Optional.of(createHenkilo("1.2.3.4.5")));
        given(this.permissionCheckerService.getCurrentUserOid()).willReturn("1.2.3.4.1");
        given(this.permissionCheckerService.organisaatioLimitationCheck(anyString(), anySetOf(OrganisaatioViite.class))).willReturn(true);
        given(this.permissionCheckerService.kayttooikeusMyontoviiteLimitationCheck(2001L)).willReturn(true);
        given(this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository.findMyonnettyTapahtuma(2001L,
                "1.2.0.0.1", "1.2.3.4.5"))
                .willReturn(Optional.of(createMyonnettyKayttoOikeusRyhmaTapahtuma(3001L, 2001L)));

        GrantKayttooikeusryhmaDto grantKayttooikeusryhmaDto = createGrantKayttooikeusryhmaDto(2001L,
                LocalDate.now().plusYears(1));
        this.kayttooikeusAnomusService.grantKayttooikeusryhma("1.2.3.4.5", "1.2.0.0.1",
                Lists.newArrayList(grantKayttooikeusryhmaDto));

        ArgumentCaptor<MyonnettyKayttoOikeusRyhmaTapahtuma> myonnettyKayttoOikeusRyhmaTapahtumaArgumentCaptor =
                ArgumentCaptor.forClass(MyonnettyKayttoOikeusRyhmaTapahtuma.class);
        verify(this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository, times(1))
                .save(myonnettyKayttoOikeusRyhmaTapahtumaArgumentCaptor.capture());
        MyonnettyKayttoOikeusRyhmaTapahtuma myonnettyKayttoOikeusRyhmaTapahtuma = myonnettyKayttoOikeusRyhmaTapahtumaArgumentCaptor.getValue();
        ArgumentCaptor<KayttoOikeusRyhmaTapahtumaHistoria> kayttoOikeusRyhmaTapahtumaHistoriaArgumentCaptor =
                ArgumentCaptor.forClass(KayttoOikeusRyhmaTapahtumaHistoria.class);
        verify(this.kayttoOikeusRyhmaTapahtumaHistoriaDataRepository, times(1))
                .save(kayttoOikeusRyhmaTapahtumaHistoriaArgumentCaptor.capture());
        KayttoOikeusRyhmaTapahtumaHistoria kayttoOikeusRyhmaTapahtumaHistoria = kayttoOikeusRyhmaTapahtumaHistoriaArgumentCaptor.getValue();
        assertThat(myonnettyKayttoOikeusRyhmaTapahtuma.getTila())
                .isEqualByComparingTo(kayttoOikeusRyhmaTapahtumaHistoria.getTila())
                .isEqualByComparingTo(KayttoOikeudenTila.UUSITTU);

        assertThat(kayttoOikeusRyhmaTapahtumaHistoria.getSyy()).isEqualTo("Oikeuksien päivitys");
    }


    @Test
    public void updateHaettuKayttooikeusryhmaMyonnetty() {
        HaettuKayttoOikeusRyhma haettuKayttoOikeusRyhma = createHaettuKayttoOikeusRyhma("1.2.3.4.5", "1.2.3.4.1",
                "1.2.0.0.1", "devaaja", "Haluan devata", 2001L);
        // this has it's own test
        doNothing().when(this.kayttooikeusAnomusService).grantKayttooikeusryhma(any(), anyString(), anyListOf(GrantKayttooikeusryhmaDto.class));
        given(this.haettuKayttooikeusRyhmaDataRepository.findById(1L))
                .willReturn(Optional.of(haettuKayttoOikeusRyhma));
        given(this.kayttooikeusryhmaDataRepository.findById(2001L)).willReturn(Optional.of(haettuKayttoOikeusRyhma.getKayttoOikeusRyhma()));
        given(this.permissionCheckerService.checkRoleForOrganisation(anyListOf(String.class), anyListOf(String.class)))
                .willReturn(true);
        given(this.permissionCheckerService.getCurrentUserOid()).willReturn("1.2.3.4.1");
        given(this.permissionCheckerService.organisaatioLimitationCheck(anyString(), anySetOf(OrganisaatioViite.class))).willReturn(true);
        given(this.permissionCheckerService.kayttooikeusMyontoviiteLimitationCheck(2001L)).willReturn(true);
        given(this.permissionCheckerService.notOwnData("1.2.3.4.5")).willReturn(true);
        given(this.henkiloRepository.findByOidHenkilo("1.2.3.4.5")).willReturn(Optional.of(createHenkilo("1.2.3.4.5")));
        given(this.henkiloRepository.findByOidHenkilo("1.2.3.4.1")).willReturn(Optional.of(createHenkilo("1.2.3.4.1")));
        given(this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository.findMyonnettyTapahtuma(2001L,
                "1.2.0.0.1", "1.2.3.4.5"))
                .willReturn(Optional.of(createMyonnettyKayttoOikeusRyhmaTapahtuma(3001L, 2001L)));

        UpdateHaettuKayttooikeusryhmaDto updateHaettuKayttooikeusryhmaDto = createUpdateHaettuKayttooikeusryhmaDto(1L,
                "MYONNETTY", LocalDate.now().plusYears(1));
        this.kayttooikeusAnomusService.updateHaettuKayttooikeusryhma(updateHaettuKayttooikeusryhmaDto);

        assertThat(haettuKayttoOikeusRyhma.getTyyppi()).isEqualByComparingTo(KayttoOikeudenTila.MYONNETTY);
        assertThat(haettuKayttoOikeusRyhma.getAnomus().getAnomuksenTila()).isEqualByComparingTo(AnomuksenTila.KASITELTY);
        assertThat(haettuKayttoOikeusRyhma.getAnomus().getAnomusTyyppi()).isEqualByComparingTo(AnomusTyyppi.UUSI);
    }

    @Test
    public void updateHaettuKayttooikeusryhmaHylatty() {
        HaettuKayttoOikeusRyhma haettuKayttoOikeusRyhma = createHaettuKayttoOikeusRyhma("1.2.3.4.5", "1.2.3.4.1",
                "1.2.0.0.1", "devaaja", "Haluan devata", 2001L);
        // this has it's own test
        doNothing().when(this.kayttooikeusAnomusService).grantKayttooikeusryhma(any(), anyString(), anyListOf(GrantKayttooikeusryhmaDto.class));
        given(this.haettuKayttooikeusRyhmaDataRepository.findById(1L))
                .willReturn(Optional.of(haettuKayttoOikeusRyhma));
        given(this.kayttooikeusryhmaDataRepository.findById(2001L)).willReturn(Optional.of(haettuKayttoOikeusRyhma.getKayttoOikeusRyhma()));
        given(this.permissionCheckerService.checkRoleForOrganisation(anyListOf(String.class), anyListOf(String.class)))
                .willReturn(true);
        given(this.permissionCheckerService.getCurrentUserOid()).willReturn("1.2.3.4.1");
        given(this.permissionCheckerService.organisaatioLimitationCheck(anyString(), anySetOf(OrganisaatioViite.class))).willReturn(true);
        given(this.permissionCheckerService.kayttooikeusMyontoviiteLimitationCheck(2001L)).willReturn(true);
        given(this.permissionCheckerService.notOwnData("1.2.3.4.5")).willReturn(true);
        given(this.henkiloRepository.findByOidHenkilo("1.2.3.4.5")).willReturn(Optional.of(createHenkilo("1.2.3.4.5")));
        given(this.henkiloRepository.findByOidHenkilo("1.2.3.4.1")).willReturn(Optional.of(createHenkilo("1.2.3.4.1")));

        UpdateHaettuKayttooikeusryhmaDto updateHaettuKayttooikeusryhmaDto = createUpdateHaettuKayttooikeusryhmaDto(1L,
                "HYLATTY", LocalDate.now().plusYears(1));
        this.kayttooikeusAnomusService.updateHaettuKayttooikeusryhma(updateHaettuKayttooikeusryhmaDto);

        assertThat(haettuKayttoOikeusRyhma.getTyyppi()).isEqualByComparingTo(KayttoOikeudenTila.HYLATTY);
        assertThat(haettuKayttoOikeusRyhma.getAnomus().getAnomuksenTila()).isEqualByComparingTo(AnomuksenTila.HYLATTY);
        assertThat(haettuKayttoOikeusRyhma.getAnomus().getAnomusTyyppi()).isEqualByComparingTo(AnomusTyyppi.UUSI);
    }

    @Test
    public void updateHaettuKayttooikeusryhmaHylkaaOneFromAnomus() {
        HaettuKayttoOikeusRyhma haettuKayttoOikeusRyhma = createHaettuKayttoOikeusRyhma("1.2.3.4.5", "1.2.3.4.1",
                "1.2.0.0.1", "devaaja", "Haluan devata", 2001L);
        // Second haettu kayttooikeusryhma so anomus will not be finalized
        HaettuKayttoOikeusRyhma anotherHaettuKayttoOikeusRyhma = createHaettuKayttoOikeusRyhma("1.2.3.4.5", "1.2.3.4.1",
                "1.2.0.0.1", "devaaja", "Haluan devata", 2002L);
        haettuKayttoOikeusRyhma.getAnomus().addHaettuKayttoOikeusRyhma(anotherHaettuKayttoOikeusRyhma);
        // this has it's own test
        doNothing().when(this.kayttooikeusAnomusService).grantKayttooikeusryhma(any(), anyString(), anyListOf(GrantKayttooikeusryhmaDto.class));
        given(this.haettuKayttooikeusRyhmaDataRepository.findById(1L))
                .willReturn(Optional.of(haettuKayttoOikeusRyhma));
        given(this.kayttooikeusryhmaDataRepository.findById(2001L)).willReturn(Optional.of(haettuKayttoOikeusRyhma.getKayttoOikeusRyhma()));
        given(this.permissionCheckerService.checkRoleForOrganisation(anyListOf(String.class), anyListOf(String.class)))
                .willReturn(true);
        given(this.permissionCheckerService.getCurrentUserOid()).willReturn("1.2.3.4.1");
        given(this.permissionCheckerService.organisaatioLimitationCheck(anyString(), anySetOf(OrganisaatioViite.class))).willReturn(true);
        given(this.permissionCheckerService.kayttooikeusMyontoviiteLimitationCheck(2001L)).willReturn(true);
        given(this.permissionCheckerService.notOwnData("1.2.3.4.5")).willReturn(true);
        given(this.henkiloRepository.findByOidHenkilo("1.2.3.4.5")).willReturn(Optional.of(createHenkilo("1.2.3.4.5")));
        given(this.henkiloRepository.findByOidHenkilo("1.2.3.4.1")).willReturn(Optional.of(createHenkilo("1.2.3.4.1")));

        UpdateHaettuKayttooikeusryhmaDto updateHaettuKayttooikeusryhmaDto = createUpdateHaettuKayttooikeusryhmaDto(1L,
                "HYLATTY", LocalDate.now().plusYears(1));
        this.kayttooikeusAnomusService.updateHaettuKayttooikeusryhma(updateHaettuKayttooikeusryhmaDto);

        assertThat(haettuKayttoOikeusRyhma.getTyyppi()).isEqualByComparingTo(KayttoOikeudenTila.HYLATTY);
        assertThat(haettuKayttoOikeusRyhma.getAnomus().getAnomuksenTila()).isEqualByComparingTo(AnomuksenTila.ANOTTU);
        assertThat(haettuKayttoOikeusRyhma.getAnomus().getAnomusTyyppi()).isEqualByComparingTo(AnomusTyyppi.UUSI);
    }
}
