package fi.vm.sade.kayttooikeus.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import fi.vm.sade.kayttooikeus.dto.OrganisaatioPalveluRooliDto;
import fi.vm.sade.kayttooikeus.repositories.MyonnettyKayttoOikeusRyhmaTapahtumaRepository;
import fi.vm.sade.kayttooikeus.service.KayttajarooliProvider;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

import java.util.HashSet;

@Profile("dev")
@Configuration
@RequiredArgsConstructor
public class DevUserDetailsServiceImpl implements UserDetailsService, KayttajarooliProvider {

  private final MyonnettyKayttoOikeusRyhmaTapahtumaRepository myonnettyKayttoOikeusRyhmaTapahtumaRepository;
  private final PasswordEncoder passwordEncoder;

  private static final String PASSWORD = "password";

  @Override
  public UserDetails loadUserByUsername(String oid) throws UsernameNotFoundException {
    return User.builder()
        .authorities(streamRooliByKayttajaOid(oid).map(SimpleGrantedAuthority::new).collect(toSet()))
        .password(this.passwordEncoder.encode(PASSWORD))
        .username(oid)
        .build();
  }

  @Override
  public Set<String> getByKayttajaOid(String kayttajaOid) {
      return streamRooliByKayttajaOid(kayttajaOid).collect(toSet());
  }

  private Stream<String> streamRooliByKayttajaOid(String kayttajaOid) {
      return myonnettyKayttoOikeusRyhmaTapahtumaRepository.findOrganisaatioPalveluRooliByOid(kayttajaOid)
              .stream()
              .flatMap(DevUserDetailsServiceImpl::getRoolit)
              .map(String::toUpperCase);
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
}
