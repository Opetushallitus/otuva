package fi.vm.sade.kayttooikeus.controller;


import fi.vm.sade.auditlog.ApplicationType;
import fi.vm.sade.auditlog.Audit;
import fi.vm.sade.auditlog.henkilo.HenkiloOperation;
import org.springframework.stereotype.Component;

import static fi.vm.sade.auditlog.henkilo.LogMessage.builder;

@Component
public class AccessRightAuditLogger {
    private final String SERVICE_NAME = "kayttooikeus-service";

    private Audit audit = new Audit(SERVICE_NAME, ApplicationType.VIRKAILIJA);

    public void auditModifyAccessRightGroupData(String currentUserOid, String targetGroup, boolean createEvent) {
        String modificationType = createEvent ? "luonti" : "päivitys";
        audit.log(builder()
                .id(currentUserOid)
                .add("ryhmaId", targetGroup)
                .tapahtumatyyppi(modificationType)
                .setOperaatio(HenkiloOperation.KAYTTAJAOIKEUS_MUUTOS)
                .build());
    }

}
