package fi.vm.sade.kayttooikeus.config.mapper;

import fi.vm.sade.kayttooikeus.dto.KayttoOikeusRyhmaDto;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioViiteDto;
import fi.vm.sade.kayttooikeus.dto.TextGroupDto;
import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.metadata.Type;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class KayttoOikeusRyhmaConverter extends CustomConverter<KayttoOikeusRyhma, KayttoOikeusRyhmaDto> {

    @Override
    public KayttoOikeusRyhmaDto convert(KayttoOikeusRyhma source, Type<? extends KayttoOikeusRyhmaDto> destinationType) {
        return KayttoOikeusRyhmaDto.builder()
                .id(source.getId())
                .name(source.getName())
                .rooliRajoite(source.getRooliRajoite())
                .description(new TextGroupDto(source.getDescription().getId()))
                .organisaatioViite(source.getOrganisaatioViite().stream().map(organisaatioViite -> OrganisaatioViiteDto.builder()
                        .id(organisaatioViite.getId())
                        .organisaatioTyyppi(organisaatioViite.getOrganisaatioTyyppi())
                        .build()).collect(Collectors.toList()))
                .build();
    }

}
