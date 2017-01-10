package fi.vm.sade.kayttooikeus.service.external;

import lombok.Getter;

import java.util.Collection;
import java.util.List;

public interface OrganisaatioClient {
    @Getter
    class Mode {
        private final boolean expectMultiple;
        private boolean changeChecked;

        Mode(boolean expectMultiple) {
            this.expectMultiple = expectMultiple;
        }

        public static Mode single() {
            return new Mode(false);
        }
        
        public static Mode multiple() {
            return new Mode(true);
        }

        public static Mode requireCache() {
            return new Mode(true);
        }

        public Mode checked() {
            this.changeChecked = true;
            return this;
        }
    }
    
    List<OrganisaatioPerustieto> listActiveOganisaatioPerustiedotRecursiveCached(String organisaatioOid, Mode mode);

    OrganisaatioPerustieto getOrganisaatioPerustiedotCached(String oid, Mode mode);

    List<OrganisaatioPerustieto> listActiveOganisaatioPerustiedotByOidRestrictionList(Collection<String> organisaatioOids);
}
