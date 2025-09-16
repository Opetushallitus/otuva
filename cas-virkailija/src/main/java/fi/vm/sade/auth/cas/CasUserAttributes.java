package fi.vm.sade.auth.cas;

import java.util.List;
import java.util.Optional;

public record CasUserAttributes(
        String oidHenkilo,
        String username,
        List<Object> sessionindex,
        Optional<String> mfaProvider,
        Optional<String> kayttajaTyyppi,
        Optional<String> idpEntityId,
        List<String> roles,
        Optional<String> hakaRegistrationToken
) {
}
