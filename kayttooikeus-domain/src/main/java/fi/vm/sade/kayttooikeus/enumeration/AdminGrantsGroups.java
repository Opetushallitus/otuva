package fi.vm.sade.kayttooikeus.enumeration;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum AdminGrantsGroups {
    REKISTERINPITAJA("Rekisterinpitäjä (vain OPHn käytössä)"),
    PAAKAYTTAJA_KK("Pääkäyttäjä (kk)"),
    KOULUTUSTOIMIJA_VASTUUKAYTTAJA("Koulutustoimijan vastuukäyttäjä"),
    KOULUTUSTOIMIJAN_VASTUUKAYTTAJA_AIKU("Koulutustoimijan vastuukäyttäjä (aiku)"),
    AIPAL_PAAKAYTTAJA("AIPAL-OPH:n pääkäyttäjä"),
    AITU_PAAKAYTTAJA("AITU-OPH:n pääkäyttäjä"),
    ARVO_AMK_PAAKAYTTAJA("ARVO-AMK-pääkäyttäjä"),
    ARVO_YO_PAAKAYTTAJA("ARVO-YO-pääkäyttäjä"),
    ARVO_YO_VASTUUKAYTTAJA("ARVO-YO-vastuukäyttäjä"),
    EPERUSTEET_PAAKAYTTAJA("ePerusteet OPH:n pääkäyttäjä"),
    KOODISTON_YLLAPITAJA("Koodiston ylläpitäjä"),
    KOSKI_PAAKAYTTAJA("koski-oph-pääkäyttäjä"),
    OIKEUSTULKKIREKISTERI_YLLAPITAJA("Oikeustulkkirekisterin ylläpitäjä"),
    OIVA_YLLAPITAJA("Oiva-ylläpitäjä"),
    VALTIIONAVUSTUS_PAAKAYTTAJA("Valtionavustus-pääkäyttäjä"),
    VIRANOMAIS_PAAKAYTTAJA("KELA-pääkäyttäjä"),
    ;

    String groupName;

    AdminGrantsGroups(String groupName) {
        this.groupName = groupName;
    }

    public static List<String> allValuesAsList() {
        return Arrays.stream(AdminGrantsGroups.values()).map(AdminGrantsGroups::getGroupName).collect(Collectors.toList());
    }
}
