package fi.vm.sade.kayttooikeus.service.external;

import lombok.Getter;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrganisaatioClient {
    List<String> getChildOids(String oid);

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

    List<OrganisaatioPerustieto> listWithoutRoot();

    List<OrganisaatioPerustieto> listActiveOganisaatioPerustiedotRecursiveCached(String organisaatioOid, Mode mode);

    List<OrganisaatioPerustieto> refreshCache();

    Optional<OrganisaatioPerustieto> getOrganisaatioPerustiedotCached(String oid, Mode mode);

    List<OrganisaatioPerustieto> listActiveOrganisaatioPerustiedotByOidRestrictionList(Collection<String> organisaatioOids);

    List<String> getParentOids(String oid);
}
