package fi.vm.sade.kayttooikeus.model;

import java.util.HashSet;
import java.util.Set;
import javax.naming.Name;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

/**
 * Ryhmän tiedot LDAP:ssa.
 *
 * @see Kayttaja käyttäjän tiedot
 */
@Entry(base = "ou=groups", objectClasses = {"groupOfUniqueNames"})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public final class Ryhma {

    @Id
    private Name dn;

    @Attribute(name = "cn")
    @DnAttribute(value = "cn", index = 1)
    private String nimi;

    @Attribute(name = "uniqueMember")
    private Set<String> kayttajat;

    public boolean isEmpty() {
        if (kayttajat == null) {
            return true;
        }
        return kayttajat.isEmpty();
    }

    public boolean addKayttaja(String kayttajaDn) {
        if (kayttajat == null) {
            kayttajat = new HashSet<>();
        }
        return kayttajat.add(kayttajaDn);
    }

    public boolean deleteKayttaja(String kayttajaDn) {
        if (kayttajat == null) {
            return false;
        }
        return kayttajat.remove(kayttajaDn);
    }

}
