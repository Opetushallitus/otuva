package fi.vm.sade.kayttooikeus.dto;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RyhmanHenkilotDto implements Serializable {
    private List<String> personOids;
}
