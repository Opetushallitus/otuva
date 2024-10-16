package fi.vm.sade.kayttooikeus.client.viestinvalitys;

import lombok.Data;

@Data
public class ApiResponse {
    private final Integer status;
    private final String body;
}
