package fi.vm.sade.kayttooikeus.service.dto.mapper;

import fi.vm.sade.kayttooikeus.model.MyonnettyKayttoOikeusRyhmaTapahtuma;
import fi.vm.sade.kayttooikeus.service.dto.LocaleTextDto;
import fi.vm.sade.kayttooikeus.service.dto.MyonnettyKayttoOikeusDTO;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.springframework.stereotype.Component;

@Component
public class MyonnettyKayttoOikeusRyhmaTapahtumaMapper extends CustomMapper<MyonnettyKayttoOikeusRyhmaTapahtuma, MyonnettyKayttoOikeusDTO> {

    @Override
    public void mapAtoB(MyonnettyKayttoOikeusRyhmaTapahtuma myonnettyKayttoOikeusRyhmaTapahtuma, MyonnettyKayttoOikeusDTO myonnettyKayttoOikeusDTO, MappingContext context) {
        myonnettyKayttoOikeusDTO.setRyhmaId(myonnettyKayttoOikeusRyhmaTapahtuma.getKayttoOikeusRyhma().getId());
        myonnettyKayttoOikeusDTO.setMyonnettyTapahtumaId(myonnettyKayttoOikeusRyhmaTapahtuma.getId());
        myonnettyKayttoOikeusDTO.setTila(myonnettyKayttoOikeusRyhmaTapahtuma.getTila());
        myonnettyKayttoOikeusDTO.setKasittelijaOid(myonnettyKayttoOikeusRyhmaTapahtuma.getKasittelija().getOidHenkilo());
        myonnettyKayttoOikeusDTO.setKasittelijaNimi("N/A");
        myonnettyKayttoOikeusDTO.setMuutosSyy(myonnettyKayttoOikeusRyhmaTapahtuma.getSyy());

        if( myonnettyKayttoOikeusRyhmaTapahtuma.getOrganisaatioHenkilo() != null ){
            myonnettyKayttoOikeusDTO.setTehtavanimike(myonnettyKayttoOikeusRyhmaTapahtuma.getOrganisaatioHenkilo().getTehtavanimike());
            myonnettyKayttoOikeusDTO.setOrganisaatioOid(myonnettyKayttoOikeusRyhmaTapahtuma.getOrganisaatioHenkilo().getOrganisaatioOid());
        }

        myonnettyKayttoOikeusDTO.setTyyppi("KORyhma");
        myonnettyKayttoOikeusDTO.setAlkuPvm(myonnettyKayttoOikeusRyhmaTapahtuma.getVoimassaAlkuPvm());
        myonnettyKayttoOikeusDTO.setVoimassaPvm(myonnettyKayttoOikeusRyhmaTapahtuma.getVoimassaLoppuPvm());
        myonnettyKayttoOikeusDTO.setKasitelty(myonnettyKayttoOikeusRyhmaTapahtuma.getAikaleima());
        myonnettyKayttoOikeusRyhmaTapahtuma.getKayttoOikeusRyhma().getDescription().getTexts().forEach(text ->
                myonnettyKayttoOikeusDTO.getRyhmaNames().add(new LocaleTextDto(text.getText(), text.getLang())));
    }
}
