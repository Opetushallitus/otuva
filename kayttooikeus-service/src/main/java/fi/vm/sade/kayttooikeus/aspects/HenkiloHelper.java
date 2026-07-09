package fi.vm.sade.kayttooikeus.aspects;

import fi.vm.sade.auditlog.Changes;
import fi.vm.sade.auditlog.Target;
import fi.vm.sade.kayttooikeus.dto.KayttajatiedotCreateDto;
import fi.vm.sade.kayttooikeus.dto.KayttajatiedotUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class HenkiloHelper {

    private final AuditLogger auditLogger;

    public void logPassivoiHenkilo(String henkiloOid, String kasittelijaOid, Object returnHenkilo) {
        Target target = new Target.Builder()
                .setField("oid", henkiloOid)
                .build();
        Changes changes = new Changes.Builder()
                .build();
        auditLogger.log(KayttooikeusOperation.PASSIVOI_HENKILO, target, changes);
    }

    void logChangePassword(String henkiloOid, String password, Object result) {
        Target target = new Target.Builder()
                .setField("oid", henkiloOid)
                .build();
        Changes changes = new Changes.Builder()
                .build();
        auditLogger.log(KayttooikeusOperation.CHANGE_PASSWORD, target, changes);
    }

    void logUpdateHakaTunnisteet(String henkiloOid, String ipdKey, Set<String> hakatunnisteet, Object result) {
        Target target = new Target.Builder()
                .setField("oid", henkiloOid)
                .build();
        Changes changes = new Changes.Builder()
                .build();
        auditLogger.log(KayttooikeusOperation.UPDATE_HAKATUNNISTEET, target, changes);
    }

    void logCreateKayttajatiedot(String henkiloOid, KayttajatiedotCreateDto kayttajatiedot, Object result) {
        Target target = new Target.Builder()
                .setField("oid", henkiloOid)
                .build();
        Changes changes = new Changes.Builder()
                .build();
        auditLogger.log(KayttooikeusOperation.CREATE_KAYTTAJATIEDOT, target, changes);
    }

    void logUpdateKayttajatiedot(String henkiloOid, KayttajatiedotUpdateDto kayttajatiedot, Object result) {
        Target target = new Target.Builder()
                .setField("oid", henkiloOid)
                .build();
        Changes changes = new Changes.Builder()
                .build();
        auditLogger.log(KayttooikeusOperation.UPDATE_KAYTTAJATIEDOT, target, changes);
    }

    public void logEnableGauthMfa(String henkiloOid) {
        Target target = new Target.Builder()
                .setField("oid", henkiloOid)
                .build();
        Changes changes = new Changes.Builder()
                .build();
        auditLogger.log(KayttooikeusOperation.ENABLE_MFA_GAUTH, target, changes);
    }

    public void logDisableGauthMfa(String henkiloOid) {
        Target target = new Target.Builder()
                .setField("oid", henkiloOid)
                .build();
        Changes changes = new Changes.Builder()
                .build();
        auditLogger.log(KayttooikeusOperation.DISABLE_MFA_GAUTH, target, changes);
    }
}
