package fi.vm.sade.kayttooikeus.config.mapper;

import fi.vm.sade.kayttooikeus.model.Kutsu;
import fi.vm.sade.kayttooikeus.service.impl.email.SahkopostiHenkiloDto;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KutsuMappers {

    @Bean
    public CustomMapper<Kutsu, SahkopostiHenkiloDto> sahkopostiHenkiloDtoClassMap() {
        return new CustomMapper<Kutsu, SahkopostiHenkiloDto>() {
            @Override
            public void mapAtoB(Kutsu a, SahkopostiHenkiloDto b, MappingContext context) {
                b.setEtunimet(a.getEtunimi());
                b.setKutsumanimi(a.getEtunimi());
                b.setSukunimi(a.getSukunimi());
            }
        };
    }

}
