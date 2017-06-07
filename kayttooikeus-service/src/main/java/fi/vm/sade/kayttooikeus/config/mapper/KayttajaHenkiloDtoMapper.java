package fi.vm.sade.kayttooikeus.config.mapper;

import fi.vm.sade.kayttooikeus.dto.YhteystietojenTyypit;
import fi.vm.sade.kayttooikeus.model.Kayttaja;
import fi.vm.sade.kayttooikeus.util.YhteystietoUtil;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloDto;
import fi.vm.sade.oppijanumerorekisteri.dto.KielisyysDto;
import fi.vm.sade.oppijanumerorekisteri.dto.YhteystiedotRyhmaDto;
import fi.vm.sade.oppijanumerorekisteri.dto.YhteystietoDto;
import fi.vm.sade.oppijanumerorekisteri.dto.YhteystietoTyyppi;
import static java.util.Collections.emptySet;
import java.util.Optional;
import java.util.Set;
import static java.util.stream.Collectors.toSet;
import java.util.stream.Stream;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.springframework.stereotype.Component;

@Component
public class KayttajaHenkiloDtoMapper extends CustomMapper<Kayttaja, HenkiloDto> {

    @Override
    public void mapBtoA(HenkiloDto b, Kayttaja a, MappingContext context) {
        a.setOid(b.getOidHenkilo());
        a.setEtunimet(b.getEtunimet());
        a.setKutsumanimi(b.getKutsumanimi());
        a.setSukunimi(b.getSukunimi());
        a.setKieliKoodi(createKieliKoodi(b.getAsiointiKieli()));
        a.setSahkoposti(createSahkoposti(b.getYhteystiedotRyhma()).orElseGet(() -> createOletusSahkoposti(a)));
    }

    private String createKieliKoodi(KielisyysDto kielisyys) {
        return kielisyys != null ? kielisyys.getKieliKoodi() : null;
    }

    private Optional<String> createSahkoposti(Set<YhteystiedotRyhmaDto> yhteystietoRyhmat) {
        return YhteystietoUtil.getYhteystietoArvo(yhteystietoRyhmat,
                YhteystietoTyyppi.YHTEYSTIETO_SAHKOPOSTI,
                YhteystietojenTyypit.PRIORITY_ORDER);
    }

    private String createOletusSahkoposti(Kayttaja kayttaja) {
        return kayttaja.getKayttajatunnus() + "@epavalidi.local";
    }

    @Override
    public void mapAtoB(Kayttaja a, HenkiloDto b, MappingContext context) {
        b.setOidHenkilo(a.getOid());
        b.setEtunimet(a.getEtunimet());
        b.setKutsumanimi(a.getKutsumanimi());
        b.setSukunimi(a.getSukunimi());
        b.setAsiointiKieli(createKielisyys(a.getKieliKoodi()));
        b.setYhteystiedotRyhma(createYhteystiedotRyhma(YhteystietoTyyppi.YHTEYSTIETO_SAHKOPOSTI, a.getSahkoposti()));
    }

    private KielisyysDto createKielisyys(String kieliKoodi) {
        return kieliKoodi != null ? new KielisyysDto(kieliKoodi, null) : null;
    }

    private Set<YhteystiedotRyhmaDto> createYhteystiedotRyhma(YhteystietoTyyppi tyyppi, String arvo) {
        if (arvo == null) {
            return emptySet();
        }
        YhteystietoDto yhteystieto = YhteystietoDto.builder()
                .yhteystietoTyyppi(tyyppi)
                .yhteystietoArvo(arvo)
                .build();
        YhteystiedotRyhmaDto yhteystiedotRyhma = YhteystiedotRyhmaDto.builder()
                .yhteystieto(yhteystieto)
                .build();
        return Stream.of(yhteystiedotRyhma).collect(toSet());
    }

}
