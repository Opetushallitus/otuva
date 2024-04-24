package fi.vm.sade.kayttooikeus.export;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExportTask {
    private final ExportService exportService;

    @Value("${kayttooikeus.tasks.export.copy-to-lampi}")
    private boolean copyToLampi;

    public void execute() {
        try {
            log.info("Running kayttooikeus export task");
            exportService.createSchema();
            exportService.generateExportFiles();
            if (copyToLampi) {
                exportService.copyExportFilesToLampi();
            } else {
                log.info("Copying export files to Lampi is disabled");
            }
            log.info("Kayttooikeus export task completed");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
