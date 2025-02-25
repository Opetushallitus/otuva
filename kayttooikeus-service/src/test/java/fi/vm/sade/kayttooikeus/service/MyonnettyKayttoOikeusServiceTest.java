package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.DatabaseService;
import fi.vm.sade.kayttooikeus.dto.KayttoOikeudenTila;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhmaTapahtumaHistoria;
import fi.vm.sade.kayttooikeus.model.OrganisaatioHenkilo;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepository;
import fi.vm.sade.kayttooikeus.repositories.OrganisaatioHenkiloRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static fi.vm.sade.kayttooikeus.repositories.populate.HenkiloPopulator.henkilo;
import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusRyhmaPopulator.kayttoOikeusRyhma;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloKayttoOikeusPopulator.myonnettyKayttoOikeus;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloPopulator.organisaatioHenkilo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest
public class MyonnettyKayttoOikeusServiceTest {

    @Autowired
    private MyonnettyKayttoOikeusService myonnettyKayttoOikeusService;

    @Autowired
    private HenkiloDataRepository henkiloDataRepository;

    @Autowired
    private OrganisaatioHenkiloRepository organisaatioHenkiloRepository;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    public void cleanup() {
        databaseService.truncate();
    }

    @Test
    public void poistaVanhentuneet() throws Exception {
        databaseService.populate(henkilo("kayttaja"));
        databaseService.populate(myonnettyKayttoOikeus(organisaatioHenkilo("henkilo1", "organisaatio1"),
                kayttoOikeusRyhma("ryhmä1")).voimassaPaattyen(LocalDate.now().minusDays(1)));
        databaseService.populate(myonnettyKayttoOikeus(organisaatioHenkilo("henkilo1", "organisaatio1"),
                kayttoOikeusRyhma("ryhmä2")).voimassaPaattyen(LocalDate.now().minusDays(1)));
        databaseService.populate(myonnettyKayttoOikeus(organisaatioHenkilo("henkilo1", "organisaatio2"),
                kayttoOikeusRyhma("ryhmä1")));
        databaseService.populate(myonnettyKayttoOikeus(organisaatioHenkilo("henkilo2", "organisaatio1"),
                kayttoOikeusRyhma("ryhmä1")));

        Henkilo kayttaja = henkiloDataRepository.findByOidHenkilo("kayttaja").get();
        myonnettyKayttoOikeusService.poistaVanhentuneet(new MyonnettyKayttoOikeusService.DeleteDetails(
                kayttaja, KayttoOikeudenTila.SULJETTU, "suljettu testissä"));

        databaseService.runInTransaction(() -> {
            OrganisaatioHenkilo henkilo1organisaatio1 = organisaatioHenkiloRepository
                    .findByHenkiloOidHenkiloAndOrganisaatioOid("henkilo1", "organisaatio1").get();
            assertThat(henkilo1organisaatio1.isPassivoitu()).isTrue();
            assertThat(henkilo1organisaatio1.getMyonnettyKayttoOikeusRyhmas()).isEmpty();
            assertThat(henkilo1organisaatio1.getKayttoOikeusRyhmaHistorias())
                    .extracting(historia -> historia.getKayttoOikeusRyhma().getTunniste(),
                            KayttoOikeusRyhmaTapahtumaHistoria::getTila, KayttoOikeusRyhmaTapahtumaHistoria::getSyy)
                    .containsExactlyInAnyOrder(
                            tuple("ryhmä1", KayttoOikeudenTila.SULJETTU, "suljettu testissä"),
                            tuple("ryhmä2", KayttoOikeudenTila.SULJETTU, "suljettu testissä"));
            OrganisaatioHenkilo henkilo1organisaatio2 = organisaatioHenkiloRepository
                    .findByHenkiloOidHenkiloAndOrganisaatioOid("henkilo1", "organisaatio2").get();
            assertThat(henkilo1organisaatio2.isPassivoitu()).isFalse();
            assertThat(henkilo1organisaatio2.getMyonnettyKayttoOikeusRyhmas())
                    .extracting(myonnetty -> myonnetty.getKayttoOikeusRyhma().getTunniste())
                    .containsExactlyInAnyOrder("ryhmä1");
            assertThat(henkilo1organisaatio2.getKayttoOikeusRyhmaHistorias()).isEmpty();
            OrganisaatioHenkilo henkilo2organisaatio1 = organisaatioHenkiloRepository
                    .findByHenkiloOidHenkiloAndOrganisaatioOid("henkilo2", "organisaatio1").get();
            assertThat(henkilo2organisaatio1.isPassivoitu()).isFalse();
            assertThat(henkilo2organisaatio1.getMyonnettyKayttoOikeusRyhmas())
                    .extracting(myonnetty -> myonnetty.getKayttoOikeusRyhma().getTunniste())
                    .containsExactlyInAnyOrder("ryhmä1");
            assertThat(henkilo2organisaatio1.getKayttoOikeusRyhmaHistorias()).isEmpty();
        });
    }

    @Test
    public void passivoi() {
        databaseService.populate(henkilo("kayttaja"));
        databaseService.populate(myonnettyKayttoOikeus(organisaatioHenkilo("henkilo1", "organisaatio1"),
                kayttoOikeusRyhma("ryhmä1")));
        databaseService.populate(myonnettyKayttoOikeus(organisaatioHenkilo("henkilo1", "organisaatio1"),
                kayttoOikeusRyhma("ryhmä2")));
        databaseService.populate(myonnettyKayttoOikeus(organisaatioHenkilo("henkilo1", "organisaatio2"),
                kayttoOikeusRyhma("ryhmä1")));
        databaseService.populate(myonnettyKayttoOikeus(organisaatioHenkilo("henkilo2", "organisaatio1"),
                kayttoOikeusRyhma("ryhmä1")));

        databaseService.runInTransaction(() -> {
            Henkilo kayttaja = henkiloDataRepository.findByOidHenkilo("kayttaja").get();
            OrganisaatioHenkilo organisaatioHenkilo = organisaatioHenkiloRepository
                    .findByHenkiloOidHenkiloAndOrganisaatioOid("henkilo1", "organisaatio1").get();
            myonnettyKayttoOikeusService.passivoi(organisaatioHenkilo, new MyonnettyKayttoOikeusService.DeleteDetails(
                    kayttaja, KayttoOikeudenTila.SULJETTU, "suljettu testissä"));
        });

        databaseService.runInTransaction(() -> {
            OrganisaatioHenkilo henkilo1organisaatio1 = organisaatioHenkiloRepository
                    .findByHenkiloOidHenkiloAndOrganisaatioOid("henkilo1", "organisaatio1").get();
            assertThat(henkilo1organisaatio1.isPassivoitu()).isTrue();
            assertThat(henkilo1organisaatio1.getMyonnettyKayttoOikeusRyhmas()).isEmpty();
            assertThat(henkilo1organisaatio1.getKayttoOikeusRyhmaHistorias())
                    .extracting(historia -> historia.getKayttoOikeusRyhma().getTunniste(),
                            KayttoOikeusRyhmaTapahtumaHistoria::getTila, KayttoOikeusRyhmaTapahtumaHistoria::getSyy)
                    .containsExactlyInAnyOrder(
                            tuple("ryhmä1", KayttoOikeudenTila.SULJETTU, "suljettu testissä"),
                            tuple("ryhmä2", KayttoOikeudenTila.SULJETTU, "suljettu testissä"));
            OrganisaatioHenkilo henkilo1organisaatio2 = organisaatioHenkiloRepository
                    .findByHenkiloOidHenkiloAndOrganisaatioOid("henkilo1", "organisaatio2").get();
            assertThat(henkilo1organisaatio2.isPassivoitu()).isFalse();
            assertThat(henkilo1organisaatio2.getMyonnettyKayttoOikeusRyhmas())
                    .extracting(myonnetty -> myonnetty.getKayttoOikeusRyhma().getTunniste())
                    .containsExactlyInAnyOrder("ryhmä1");
            assertThat(henkilo1organisaatio2.getKayttoOikeusRyhmaHistorias()).isEmpty();
            OrganisaatioHenkilo henkilo2organisaatio1 = organisaatioHenkiloRepository
                    .findByHenkiloOidHenkiloAndOrganisaatioOid("henkilo2", "organisaatio1").get();
            assertThat(henkilo2organisaatio1.isPassivoitu()).isFalse();
            assertThat(henkilo2organisaatio1.getMyonnettyKayttoOikeusRyhmas())
                    .extracting(myonnetty -> myonnetty.getKayttoOikeusRyhma().getTunniste())
                    .containsExactlyInAnyOrder("ryhmä1");
            assertThat(henkilo2organisaatio1.getKayttoOikeusRyhmaHistorias()).isEmpty();
        });
    }

    private List<MyonnettyKayttooikeusAuditRow> getAuditTrail() {
        RowMapper<MyonnettyKayttooikeusAuditRow> auditRowMapper = (rs, rowNum) -> new MyonnettyKayttooikeusAuditRow(
            rs.getLong("id"),
            rs.getLong("revtype"),
            rs.getLong("kasittelija_henkilo_id"),
            rs.getLong("organisaatiohenkilo_id"),
            rs.getLong("kayttooikeusryhma_id"),
            rs.getDate("voimassaalkupvm"),
            rs.getDate("voimassaloppupvm"),
            rs.getString("tila"),
            rs.getString("syy")
        );
        return jdbcTemplate.query(auditSql, auditRowMapper);
    }

    private String auditSql = "select * from myonnetty_kayttooikeusryhma_tapahtuma_aud";

    @Test
    public void poistaVanhentuneetLeavesEnversAuditTrail() throws Exception {
        databaseService.populate(henkilo("kayttaja"));
        databaseService.populate(myonnettyKayttoOikeus(organisaatioHenkilo("henkilo1", "organisaatio1"),
                kayttoOikeusRyhma("ryhmä1")).voimassaPaattyen(LocalDate.now().minusDays(1)));
        databaseService.populate(myonnettyKayttoOikeus(organisaatioHenkilo("henkilo1", "organisaatio1"),
                kayttoOikeusRyhma("ryhmä2")).voimassaPaattyen(LocalDate.now().minusDays(1)));

        databaseService.runInTransaction(() -> {
                OrganisaatioHenkilo henkilo1organisaatio = organisaatioHenkiloRepository
                        .findByHenkiloOidHenkiloAndOrganisaatioOid("henkilo1", "organisaatio1").get();

                List<MyonnettyKayttooikeusAuditRow> audit = getAuditTrail();
                assertThat(audit.size()).isEqualTo(2);
                assertThat(audit.stream().filter(a -> a.revtype() == 0).toList().size()).isEqualTo(2);
                assertThat(audit).allMatch(a -> a.organisaatioHenkiloId().equals(henkilo1organisaatio.getId()));
                assertThat(audit).allMatch(a -> a.voimassaalkupvm().toString().equals(LocalDate.now().toString()));
                assertThat(audit).allMatch(a -> a.voimassaloppupvm().toString().equals(LocalDate.now().minusDays(1).toString()));
                assertThat(audit).allMatch(a -> "MYONNETTY".equals(a.tila()));
        });

        Henkilo kayttaja = henkiloDataRepository.findByOidHenkilo("kayttaja").get();
        myonnettyKayttoOikeusService.poistaVanhentuneet(new MyonnettyKayttoOikeusService.DeleteDetails(
                kayttaja, KayttoOikeudenTila.SULJETTU, "suljettu testissä"));

        List<MyonnettyKayttooikeusAuditRow> auditAfterPoista = getAuditTrail();
        assertThat(auditAfterPoista.size()).isEqualTo(4);
        assertThat(auditAfterPoista.stream().filter(a -> a.revtype() == 2).toList().size()).isEqualTo(2);
    }


}

record MyonnettyKayttooikeusAuditRow(
        Long id,
        Long revtype,
        Long kasittelijaHenkiloId,
        Long organisaatioHenkiloId,
        Long kayttooikeusryhmaId,
        Date voimassaalkupvm,
        Date voimassaloppupvm,
        String tila,
        String syy
) {}
