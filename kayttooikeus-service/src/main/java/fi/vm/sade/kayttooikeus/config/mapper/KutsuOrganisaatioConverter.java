package fi.vm.sade.kayttooikeus.config.mapper;

import fi.vm.sade.kayttooikeus.dto.KutsuDto;
import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.model.KutsuOrganisaatio;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import org.springframework.stereotype.Component;

@Component
public class KutsuOrganisaatioConverter extends BidirectionalConverter<KutsuOrganisaatio, KutsuDto.KutsuOrganisaatioDto> {

    @Override
    public KutsuDto.KutsuOrganisaatioDto convertTo(KutsuOrganisaatio source, Type<KutsuDto.KutsuOrganisaatioDto> destinationType) {
        KutsuDto.KutsuOrganisaatioDto destination = new KutsuDto.KutsuOrganisaatioDto();
        destination.setOrganisaatioOid(source.getOrganisaatioOid());
        destination.setKayttoOikeusRyhmat(mapperFacade.mapAsSet(source.getRyhmat(), KutsuDto.KayttoOikeusRyhmaDto.class));
        return destination;
    }

    @Override
    public KutsuOrganisaatio convertFrom(KutsuDto.KutsuOrganisaatioDto source, Type<KutsuOrganisaatio> destinationType) {
        KutsuOrganisaatio entity = new KutsuOrganisaatio();
        entity.setOrganisaatioOid(source.getOrganisaatioOid());
        entity.setRyhmat(mapperFacade.mapAsSet(source.getKayttoOikeusRyhmat(), KayttoOikeusRyhma.class));
        return entity;
    }

}
