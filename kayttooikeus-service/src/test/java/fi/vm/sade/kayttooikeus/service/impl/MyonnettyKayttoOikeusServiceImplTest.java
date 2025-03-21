package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.dto.KayttoOikeudenTila;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.HenkiloVarmentaja;
import fi.vm.sade.kayttooikeus.model.MyonnettyKayttoOikeusRyhmaTapahtuma;
import fi.vm.sade.kayttooikeus.model.OrganisaatioHenkilo;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRyhmaTapahtumaHistoriaDataRepository;
import fi.vm.sade.kayttooikeus.repositories.MyonnettyKayttoOikeusRyhmaTapahtumaRepository;
import fi.vm.sade.kayttooikeus.repositories.OrganisaatioHenkiloRepository;
import fi.vm.sade.kayttooikeus.service.MyonnettyKayttoOikeusService;
import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
public class MyonnettyKayttoOikeusServiceImplTest {
    @InjectMocks
    private MyonnettyKayttoOikeusServiceImpl myonnettyKayttoOikeusService;

    @Mock
    private PermissionCheckerService permissionCheckerService;

    @Mock
    private HenkiloDataRepository henkiloDataRepository;

    @Mock
    private MyonnettyKayttoOikeusRyhmaTapahtumaRepository myonnettyKayttoOikeusRyhmaTapahtumaRepository;

    @Mock
    private KayttoOikeusRyhmaTapahtumaHistoriaDataRepository kayttoOikeusRyhmaTapahtumaHistoriaDataRepository;

    @Mock
    private OrganisaatioHenkiloRepository organisaatioHenkiloRepository;

    @Test
    public void varmentajallaOnYhaOikeuksiaSamaanOrganisaatioon() {
        Henkilo henkilo = Henkilo.builder().oidHenkilo("kasittelija").build();
        given(this.henkiloDataRepository.findByOidHenkilo(eq("kasittelija"))).willReturn(Optional.of(henkilo));

        Henkilo varmennettavaHenkilo = Henkilo.builder()
                .oidHenkilo("varmennettava")
                .build();
        HenkiloVarmentaja henkiloVarmentaja = new HenkiloVarmentaja();
        henkiloVarmentaja.setTila(true);
        henkiloVarmentaja.setVarmennettavaHenkilo(varmennettavaHenkilo);

        Henkilo varmentavaHenkilo = Henkilo.builder().oidHenkilo("varmentaja")
                .henkiloVarmennettavas(Collections.singleton(henkiloVarmentaja))
                .build();
        henkiloVarmentaja.setVarmentavaHenkilo(varmentavaHenkilo);
        OrganisaatioHenkilo poistuvanOikeudenOrganisaatioHenkilo = OrganisaatioHenkilo.builder()
                .organisaatioOid("1.2.0.0.1")
                .henkilo(varmentavaHenkilo)
                .myonnettyKayttoOikeusRyhmas(new HashSet<>())
                .kayttoOikeusRyhmaHistorias(new HashSet<>())
                .build();
        MyonnettyKayttoOikeusRyhmaTapahtuma poistuvaKayttooikeus = MyonnettyKayttoOikeusRyhmaTapahtuma.builder()
                .organisaatioHenkilo(poistuvanOikeudenOrganisaatioHenkilo)
                .build();
        poistuvaKayttooikeus.setId(1L);

        List<MyonnettyKayttoOikeusRyhmaTapahtuma> kayttoOikeudet = new ArrayList<>();
        kayttoOikeudet.add(poistuvaKayttooikeus);
        given(this.myonnettyKayttoOikeusRyhmaTapahtumaRepository.findByVoimassaLoppuPvmBefore(any())).willReturn(kayttoOikeudet);

        OrganisaatioHenkilo yhaOlemassaOlevanOikeudenOrganisaatioHenkilo = OrganisaatioHenkilo.builder()
                .organisaatioOid("1.2.0.0.1")
                .build();
        MyonnettyKayttoOikeusRyhmaTapahtuma yhaOlemassaOlevaKayttooikeus = MyonnettyKayttoOikeusRyhmaTapahtuma.builder()
                .organisaatioHenkilo(yhaOlemassaOlevanOikeudenOrganisaatioHenkilo)
                .build();
        yhaOlemassaOlevaKayttooikeus.setId(2L);
        List<MyonnettyKayttoOikeusRyhmaTapahtuma> varmentajanOikeudet = new ArrayList<>();
        varmentajanOikeudet.add(poistuvaKayttooikeus);
        varmentajanOikeudet.add(yhaOlemassaOlevaKayttooikeus);
        given(this.myonnettyKayttoOikeusRyhmaTapahtumaRepository.findByOrganisaatioHenkiloHenkiloOidHenkilo(eq("varmentaja")))
                .willReturn(varmentajanOikeudet);

        this.myonnettyKayttoOikeusService.poistaVanhentuneet(new MyonnettyKayttoOikeusService.DeleteDetails(henkilo,
                KayttoOikeudenTila.VANHENTUNUT, "Oikeus vanhentunut"));

        assertThat(henkiloVarmentaja.isTila()).isTrue();
        verify(kayttoOikeusRyhmaTapahtumaHistoriaDataRepository, times(1)).save(any());
        verify(myonnettyKayttoOikeusRyhmaTapahtumaRepository, times(1)).delete(any());
    }

    @Test
    public void varmentajallaEiOleEnaaOikeuksiaSamaanOrganisaatioon() {
        Henkilo henkilo = Henkilo.builder().oidHenkilo("kasittelija").build();
        given(this.henkiloDataRepository.findByOidHenkilo(eq("kasittelija"))).willReturn(Optional.of(henkilo));

        Henkilo varmennettavaHenkilo = Henkilo.builder()
                .oidHenkilo("varmennettava")
                .build();
        HenkiloVarmentaja henkiloVarmentaja = new HenkiloVarmentaja();
        henkiloVarmentaja.setTila(true);
        henkiloVarmentaja.setVarmennettavaHenkilo(varmennettavaHenkilo);

        Henkilo varmentavaHenkilo = Henkilo.builder().oidHenkilo("varmentaja")
                .henkiloVarmennettavas(Collections.singleton(henkiloVarmentaja))
                .build();
        henkiloVarmentaja.setVarmentavaHenkilo(varmentavaHenkilo);
        OrganisaatioHenkilo poistuvanOikeudenOrganisaatioHenkilo = OrganisaatioHenkilo.builder()
                .organisaatioOid("1.2.0.0.1")
                .henkilo(varmentavaHenkilo)
                .myonnettyKayttoOikeusRyhmas(new HashSet<>())
                .kayttoOikeusRyhmaHistorias(new HashSet<>())
                .build();
        MyonnettyKayttoOikeusRyhmaTapahtuma poistuvaKayttooikeus = MyonnettyKayttoOikeusRyhmaTapahtuma.builder()
                .organisaatioHenkilo(poistuvanOikeudenOrganisaatioHenkilo)
                .build();
        poistuvaKayttooikeus.setId(1L);

        List<MyonnettyKayttoOikeusRyhmaTapahtuma> kayttoOikeudet = new ArrayList<>();
        kayttoOikeudet.add(poistuvaKayttooikeus);
        given(this.myonnettyKayttoOikeusRyhmaTapahtumaRepository.findByVoimassaLoppuPvmBefore(any())).willReturn(kayttoOikeudet);

        List<MyonnettyKayttoOikeusRyhmaTapahtuma> varmentajanOikeudet = new ArrayList<>();
        varmentajanOikeudet.add(poistuvaKayttooikeus);
        given(this.myonnettyKayttoOikeusRyhmaTapahtumaRepository.findByOrganisaatioHenkiloHenkiloOidHenkilo(eq("varmentaja")))
                .willReturn(varmentajanOikeudet);

        this.myonnettyKayttoOikeusService.poistaVanhentuneet(new MyonnettyKayttoOikeusService.DeleteDetails(henkilo,
                KayttoOikeudenTila.VANHENTUNUT, "Oikeus vanhentunut"));

        assertThat(henkiloVarmentaja.isTila()).isFalse();
        verify(kayttoOikeusRyhmaTapahtumaHistoriaDataRepository, times(1)).save(any());
        verify(myonnettyKayttoOikeusRyhmaTapahtumaRepository, times(1)).delete(any());
    }

}
