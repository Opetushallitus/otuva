package fi.vm.sade.kayttooikeus.config.mapper;

import fi.vm.sade.kayttooikeus.dto.KutsuDto;
import fi.vm.sade.kayttooikeus.model.Kutsu;
import fi.vm.sade.kayttooikeus.model.KutsuOrganisaatio;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import org.springframework.stereotype.Component;

@Component
public class KutsuConverter extends BidirectionalConverter<Kutsu, KutsuDto> {

    @Override
    public KutsuDto convertTo(Kutsu source, Type<KutsuDto> destinationType) {
        KutsuDto destination = new KutsuDto();
        destination.setId(source.getId());
        destination.setSahkoposti(source.getSahkoposti());
        destination.setAsiointikieli(source.getKieliKoodi());
        destination.setOrganisaatiot(mapperFacade.mapAsSet(source.getOrganisaatiot(), KutsuDto.KutsuOrganisaatioDto.class));
        return destination;
    }

    @Override
    public Kutsu convertFrom(KutsuDto source, Type<Kutsu> destinationType) {
        Kutsu destination = new Kutsu();
        destination.setId(source.getId());
        destination.setSahkoposti(source.getSahkoposti());
        destination.setKieliKoodi(source.getAsiointikieli());
        destination.setOrganisaatiot(mapperFacade.mapAsSet(source.getOrganisaatiot(), KutsuOrganisaatio.class));
        return destination;
    }

}
