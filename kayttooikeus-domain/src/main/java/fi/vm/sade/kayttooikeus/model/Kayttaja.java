package fi.vm.sade.kayttooikeus.model;

import java.util.Objects;
import static java.util.stream.Collectors.joining;
import java.util.stream.Stream;
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
 * Käyttäjän tiedot LDAP:ssa.
 *
 * @see Ryhma ryhmän tiedot
 */
@Entry(base = "ou=People", objectClasses = {"person", "inetOrgPerson"})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"salasana"})
public final class Kayttaja {

    @Id
    private Name dn;

    /* kayttooikeuden tietoja */
    @Attribute(name = "uid")
    @DnAttribute(value = "uid", index = 1)
    private String kayttajatunnus;
    @Attribute(name = "userPassword", type = Attribute.Type.BINARY)
    private byte[] salasana;
    @Attribute(name = "description")
    private String roolit;

    /* oppijanumerorekisterin tietoja */
    @Attribute(name = "employeeNumber")
    private String oid;
    @Attribute(name = "givenName")
    private String etunimet;
    @Attribute(name = "cn")
    private String kutsumanimi;
    @Attribute(name = "sn")
    private String sukunimi;
    @Attribute(name = "preferredLanguage")
    private String kieliKoodi;
    @Attribute(name = "mail")
    private String sahkoposti;

    public String getDnAsString() {
        return "uid=" + kayttajatunnus + ",ou=People,dc=opintopolku,dc=fi";
    }

    public String getNimi() {
        return Stream.of(etunimet, sukunimi).filter(Objects::nonNull).collect(joining(" "));
    }

}
