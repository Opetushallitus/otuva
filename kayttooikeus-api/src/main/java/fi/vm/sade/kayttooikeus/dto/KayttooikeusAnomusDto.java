package fi.vm.sade.kayttooikeus.dto;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Email;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class KayttooikeusAnomusDto {

    @NotNull
    private String organisaatioOrRyhmaOid;

    private String tehtavaNimike;

    @Email
    private String email;

    @NotNull
    @Size(min=1)
    private List<Long> kayttooikeusRyhmaIds;

    private String perustelut;

}
