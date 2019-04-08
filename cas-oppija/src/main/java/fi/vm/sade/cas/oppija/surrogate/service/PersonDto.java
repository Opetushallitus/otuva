package fi.vm.sade.cas.oppija.surrogate.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import fi.vm.sade.cas.oppija.surrogate.SurrogateBaseDto;

class PersonDto extends SurrogateBaseDto {

    @JsonProperty("personId")
    public String nationalIdentificationNumber;
    public String name;

}
