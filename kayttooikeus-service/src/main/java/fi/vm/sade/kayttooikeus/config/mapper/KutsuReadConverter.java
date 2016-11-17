package fi.vm.sade.kayttooikeus.config.mapper;

import fi.vm.sade.kayttooikeus.dto.KutsuReadDto;
import fi.vm.sade.kayttooikeus.dto.TextGroupMapDto;
import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.model.Kutsu;
import fi.vm.sade.kayttooikeus.model.KutsuOrganisaatio;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.metadata.Type;
import org.springframework.stereotype.Component;

@Component
public class KutsuReadConverter extends CustomConverter<Kutsu, KutsuReadDto> {

    @Override
    public KutsuReadDto convert(Kutsu source, Type<? extends KutsuReadDto> destinationType) {
        KutsuReadDto destination = new KutsuReadDto();
        destination.setId(source.getId());
        destination.setSahkoposti(source.getSahkoposti());
        destination.setAsiointikieli(source.getKieliKoodi());
        destination.setOrganisaatiot(mapperFacade.mapAsSet(source.getOrganisaatiot(), KutsuReadDto.KutsuOrganisaatioDto.class));
        return destination;
    }

    @Component
    public class OrganisaatioConverter extends CustomConverter<KutsuOrganisaatio, KutsuReadDto.KutsuOrganisaatioDto> {

        @Override
        public KutsuReadDto.KutsuOrganisaatioDto convert(KutsuOrganisaatio source, Type<? extends KutsuReadDto.KutsuOrganisaatioDto> destinationType) {
            KutsuReadDto.KutsuOrganisaatioDto destination = new KutsuReadDto.KutsuOrganisaatioDto();
            destination.setOrganisaatioOid(source.getOrganisaatioOid());
            destination.setKayttoOikeusRyhmat(mapperFacade.mapAsSet(source.getRyhmat(), KutsuReadDto.KayttoOikeusRyhmaDto.class));
            return destination;
        }

    }

    @Component
    public class KayttoOikeusRyhmaConverter extends CustomConverter<KayttoOikeusRyhma, KutsuReadDto.KayttoOikeusRyhmaDto> {

        @Override
        public KutsuReadDto.KayttoOikeusRyhmaDto convert(KayttoOikeusRyhma source, Type<? extends KutsuReadDto.KayttoOikeusRyhmaDto> destinationType) {
            KutsuReadDto.KayttoOikeusRyhmaDto destination = new KutsuReadDto.KayttoOikeusRyhmaDto();
            destination.setId(source.getId());
            destination.setNimi(mapperFacade.map(source.getDescription(), TextGroupMapDto.class));
            return destination;
        }

    }

}
