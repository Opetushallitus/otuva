package fi.vm.sade.kayttooikeus.repositories.impl;

import fi.vm.sade.kayttooikeus.repositories.RyhmaRepositoryCustom;
import java.util.Set;
import static java.util.stream.Collectors.toSet;
import lombok.RequiredArgsConstructor;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;

@RequiredArgsConstructor
public class RyhmaRepositoryImpl implements RyhmaRepositoryCustom {

    private final LdapTemplate ldapTemplate;

    @Override
    public Set<String> findNimiByKayttaja(String kayttajaDn) {
        return ldapTemplate.search("ou=groups",
                new EqualsFilter("uniqueMember", kayttajaDn).encode(),
                (Object ctx) -> ((DirContextOperations) ctx).getStringAttribute("cn"))
                .stream().collect(toSet());
    }

}
