package fi.vm.sade.kayttooikeus.util;

import com.google.common.collect.Lists;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioPerustieto;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloDto;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloPerustietoDto;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static java.util.Optional.ofNullable;

public class UserDetailsUtil {

    private static final String DEFAULT_LANGUAGE_CODE = "fi";

    public static String getCurrentUserOid() throws NullPointerException {
        String oid = SecurityContextHolder.getContext().getAuthentication().getName();
        if (oid == null) {
            throw new NullPointerException("No user name available from SecurityContext!");
        }
        return oid;
    }

    /**
     * Returns name for given {@link HenkiloDto}.
     *
     * @param henkilo
     * @return name
     */
    public static String getName(HenkiloDto henkilo) {
        return henkilo.getKutsumanimi() + " " + henkilo.getSukunimi();
    }

    /**
     * Palauttaa {@link HenkiloDto henkilön} asiointikielen (jos ei löydy niin
     * palautetaan {@link #DEFAULT_LANGUAGE_CODE}).
     *
     * @param henkilo henkilö
     * @param kielikoodit sallitut kielikoodit (tyhjä hyväksyy kaikki)
     * @return kielikoodi
     */
    public static String getLanguageCode(HenkiloDto henkilo, String... kielikoodit) {
        List<String> kielikoodilista = Arrays.asList(kielikoodit);
        return ofNullable(henkilo.getAsiointiKieli())
                .flatMap(kielisyys -> ofNullable(kielisyys.getKieliKoodi()))
                .filter(kielikoodi -> kielikoodilista.isEmpty() || kielikoodilista.contains(kielikoodi))
                .orElse(DEFAULT_LANGUAGE_CODE);
    }

    /**
     * Palauttaa {@link HenkiloPerustietoDto henkilön} asiointikielen (jos ei
     * löydy niin palautetaan {@link #DEFAULT_LANGUAGE_CODE}).
     *
     * @param henkilo henkilö
     * @param kielikoodit sallitut kielikoodit
     * @return kielikoodi
     */
    public static String getLanguageCode(HenkiloPerustietoDto henkilo, String... kielikoodit) {
        List<String> kielikoodilista = Arrays.asList(kielikoodit);
        return ofNullable(henkilo.getAsiointiKieli())
                .flatMap(kielisyys -> ofNullable(kielisyys.getKieliKoodi()))
                .filter(kielikoodi -> kielikoodilista.isEmpty() || kielikoodilista.contains(kielikoodi))
                .orElse(DEFAULT_LANGUAGE_CODE);
    }

    public static OrganisaatioPerustieto createUnknownOrganisation(String organisaatioOid) {
        return new OrganisaatioPerustieto().toBuilder()
                .oid(organisaatioOid)
                .nimi(new HashMap<String, String>() {{
                    put("fi", "Tuntematon organisaatio");
                    put("sv", "Okänd organisation");
                    put("en", "Unknown organisation");
                }})
                .tyypit(Lists.newArrayList())
                .build();
    }
}
