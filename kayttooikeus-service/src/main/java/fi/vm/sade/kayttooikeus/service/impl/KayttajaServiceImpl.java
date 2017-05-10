package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.service.KayttajaService;
import fi.vm.sade.kayttooikeus.service.exception.DataInconsistencyException;
import fi.vm.sade.kayttooikeus.service.exception.UnauthorizedException;
import java.util.Collection;
import java.util.Optional;
import static java.util.stream.Collectors.toList;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class KayttajaServiceImpl implements KayttajaService {

    private static Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new UnauthorizedException("Käyttäjä ei ole kirjautunut");
        }
        return authentication;
    }

    @Override
    public String getOid() {
        Authentication authentication = getAuthentication();
        return Optional.ofNullable(authentication.getName())
                .orElseThrow(() -> new DataInconsistencyException("Käyttäjällä ei ole käyttäjänimeä"));
    }

    @Override
    public Collection<String> getRoolit() {
        Authentication authentication = getAuthentication();
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(toList());
    }

}
