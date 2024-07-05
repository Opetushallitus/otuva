package fi.vm.sade.kayttooikeus.config.security;

import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.List;

public class OpintopolkuUserDetailsService implements AuthenticationUserDetailsService<CasAssertionAuthenticationToken> {
    @Override
    public UserDetails loadUserDetails(CasAssertionAuthenticationToken token) throws UsernameNotFoundException {
        var attributes = token.getAssertion().getPrincipal().getAttributes();
        var roles = (List<String>) attributes.getOrDefault("roles", List.of());
        return new OpintopolkuUserDetailsl((String) attributes.get("oidHenkilo"), roles);
    }

    public static final class OpintopolkuUserDetailsl implements UserDetails {
        private final String oidHenkilo;
        private final List<SimpleGrantedAuthority> authorities;

        public OpintopolkuUserDetailsl(String oidHenkilo, List<String> authorities) {
            this.oidHenkilo = oidHenkilo;
            this.authorities = authorities.stream().map(SimpleGrantedAuthority::new).toList();
        }

        @Override
        public Collection<SimpleGrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getPassword() {
            return null;
        }

        @Override
        public String getUsername() {
            return oidHenkilo;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}
