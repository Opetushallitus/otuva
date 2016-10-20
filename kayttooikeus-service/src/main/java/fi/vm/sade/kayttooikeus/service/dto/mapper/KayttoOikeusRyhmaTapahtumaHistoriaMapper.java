package fi.vm.sade.kayttooikeus.service.dto.mapper;

import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhmaTapahtumaHistoria;
import fi.vm.sade.kayttooikeus.service.dto.LocaleTextDto;
import fi.vm.sade.kayttooikeus.service.dto.MyonnettyKayttoOikeusDTO;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.springframework.stereotype.Component;

@Component
public class KayttoOikeusRyhmaTapahtumaHistoriaMapper extends CustomMapper<KayttoOikeusRyhmaTapahtumaHistoria, MyonnettyKayttoOikeusDTO> {

    @Override
    public void mapAtoB(KayttoOikeusRyhmaTapahtumaHistoria kayttoOikeusRyhmaTapahtumaHistoria, MyonnettyKayttoOikeusDTO myonnettyKayttoOikeusDTO, MappingContext context) {
        myonnettyKayttoOikeusDTO.setRyhmaId(kayttoOikeusRyhmaTapahtumaHistoria.getKayttoOikeusRyhma().getId());
        myonnettyKayttoOikeusDTO.setMyonnettyTapahtumaId(kayttoOikeusRyhmaTapahtumaHistoria.getId());
        myonnettyKayttoOikeusDTO.setTehtavanimike("N/A");
        myonnettyKayttoOikeusDTO.setTila(kayttoOikeusRyhmaTapahtumaHistoria.getTila());
        myonnettyKayttoOikeusDTO.setKasittelijaOid(kayttoOikeusRyhmaTapahtumaHistoria.getKasittelija().getOidHenkilo());
        myonnettyKayttoOikeusDTO.setKasittelijaNimi("N/A");
        myonnettyKayttoOikeusDTO.setMuutosSyy(kayttoOikeusRyhmaTapahtumaHistoria.getSyy());

        myonnettyKayttoOikeusDTO.setTyyppi("KORyhma");
        myonnettyKayttoOikeusDTO.setKasitelty(kayttoOikeusRyhmaTapahtumaHistoria.getAikaleima());
        kayttoOikeusRyhmaTapahtumaHistoria.getKayttoOikeusRyhma().getDescription().getTexts().forEach(text ->
                myonnettyKayttoOikeusDTO.getRyhmaNames().add(new LocaleTextDto(text.getText(), text.getLang())));
    }
}
