package fi.vm.sade.kayttooikeus.service.validators;

import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloDto;
import org.springframework.util.StringUtils;

public abstract class KutsujaValidator {

    public abstract boolean isKutsujaYksiloity(String kutsujaOid);

    public static class Enabled extends KutsujaValidator {
        private final OppijanumerorekisteriClient oppijanumerorekisteriClient;

        public Enabled(OppijanumerorekisteriClient oppijanumerorekisteriClient) {
            this.oppijanumerorekisteriClient = oppijanumerorekisteriClient;
        }

        public boolean isKutsujaYksiloity(String kutsujaOid) {
            HenkiloDto kutsuja = this.oppijanumerorekisteriClient.getHenkiloByOid(kutsujaOid);
            return StringUtils.hasLength(kutsuja.getHetu()) && kutsuja.isYksiloityVTJ();
        }
    }

    public static class Disabled extends KutsujaValidator {
        public boolean isKutsujaYksiloity(String kutsujaOid) {
            return true;
        }
    }

}
