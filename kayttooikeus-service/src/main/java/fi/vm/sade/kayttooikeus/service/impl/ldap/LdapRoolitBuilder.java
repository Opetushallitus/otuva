package fi.vm.sade.kayttooikeus.service.impl.ldap;

import fi.vm.sade.kayttooikeus.model.Identification;
import fi.vm.sade.kayttooikeus.model.Kayttajatiedot;
import fi.vm.sade.kayttooikeus.model.KayttoOikeus;
import fi.vm.sade.kayttooikeus.model.MyonnettyKayttoOikeusRyhmaTapahtuma;
import fi.vm.sade.kayttooikeus.dto.KayttajaTyyppi;
import fi.vm.sade.oppijanumerorekisteri.dto.KielisyysDto;
import static java.util.Collections.unmodifiableSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import static java.util.stream.Collectors.joining;
import static java.util.stream.StreamSupport.stream;

/**
 * Apuluokka LDAP-roolien muodostamiseen.
 */
public final class LdapRoolitBuilder {

    private static final String ROOLI_KAYTTAJA_TEMPLATE = "USER_%s";
    private static final String ROOLI_KIELI_TEMPLATE = "LANG_%s";
    private static final String ROOLI_VANHA_KIRJAUTUMINEN_TEMPLATE = "STRONG_AUTHENTICATED_%s";
    private static final String ROOLI_VAHVA_KIRJAUTUMINEN = "STRONG_AUTHENTICATED";
    private static final String ROOLI_PALVELU_PREFIX = "APP_";

    private final SortedSet<String> roolit;

    public LdapRoolitBuilder() {
        this.roolit = new TreeSet<>();
    }

    public LdapRoolitBuilder kayttajatiedot(Kayttajatiedot kayttajatiedot) {
        if (kayttajatiedot != null) {
            kayttajatunnus(kayttajatiedot.getUsername());
        }
        return this;
    }

    public LdapRoolitBuilder kayttajatunnus(String kayttajatunnus) {
        if (kayttajatunnus != null) {
            roolit.add(String.format(ROOLI_KAYTTAJA_TEMPLATE, kayttajatunnus));
        }
        return this;
    }

    public LdapRoolitBuilder myonnetyt(List<MyonnettyKayttoOikeusRyhmaTapahtuma> myonnetyt) {
        myonnetyt.forEach(this::myonnetty);
        return this;
    }

    public LdapRoolitBuilder myonnetty(MyonnettyKayttoOikeusRyhmaTapahtuma myonnetty) {
        myonnetty.getKayttoOikeusRyhma().getKayttoOikeus().stream()
                .forEach(kayttoOikeus -> myonnetty(kayttoOikeus, myonnetty));
        return this;
    }

    private void myonnetty(KayttoOikeus kayttoOikeus, MyonnettyKayttoOikeusRyhmaTapahtuma myonnetty) {
        StringBuilder builder = new StringBuilder(ROOLI_PALVELU_PREFIX);

        // APP_<palvelu>
        builder.append(kayttoOikeus.getPalvelu().getName());
        roolit.add(builder.toString());

        // APP_<palvelu>_<kayttooikeus_rooli>
        builder.append("_");
        builder.append(kayttoOikeus.getRooli());
        roolit.add(builder.toString());

        // APP_<palvelu>_<kayttooikeus_rooli>_<organisaatiooid>
        builder.append("_");
        builder.append(myonnetty.getOrganisaatioHenkilo().getOrganisaatioOid());
        roolit.add(builder.toString());
    }

    public LdapRoolitBuilder identifications(Iterable<Identification> identifications) {
        long count = stream(identifications.spliterator(), false)
                .filter(Identification::isVahvaTunniste)
                .map(identification -> roolit.add(String.format(ROOLI_VANHA_KIRJAUTUMINEN_TEMPLATE, identification.getIdpEntityId().toUpperCase())))
                .count();
        if (count > 0) {
            roolit.add(ROOLI_VAHVA_KIRJAUTUMINEN);
        }
        return this;
    }

    public LdapRoolitBuilder kayttajaTyyppi(KayttajaTyyppi tyyppi) {
        if (tyyppi != null) {
            roolit.add(tyyppi.name());
        }
        return this;
    }

    public LdapRoolitBuilder asiointikieli(KielisyysDto asiointikieli) {
        if (asiointikieli != null) {
            kieliKoodi(asiointikieli.getKieliKoodi());
        }
        return this;
    }

    public LdapRoolitBuilder kieliKoodi(String kieliKoodi) {
        if (kieliKoodi != null) {
            roolit.add(String.format(ROOLI_KIELI_TEMPLATE, kieliKoodi));
        }
        return this;
    }

    public Set<String> asSet() {
        return unmodifiableSet(new TreeSet<>(roolit));
    }

    public String asString() {
        return roolit.stream()
                .map(rooli -> "\"" + rooli + "\"")
                .collect(joining(", ", "[", "]"));
    }

}
