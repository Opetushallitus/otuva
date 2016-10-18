package fi.vm.sade.kayttooikeus.service.dto;

public interface Localizable {
    Long getId();
    
    void put(String lang, String value);
    
    String get(String lang);
}
