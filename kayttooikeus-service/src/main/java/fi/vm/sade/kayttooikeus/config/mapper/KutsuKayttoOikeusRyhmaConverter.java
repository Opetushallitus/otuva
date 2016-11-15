package fi.vm.sade.kayttooikeus.config.mapper;

import fi.vm.sade.kayttooikeus.dto.KutsuDto;
import fi.vm.sade.kayttooikeus.dto.TextGroupMapDto;
import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRyhmaRepository;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import org.springframework.stereotype.Component;

@Component
public class KutsuKayttoOikeusRyhmaConverter extends BidirectionalConverter<KayttoOikeusRyhma, KutsuDto.KayttoOikeusRyhmaDto> {

    private final KayttoOikeusRyhmaRepository kayttoOikeusRyhmaRepository;

    public KutsuKayttoOikeusRyhmaConverter(KayttoOikeusRyhmaRepository kayttoOikeusRyhmaRepository) {
        this.kayttoOikeusRyhmaRepository = kayttoOikeusRyhmaRepository;
    }

    @Override
    public KutsuDto.KayttoOikeusRyhmaDto convertTo(KayttoOikeusRyhma source, Type<KutsuDto.KayttoOikeusRyhmaDto> destinationType) {
        KutsuDto.KayttoOikeusRyhmaDto destination = new KutsuDto.KayttoOikeusRyhmaDto();
        destination.setId(source.getId());
        destination.setNimi(mapperFacade.map(source.getDescription(), TextGroupMapDto.class));
        return destination;
    }

    @Override
    public KayttoOikeusRyhma convertFrom(KutsuDto.KayttoOikeusRyhmaDto source, Type<KayttoOikeusRyhma> destinationType) {
        return kayttoOikeusRyhmaRepository.findById(source.getId())
                .orElseThrow(() -> new IllegalArgumentException("KayttoOikeusRyhma not found with id " + source.getId()));
    }

}
