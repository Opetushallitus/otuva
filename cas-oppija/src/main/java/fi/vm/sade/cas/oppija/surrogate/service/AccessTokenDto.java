package fi.vm.sade.cas.oppija.surrogate.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import fi.vm.sade.cas.oppija.surrogate.SurrogateBaseDto;

class AccessTokenDto extends SurrogateBaseDto {

    @JsonProperty("access_token")
    public String accessToken;

}
