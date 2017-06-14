package fi.vm.sade.kayttooikeus.config.mapper;

import fi.vm.sade.kayttooikeus.dto.AnomusHakuDto;
import fi.vm.sade.kayttooikeus.dto.TextGroupMapDto;
import fi.vm.sade.kayttooikeus.model.Anomus;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MappingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnomusHakuMappers {

    @Bean
    public Mapper<Anomus, AnomusHakuDto> anomusHakuMapper() {
        return new CustomMapper<Anomus, AnomusHakuDto>() {
            @Override
            public void mapAtoB(Anomus a, AnomusHakuDto b, MappingContext context) {
                b.setAnoja(mapperFacade.map(a.getHenkilo(), AnomusHakuDto.HenkiloDto.class));
                b.setHaetutKayttoOikeusRyhmat(mapperFacade.mapAsList(a.getHaettuKayttoOikeusRyhmas(), AnomusHakuDto.HaettuKayttoOikeusRyhmaDto.class));
            }
        };
    }

    @Bean
    public Mapper anomusHakuHenkiloMapper() {
        return new CustomMapper<Henkilo, AnomusHakuDto.HenkiloDto>() {
            @Override
            public void mapAtoB(Henkilo a, AnomusHakuDto.HenkiloDto b, MappingContext context) {
                b.setOid(a.getOidHenkilo());
                b.setEtunimet(a.getEtunimetCached());
                b.setSukunimi(a.getSukunimiCached());
                b.setKayttajatunnus(a.getKayttajatiedot() != null ? a.getKayttajatiedot().getUsername() : null);
            }
        };
    }

    @Bean
    public Mapper anomusHakuHaettuKayttoOikeusRyhmaMapper() {
        return new CustomMapper<KayttoOikeusRyhma, AnomusHakuDto.KayttoOikeusRyhmaDto>() {
            @Override
            public void mapAtoB(KayttoOikeusRyhma a, AnomusHakuDto.KayttoOikeusRyhmaDto b, MappingContext context) {
                b.setNimi(mapperFacade.map(a.getDescription(), TextGroupMapDto.class));
            }
        };
    }

}
