package fi.vm.sade.kayttooikeus;

import fi.vm.sade.kayttooikeus.dto.KayttajaTyyppi;
import fi.vm.sade.kayttooikeus.dto.KayttajatiedotReadDto;
import fi.vm.sade.kayttooikeus.dto.MfaProvider;
import fi.vm.sade.kayttooikeus.model.Identification;
import fi.vm.sade.kayttooikeus.model.Kayttajatiedot;

import java.util.List;
import java.util.Optional;

public record CasUserAttributes(
        String oidHenkilo,
        String username,
        MfaProvider mfaProvider,
        KayttajaTyyppi kayttajaTyyppi,
        String idpEntityId,
        List<String> roles
) {
    public static CasUserAttributes fromIdentification(Identification identification, List<String> roles) {
        var h = identification.getHenkilo();
        var kt = Optional.ofNullable(h.getKayttajatiedot());
        var username = kt.map(Kayttajatiedot::getUsername).orElse(null);
        var mfaProvider = kt.map(Kayttajatiedot::getMfaProvider).orElse(null);
        return new CasUserAttributes(
                h.getOidHenkilo(),
                username,
                mfaProvider,
                h.getKayttajaTyyppi(),
                identification.getIdpEntityId(),
                roles
        );
    }

    public static CasUserAttributes fromKayttajatiedot(Kayttajatiedot kt, List<String> roles) {
        var h = kt.getHenkilo();
        return new CasUserAttributes(
                kt.getHenkilo().getOidHenkilo(),
                kt.getUsername(),
                kt.getMfaProvider(),
                h.getKayttajaTyyppi(),
                null,
                roles
        );
    }

    public static CasUserAttributes fromKayttajatiedotReadDto(String oid, KayttajatiedotReadDto kt, List<String> roles) {
        return new CasUserAttributes(
                oid,
                kt.getUsername(),
                kt.getMfaProvider(),
                kt.getKayttajaTyyppi(),
                null,
                roles
        );
    }
}
