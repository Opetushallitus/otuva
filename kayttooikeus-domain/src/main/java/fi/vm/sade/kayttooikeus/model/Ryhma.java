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
    private Set<String> jasenet;

    public boolean isEmpty() {
        if (jasenet == null) {
            return true;
        }
        return jasenet.isEmpty();
    }

    public boolean addJasen(String jasen) {
        if (jasenet == null) {
            jasenet = new HashSet<>();
        }
        return jasenet.add(jasen);
    }

    public boolean deleteJasen(String jasen) {
        if (jasenet == null) {
            return false;
        }
        return jasenet.remove(jasen);
    }

}
