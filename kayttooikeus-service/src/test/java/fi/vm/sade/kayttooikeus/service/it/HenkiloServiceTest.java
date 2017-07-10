package fi.vm.sade.kayttooikeus.service.it;

import com.google.common.collect.Sets;
import fi.vm.sade.kayttooikeus.dto.HenkiloReadDto;
import fi.vm.sade.kayttooikeus.dto.HenkilohakuCriteriaDto;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioMinimalDto;
import fi.vm.sade.kayttooikeus.enumeration.OrderByHenkilohaku;
import fi.vm.sade.kayttooikeus.model.MyonnettyKayttoOikeusRyhmaTapahtuma;
import fi.vm.sade.kayttooikeus.model.OrganisaatioHenkilo;
import fi.vm.sade.kayttooikeus.repositories.MyonnettyKayttoOikeusRyhmaTapahtumaDataRepository;
import fi.vm.sade.kayttooikeus.repositories.OrganisaatioHenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.dto.HenkilohakuResultDto;
import fi.vm.sade.kayttooikeus.service.HenkiloService;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static fi.vm.sade.kayttooikeus.repositories.populate.HenkiloPopulator.henkilo;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttajatiedotPopulator.kayttajatiedot;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusPopulator.oikeus;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusRyhmaPopulator.kayttoOikeusRyhma;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloKayttoOikeusPopulator.myonnettyKayttoOikeus;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloPopulator.organisaatioHenkilo;
import static fi.vm.sade.kayttooikeus.util.CreateUtil.creaetOrganisaatioPerustietoWithNimi;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;

@RunWith(SpringRunner.class)
public class HenkiloServiceTest extends AbstractServiceIntegrationTest {

    @Autowired
    private HenkiloService henkiloService;

    @Autowired
    private OrganisaatioHenkiloDataRepository organisaatioHenkiloDataRepository;

    @Autowired
    private MyonnettyKayttoOikeusRyhmaTapahtumaDataRepository myonnettyKayttoOikeusRyhmaTapahtumaDataRepository;

    @MockBean
    private OrganisaatioClient organisaatioClient;

    @Test
    @WithMockUser(value = "1.2.3.4.5", authorities = "ROLE_APP_HENKILONHALLINTA_OPHREKISTERI")
    public void getByKayttajatunnus() {
        populate(kayttajatiedot(henkilo("oid1"), "user1"));

        HenkiloReadDto dto = henkiloService.getByKayttajatunnus("user1");

        assertThat(dto.getOid()).isEqualTo("oid1");
    }

    @Transactional
    @WithMockUser(value = "1.2.3.4.1", authorities = "ROLE_APP_HENKILONHALLINTA_OPHREKISTERI")
    public void passivoiHenkiloOrganisationsAndKayttooikeus() {
        // Käsittelijä
        populate(henkilo("1.2.3.4.1"));
        // Passivoitava
        String oidHenkilo = "1.2.3.4.5";
        MyonnettyKayttoOikeusRyhmaTapahtuma myonnettyKayttoOikeusRyhmaTapahtuma = populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo(oidHenkilo), "4.5.6.7.8")
                        .tehtavanimike("testaaja"),
                kayttoOikeusRyhma("RYHMA2")
                        .withOikeus(oikeus("KOODISTO", "WRITE")))
                .voimassaAlkaen(LocalDate.now().minusMonths(1)).voimassaPaattyen(LocalDate.now().plusMonths(1)));
        myonnettyKayttoOikeusRyhmaTapahtuma.getOrganisaatioHenkilo().setMyonnettyKayttoOikeusRyhmas(Sets.newHashSet(myonnettyKayttoOikeusRyhmaTapahtuma));
        this.em.persist(myonnettyKayttoOikeusRyhmaTapahtuma);
        this.henkiloService.disableHenkiloOrganisationsAndKayttooikeus("1.2.3.4.5", "1.2.3.4.1");

        List<OrganisaatioHenkilo> henkilo = this.organisaatioHenkiloDataRepository.findByHenkiloOidHenkilo(oidHenkilo);
        assertThat(henkilo.size()).isEqualTo(1);
        assertThat(henkilo.get(0).getMyonnettyKayttoOikeusRyhmas()).isEmpty();
        MyonnettyKayttoOikeusRyhmaTapahtuma mkrt = this.myonnettyKayttoOikeusRyhmaTapahtumaDataRepository.findOne(myonnettyKayttoOikeusRyhmaTapahtuma.getId());
        assertThat(mkrt).isNull();
    }


    @Test
    @WithMockUser(value = "1.2.3.4.1", authorities = "ROLE_APP_HENKILONHALLINTA_OPHREKISTERI")
    public void henkilohakuAsAdminSearchByName() {
        populate(henkilo("1.2.3.4.2").withNimet("arpa", "kuutio").withPassive(false).withDuplikate(true));
        populate(henkilo("1.2.3.4.3").withNimet("arpa", "kuutio").withPassive(true).withDuplikate(false));

        HenkilohakuCriteriaDto henkilohakuCriteriaDto = new HenkilohakuCriteriaDto(null, true,
                false, true, "arpa", null, null);
        List<HenkilohakuResultDto> henkilohakuResultDtoList = this.henkiloService.henkilohaku(henkilohakuCriteriaDto, 0L, OrderByHenkilohaku.HENKILO_NIMI_ASC);
        assertThat(henkilohakuResultDtoList).extracting(HenkilohakuResultDto::getNimi).containsExactly("kuutio, arpa");
    }

    @Test
    @WithMockUser(value = "1.2.3.4.1", authorities = "ROLE_APP_HENKILONHALLINTA_OPHREKISTERI")
    public void henkilohakuAsAdminSearchOrganisationRequired() {
        populate(henkilo("1.2.3.4.2").withNimet("arpa", "kuutio").withPassive(false).withDuplikate(true));
        populate(henkilo("1.2.3.4.3").withNimet("arpa", "kuutio").withPassive(true).withDuplikate(false));

        HenkilohakuCriteriaDto henkilohakuCriteriaDto = new HenkilohakuCriteriaDto(null, false,
                false, true, "arpa", null, null);
        List<HenkilohakuResultDto> henkilohakuResultDtoList = this.henkiloService.henkilohaku(henkilohakuCriteriaDto, 0L, OrderByHenkilohaku.HENKILO_NIMI_ASC);
        assertThat(henkilohakuResultDtoList).isEmpty();
    }

    @Test
    @WithMockUser(value = "1.2.3.4.1", authorities = "ROLE_APP_HENKILONHALLINTA_OPHREKISTERI")
    public void henkilohakuAsAdminByOrganisation() {
        populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.5"), "3.4.5.6.7"),
                kayttoOikeusRyhma("RYHMA")
        ));

        populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.6"), "3.4.5.6.8"),
                kayttoOikeusRyhma("RYHMA")
        ));

        given(this.organisaatioClient.getOrganisaatioPerustiedotCached(eq("3.4.5.6.7"), anyObject()))
                .willReturn(Optional.of(creaetOrganisaatioPerustietoWithNimi("3.4.5.6.7", "nimiFi")));

        HenkilohakuCriteriaDto henkilohakuCriteriaDto = new HenkilohakuCriteriaDto(true, null,
                null, null, null, "3.4.5.6.7", null);
        List<HenkilohakuResultDto> henkilohakuResultDtoList = this.henkiloService.henkilohaku(henkilohakuCriteriaDto, 0L, OrderByHenkilohaku.HENKILO_NIMI_ASC);
        assertThat(henkilohakuResultDtoList).extracting(HenkilohakuResultDto::getOidHenkilo).containsExactly("1.2.3.4.5");
        assertThat(henkilohakuResultDtoList).flatExtracting(HenkilohakuResultDto::getOrganisaatioNimiList)
                .extracting(OrganisaatioMinimalDto::getLocalisedLabels)
                .extracting("fi")
                .containsExactly("nimiFi");
    }

    @Test
    @WithMockUser(value = "1.2.3.4.1", authorities = "ROLE_APP_HENKILONHALLINTA_OPHREKISTERI")
    public void henkilohakuAsAdminByKayttooikeusryhma() {
        MyonnettyKayttoOikeusRyhmaTapahtuma myonnettyKayttoOikeusRyhmaTapahtuma = populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.5"), "3.4.5.6.7"),
                kayttoOikeusRyhma("RYHMA")
        ));

        populate(myonnettyKayttoOikeus(
                organisaatioHenkilo(henkilo("1.2.3.4.6"), "3.4.5.6.8"),
                kayttoOikeusRyhma("RYHMA2")
        ));

        given(this.organisaatioClient.getOrganisaatioPerustiedotCached(eq("3.4.5.6.7"), anyObject()))
                .willReturn(Optional.of(creaetOrganisaatioPerustietoWithNimi("3.4.5.6.7", "nimiFi")));

        HenkilohakuCriteriaDto henkilohakuCriteriaDto = new HenkilohakuCriteriaDto(null, null,
                null, null, null, null,
                myonnettyKayttoOikeusRyhmaTapahtuma.getKayttoOikeusRyhma().getId());
        List<HenkilohakuResultDto> henkilohakuResultDtoList = this.henkiloService.henkilohaku(henkilohakuCriteriaDto, 0L, OrderByHenkilohaku.HENKILO_NIMI_ASC);
        assertThat(henkilohakuResultDtoList).extracting(HenkilohakuResultDto::getOidHenkilo).containsExactly("1.2.3.4.5");
        assertThat(henkilohakuResultDtoList).flatExtracting(HenkilohakuResultDto::getOrganisaatioNimiList)
                .extracting(OrganisaatioMinimalDto::getLocalisedLabels)
                .extracting("fi")
                .containsExactly("nimiFi");
    }


}
