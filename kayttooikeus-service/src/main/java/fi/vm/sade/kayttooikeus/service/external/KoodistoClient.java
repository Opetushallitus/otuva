package fi.vm.sade.kayttooikeus.service.external;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public interface KoodistoClient {
    List<KoodiArvoDto> listKoodisto(String koodisto);
    
    @Getter @Setter
    class KoodiArvoDto {
        private String koodiArvo;
        private List<KoodiMetadataDto> metadata = new ArrayList<KoodiMetadataDto>();
    }
    
    @Getter @Setter
    class KoodiMetadataDto {
        private String kieli; // uppercase
        private String nimi;
        private String kuvaus;
        private String lyhytNimi;
    }
}
