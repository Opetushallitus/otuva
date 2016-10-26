package fi.vm.sade.kayttooikeus.dto;

import java.util.Optional;

public interface Localizable {
    Long getId();

    Localizable put(String lang, String value);
    
    String get(String lang);
    
    Optional<String> getOrAny(String lang);
}
