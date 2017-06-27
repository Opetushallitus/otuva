package fi.vm.sade.kayttooikeus.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IdentifierLocalisableLabelDto {
    String identifier;

    Map<String, String> localisedLabels = new HashMap<>();
}
