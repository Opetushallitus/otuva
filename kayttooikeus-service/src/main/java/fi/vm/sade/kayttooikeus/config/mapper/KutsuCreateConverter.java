package fi.vm.sade.kayttooikeus.config.mapper;

import fi.vm.sade.kayttooikeus.dto.KutsuCreateDto;
import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.model.Kutsu;
import fi.vm.sade.kayttooikeus.model.KutsuOrganisaatio;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRyhmaRepository;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.metadata.Type;
import org.springframework.stereotype.Component;

@Component
public class KutsuCreateConverter extends CustomConverter<KutsuCreateDto, Kutsu> {

    @Override
    public Kutsu convert(KutsuCreateDto source, Type<? extends Kutsu> destinationType) {
        Kutsu destination = new Kutsu();
        destination.setSahkoposti(source.getSahkoposti());
        destination.setKieliKoodi(source.getAsiointikieli());
        destination.setOrganisaatiot(mapperFacade.mapAsSet(source.getOrganisaatiot(), KutsuOrganisaatio.class));
        return destination;
    }

    @Component
    public class OrganisaatioConverter extends CustomConverter<KutsuCreateDto.KutsuOrganisaatioDto, KutsuOrganisaatio> {

        @Override
        public KutsuOrganisaatio convert(KutsuCreateDto.KutsuOrganisaatioDto source, Type<? extends KutsuOrganisaatio> destinationType) {
            KutsuOrganisaatio entity = new KutsuOrganisaatio();
            entity.setOrganisaatioOid(source.getOrganisaatioOid());
            entity.setRyhmat(mapperFacade.mapAsSet(source.getKayttoOikeusRyhmat(), KayttoOikeusRyhma.class));
            return entity;
        }

    }

    @Component
    public class KayttoOikeusRyhmaConverter extends CustomConverter<KutsuCreateDto.KayttoOikeusRyhmaDto, KayttoOikeusRyhma> {

        private final KayttoOikeusRyhmaRepository kayttoOikeusRyhmaRepository;

        public KayttoOikeusRyhmaConverter(KayttoOikeusRyhmaRepository kayttoOikeusRyhmaRepository) {
            this.kayttoOikeusRyhmaRepository = kayttoOikeusRyhmaRepository;
        }

        @Override
        public KayttoOikeusRyhma convert(KutsuCreateDto.KayttoOikeusRyhmaDto source, Type<? extends KayttoOikeusRyhma> destinationType) {
            return kayttoOikeusRyhmaRepository.findById(source.getId())
                    .orElseThrow(() -> new IllegalArgumentException("KayttoOikeusRyhma not found with id " + source.getId()));
        }

    }

}
