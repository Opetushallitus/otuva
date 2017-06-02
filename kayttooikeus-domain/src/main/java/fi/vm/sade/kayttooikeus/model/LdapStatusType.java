package fi.vm.sade.kayttooikeus.model;

import java.util.Arrays;

public enum LdapStatusType {

    IN_QUEUE(0),
    RETRY(1),
    FAILED(2);

    private final int dbData;

    private LdapStatusType(int dbData) {
        this.dbData = dbData;
    }

    public int getDbData() {
        return dbData;
    }

    public static LdapStatusType fromDbData(int dbData) {
        return Arrays.stream(values())
                .filter(type -> type.dbData == dbData)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tuntematon LdapStatusType dbData " + dbData));
    }

}
