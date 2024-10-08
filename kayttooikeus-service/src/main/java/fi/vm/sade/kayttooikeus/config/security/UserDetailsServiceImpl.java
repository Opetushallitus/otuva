package fi.vm.sade.kayttooikeus.config.security;

import fi.vm.sade.kayttooikeus.dto.OrganisaatioPalveluRooliDto;
import fi.vm.sade.kayttooikeus.repositories.KayttajatiedotRepository;
import fi.vm.sade.kayttooikeus.repositories.MyonnettyKayttoOikeusRyhmaTapahtumaRepository;
import fi.vm.sade.kayttooikeus.service.KayttajarooliProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * {@link UserDetailsService}-toteutus joka muodostaa käyttäjän roolit tietokannasta.
 *
 */
@Component
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService, KayttajarooliProvider {

    private final KayttajatiedotRepository kayttajatiedotRepository;
    private final MyonnettyKayttoOikeusRyhmaTapahtumaRepository myonnettyKayttoOikeusRyhmaTapahtumaRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String oid = kayttajatiedotRepository.findOidByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("Käyttäjää ei löytynyt käyttäjätunnuksella %s", username)));
        Set<GrantedAuthority> roolit = streamRooliByKayttajaOid(oid).map(SimpleGrantedAuthority::new).collect(toSet());
        return new UserDetailsImpl(oid, roolit);
    }

    @Override
    public Set<String> getByKayttajaOid(String kayttajaOid) {
        return streamRooliByKayttajaOid(kayttajaOid).collect(toSet());
    }

    Stream<String> streamRooliByKayttajaOid(String kayttajaOid) {
        return myonnettyKayttoOikeusRyhmaTapahtumaRepository.findOrganisaatioPalveluRooliByOid(kayttajaOid)
                .stream()
                .flatMap(UserDetailsServiceImpl::getRoolit)
                .map(String::toUpperCase);
    }

    @Override
    public Map<String, Set<String>> getRolesByOrganisation(String kayttajaOid) {
        var roles = new HashMap<String, Set<String>>();
        myonnettyKayttoOikeusRyhmaTapahtumaRepository.findOrganisaatioPalveluRooliByOid(kayttajaOid)
            .stream()
            .forEach(dto -> {
                var role = dto.getPalvelu() + "_" + dto.getRooli();
                var cur = roles.get(dto.getOrganisaatioOid());
                if (cur == null) {
                    cur = new HashSet<String>();
                }
                cur.add(role);
                roles.put(dto.getOrganisaatioOid(), cur);
            });
        return roles;
    }

    private static Stream<String> getRoolit(OrganisaatioPalveluRooliDto dto) {
        Set<String> roolit = new HashSet<>();

        StringBuilder builder = new StringBuilder("ROLE_APP_");
        // ROLE_APP_<palvelu>
        builder.append(dto.getPalvelu());
        roolit.add(builder.toString());

        // ROLE_APP_<palvelu>_<kayttooikeus_rooli>
        builder.append("_");
        builder.append(dto.getRooli());
        roolit.add(builder.toString());

        // ROLE_APP_<palvelu>_<kayttooikeus_rooli>_<organisaatiooid>
        builder.append("_");
        builder.append(dto.getOrganisaatioOid());
        roolit.add(builder.toString());

        return roolit.stream();
    }

    @Getter
    @AllArgsConstructor
    private static class UserDetailsImpl implements UserDetails {

        private final String username;
        private final Collection<? extends GrantedAuthority> authorities;
        private final boolean accountNonExpired;
        private final boolean accountNonLocked;
        private final boolean credentialsNonExpired;
        private final boolean enabled;

        public UserDetailsImpl(String username, Collection<? extends GrantedAuthority> authorities) {
            this(username, authorities, true, true, true, true);
        }

        @Override
        public String getPassword() {
            // Instanssia käytetään vain roolien lataamiseen (CasAuthenticationProvider) joten salasanaa ei tarvita
            return null;
        }

    }

}
