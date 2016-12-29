package fi.vm.sade.kayttooikeus.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupListTypeDto {
    protected List<GroupTypeDto> group;
}
