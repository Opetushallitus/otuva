package fi.vm.sade.kayttooikeus.model;

import java.util.Arrays;

public enum LdapPriorityType {

    BATCH(0),
    ASAP(1),
    NORMAL(2),
    NIGHT(3);

    private final int dbData;

    private LdapPriorityType(int dbData) {
        this.dbData = dbData;
    }

    public int getDbData() {
        return dbData;
    }

    public static LdapPriorityType fromDbData(int dbData) {
        return Arrays.stream(values())
                .filter(type -> type.dbData == dbData)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tuntematon LdapPriorityType dbData " + dbData));
    }

}
