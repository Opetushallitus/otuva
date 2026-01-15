package fi.vm.sade.kayttooikeus.aspects;

import fi.vm.sade.auditlog.Changes;
import fi.vm.sade.auditlog.Target;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrganisaatioHenkiloHelper {

    private final AuditLogger auditLogger;

    void logPassivoiOrganisaatioHenkilo(String oidHenkilo, String henkiloOrganisationOid, Object result) {
        Target target = new Target.Builder()
                .setField("henkiloOid", oidHenkilo)
                .setField("organisaatioOid", henkiloOrganisationOid)
                .build();
        Changes changes = new Changes.Builder()
                .build();
        auditLogger.log(KayttooikeusOperation.PASSIVOI_ORGANISAATIO_HENKILO, target, changes);
    }

}
