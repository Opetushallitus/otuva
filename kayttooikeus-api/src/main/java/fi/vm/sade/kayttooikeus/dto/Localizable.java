package fi.vm.sade.kayttooikeus.dto;

import java.util.Optional;

import static java.util.Optional.ofNullable;

public interface Localizable {
    Long getId();

    Localizable put(String lang, String value);
    
    String get(String lang);
    
    Optional<String> getOrAny(String lang);
    
    static int compareLangs(Localizable a, Localizable b, String lang) {
        String at = ofNullable(a).map(l -> l.get(lang)).orElse(null),
                bt = ofNullable(b).map(l -> l.get(lang)).orElse(null);
        if (at == null && bt == null) {
            return 0;
        }
        if (at == null) {
            return 1;
        }
        if (bt == null) {
            return -1;
        }
        return at.compareTo(bt); 
    }
}
