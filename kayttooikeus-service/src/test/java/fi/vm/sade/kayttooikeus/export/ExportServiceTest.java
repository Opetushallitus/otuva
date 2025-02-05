package fi.vm.sade.kayttooikeus.export;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Sql("/truncate_tables.sql")
@Sql("/test-data.sql")
@SpringBootTest
class ExportServiceTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired ExportService exportService;
    @Autowired JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void before() {
        exportService.disableUploadToS3ForTests();
    }

    @Test
    void exportSchemaIsCreated() {
        exportService.createSchema();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM export.kayttooikeus", Long.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM export.kayttooikeusryhma", Long.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM export.kayttooikeusryhma_kayttooikeus", Long.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM export.henkilo_kayttooikeusryhma", Long.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM export.henkilo_kayttooikeusryhma_historia", Long.class)).isEqualTo(3);
    }

    @Test
    void jsonExport() throws IOException {
        exportService.createSchema();
        var files = exportService.generateJsonExports();
        assertThat(files).hasSize(5);

        var kayttooikeus = objectMapper.readValue(files.get(0), new TypeReference<List<Kayttooikeus>>(){});
        assertThat(kayttooikeus).hasSize(3);
        var kayttooikeusryhma = objectMapper.readValue(files.get(1), new TypeReference<List<Kayttooikeusryhma>>(){});
        assertThat(kayttooikeusryhma).hasSize(2);
        var kayttooikeusryhmaRelaatio = objectMapper.readValue(files.get(2), new TypeReference<List<KayttooikeusryhmaRelaatio>>(){});
        assertThat(kayttooikeusryhmaRelaatio).hasSize(3);
        var henkiloKayttooikeusryhma = objectMapper.readValue(files.get(3), new TypeReference<List<HenkiloKayttooikeusryhma>>(){});
        assertThat(henkiloKayttooikeusryhma).hasSize(4);
        var henkiloKayttooikeusryhmaHistoria = objectMapper.readValue(files.get(4), new TypeReference<List<HenkiloKayttooikeusryhmaHistoria>>(){});
        assertThat(henkiloKayttooikeusryhmaHistoria).hasSize(3);
    }
}