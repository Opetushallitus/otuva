package fi.vm.sade.kayttooikeus.service;

import static fi.vm.sade.kayttooikeus.repositories.populate.KayttoOikeusRyhmaPopulator.kayttoOikeusRyhma;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloKayttoOikeusPopulator.myonnettyKayttoOikeus;
import static fi.vm.sade.kayttooikeus.repositories.populate.OrganisaatioHenkiloPopulator.organisaatioHenkilo;
import static org.assertj.core.api.Assertions.assertThat;

import fi.vm.sade.kayttooikeus.DatabaseService;
import fi.vm.sade.kayttooikeus.dto.KayttoOikeudenTila;
import fi.vm.sade.kayttooikeus.model.MyonnettyKayttoOikeusRyhmaTapahtuma;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

import org.hibernate.envers.RevisionType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
class AuditTablesTest {
    private static final int FIVE_YEARS_IN_DAYS = 5 * 365;

    @Autowired
    private AuditCleanupService auditCleanupService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private DatabaseService databaseService;
    @PersistenceContext
    private EntityManager em;

    @AfterEach
    void cleanup() {
        databaseService.truncate();
    }

    @Test
    void auditTableIsPopulatedOnInsert() {
        assertThatAuditTableIsEmpty();

        var tapahtuma = createMyonnettyKayttoOikeus();

        assertThatAuditTableContainsAddFor(tapahtuma);
    }

    @Test
    void auditTableIsPopulatedOnUpdate() {
        assertThatAuditTableIsEmpty();

        var tapahtuma = createMyonnettyKayttoOikeus();
        var updated = updateMyonnettyKayttoOikeus(tapahtuma);
        var revision = latestRevision();

        assertThatAuditTableContainsUpdateFor(revision, updated);
    }

    @Test
    void auditTableIsPrunedOfDataOlderThanFiveYears() {
        createMyonnettyKayttoOikeus();
        var oldRevision = latestRevision();
        createMyonnettyKayttoOikeus();
        var newRevision = latestRevision();

        assertThat(oldRevision).isLessThan(newRevision);
        assertThatAuditTableContainsRowsForRevision(oldRevision);
        assertThatAuditTableContainsRowsForRevision(newRevision);

        ageRevision(oldRevision, Duration.ofDays(FIVE_YEARS_IN_DAYS + 1));
        auditCleanupService.cleanup();

        assertThatAuditTableIsEmptyForRevision(oldRevision);
        assertThatAuditTableContainsRowsForRevision(newRevision);
    }

    @Test
    void auditTableRetainsDataLessThanFiveYearsOld() {
        createMyonnettyKayttoOikeus();
        var revision = latestRevision();

        ageRevision(revision, Duration.ofDays(FIVE_YEARS_IN_DAYS - 1));
        auditCleanupService.cleanup();

        assertThatAuditTableContainsRowsForRevision(revision);
    }

    private MyonnettyKayttoOikeusRyhmaTapahtuma createMyonnettyKayttoOikeus() {
        return databaseService.populate(
                myonnettyKayttoOikeus(
                        organisaatioHenkilo("henkilo-" + System.nanoTime(), "organisaatio-" + System.nanoTime()),
                        kayttoOikeusRyhma("ryhma-" + System.nanoTime()))
                        .voimassaPaattyen(LocalDate.now().plusYears(1)));
    }

    private MyonnettyKayttoOikeusRyhmaTapahtuma updateMyonnettyKayttoOikeus(
            MyonnettyKayttoOikeusRyhmaTapahtuma tapahtuma) {
        return new TransactionTemplate(transactionManager).execute(status -> {
            var managed = em.find(MyonnettyKayttoOikeusRyhmaTapahtuma.class, tapahtuma.getId());
            managed.setTila(KayttoOikeudenTila.SULJETTU);
            managed.setSyy("suljettu testissa");
            managed.setVoimassaLoppuPvm(LocalDate.now().minusDays(1));
            return managed;
        });
    }

    private void assertThatAuditTableIsEmpty() {
        assertThat(jdbcTemplate.queryForList("SELECT * FROM myonnetty_kayttooikeusryhma_tapahtuma_aud")).isEmpty();
        assertThat(jdbcTemplate.queryForList("SELECT * FROM revinfo")).isEmpty();
    }

    private void assertThatAuditTableContainsAddFor(MyonnettyKayttoOikeusRyhmaTapahtuma tapahtuma) {
        var row = jdbcTemplate.queryForMap(
                "SELECT id, kayttooikeusryhma_id, organisaatiohenkilo_id, kasittelija_henkilo_id,"
                        + " tila, syy, voimassaalkupvm, voimassaloppupvm, revtype"
                        + " FROM myonnetty_kayttooikeusryhma_tapahtuma_aud WHERE id = ?",
                tapahtuma.getId());
        assertThat(row.get("id")).isEqualTo(tapahtuma.getId());
        assertThat(row.get("kayttooikeusryhma_id")).isEqualTo(tapahtuma.getKayttoOikeusRyhma().getId());
        assertThat(row.get("organisaatiohenkilo_id")).isEqualTo(tapahtuma.getOrganisaatioHenkilo().getId());
        assertThat(row.get("kasittelija_henkilo_id")).isEqualTo(tapahtuma.getKasittelija().getId());
        assertThat(row.get("tila")).isEqualTo(tapahtuma.getTila().name());
        assertThat(row.get("syy")).isEqualTo(tapahtuma.getSyy());
        assertThat(((Date) row.get("voimassaalkupvm")).toLocalDate())
                .isEqualTo(tapahtuma.getVoimassaAlkuPvm());
        assertThat(((Date) row.get("voimassaloppupvm")).toLocalDate())
                .isEqualTo(tapahtuma.getVoimassaLoppuPvm());
        assertThat(revisionTypeOf(row)).isEqualTo(RevisionType.ADD);
    }

    private void assertThatAuditTableContainsUpdateFor(
            long revision, MyonnettyKayttoOikeusRyhmaTapahtuma tapahtuma) {
        var row = jdbcTemplate.queryForMap(
                "SELECT id, tila, syy, voimassaloppupvm, revtype"
                        + " FROM myonnetty_kayttooikeusryhma_tapahtuma_aud WHERE id = ? AND rev = ?",
                tapahtuma.getId(), revision);
        assertThat(row.get("id")).isEqualTo(tapahtuma.getId());
        assertThat(row.get("tila")).isEqualTo(tapahtuma.getTila().name());
        assertThat(row.get("syy")).isEqualTo(tapahtuma.getSyy());
        assertThat(((Date) row.get("voimassaloppupvm")).toLocalDate())
                .isEqualTo(tapahtuma.getVoimassaLoppuPvm());
        assertThat(revisionTypeOf(row)).isEqualTo(RevisionType.MOD);
    }

    private void assertThatAuditTableContainsRowsForRevision(long revision) {
        assertThat(countAuditRows("myonnetty_kayttooikeusryhma_tapahtuma_aud", revision)).isPositive();
        assertThat(countRevinfoRows(revision)).isEqualTo(1);
    }

    private void assertThatAuditTableIsEmptyForRevision(long revision) {
        assertThat(countAuditRows("myonnetty_kayttooikeusryhma_tapahtuma_aud", revision)).isZero();
        assertThat(countRevinfoRows(revision)).isZero();
    }

    private long latestRevision() {
        return jdbcTemplate.queryForObject("SELECT MAX(rev) FROM revinfo", Long.class);
    }

    private RevisionType revisionTypeOf(java.util.Map<String, Object> row) {
        return RevisionType.fromRepresentation(((Number) row.get("revtype")).byteValue());
    }

    private void ageRevision(long revision, Duration age) {
        var agedMillis = Instant.now().minus(age).toEpochMilli();
        jdbcTemplate.update("UPDATE revinfo SET revtstmp = ? WHERE rev = ?", agedMillis, revision);
    }

    private int countAuditRows(String table, long revision) {
        return jdbcTemplate.queryForObject(
                "SELECT count(*) FROM " + table + " WHERE rev = ?", Integer.class, revision);
    }

    private int countRevinfoRows(long revision) {
        return jdbcTemplate.queryForObject(
                "SELECT count(*) FROM revinfo WHERE rev = ?", Integer.class, revision);
    }
}
