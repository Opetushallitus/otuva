package fi.vm.sade.kayttooikeus.enumeration;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum KayttooikeusRooli {
    VASTUUKAYTTAJAT("VASTUUKAYTTAJAT"),
    ;

    String groupName;

    KayttooikeusRooli(String groupName) {
        this.groupName = groupName;
    }

}
