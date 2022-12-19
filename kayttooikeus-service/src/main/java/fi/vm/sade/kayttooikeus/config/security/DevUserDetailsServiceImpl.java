package fi.vm.sade.kayttooikeus.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import fi.vm.sade.kayttooikeus.repositories.KayttajatiedotRepository;
import fi.vm.sade.kayttooikeus.repositories.MyonnettyKayttoOikeusRyhmaTapahtumaRepository;

import static java.util.stream.Collectors.toSet;

@Profile("dev")
@Configuration
public class DevUserDetailsServiceImpl extends UserDetailsServiceImpl {
  private final PasswordEncoder passwordEncoder;

  @Autowired
  public DevUserDetailsServiceImpl(KayttajatiedotRepository kayttajatiedotRepository, MyonnettyKayttoOikeusRyhmaTapahtumaRepository repository, PasswordEncoder passwordEncoder) {
    super(kayttajatiedotRepository, repository);
    this.passwordEncoder = passwordEncoder;
  }

  private static final String PASSWORD = "password";

  @Override
  public UserDetails loadUserByUsername(String oid) throws UsernameNotFoundException {
    return User.builder()
        .authorities(streamRooliByKayttajaOid(oid).map(SimpleGrantedAuthority::new).collect(toSet()))
        .password(this.passwordEncoder.encode(PASSWORD))
        .username(oid)
        .build();
  }
}
