package fi.vm.sade.kayttooikeus.export;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@RequiredArgsConstructor
@Service
public class ExportService {
    @Value("${kayttooikeus.tasks.export.bucket-name}")
    private String bucketName;
    @Value("${kayttooikeus.tasks.export.lampi-bucket-name}")
    private String lampiBucketName;
    @Value("${kayttooikeus.tasks.export.upload-to-s3:true}")
    private boolean uploadToS3;

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void createSchema() {
        jdbcTemplate.execute("DROP SCHEMA IF EXISTS exportnew CASCADE");
        jdbcTemplate.execute("CREATE SCHEMA exportnew");
        jdbcTemplate.execute("DROP SCHEMA IF EXISTS export CASCADE");
        jdbcTemplate.execute("ALTER SCHEMA exportnew RENAME TO export");
    }

    public void generateExportFiles() throws IOException {
    }

    public void copyExportFilesToLampi() throws IOException {
    }
}
