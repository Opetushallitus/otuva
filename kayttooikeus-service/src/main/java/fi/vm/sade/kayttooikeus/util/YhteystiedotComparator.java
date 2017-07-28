package fi.vm.sade.kayttooikeus.util;

import fi.vm.sade.oppijanumerorekisteri.dto.YhteystiedotRyhmaDto;

import fi.vm.sade.kayttooikeus.dto.YhteystietojenTyypit;

import java.util.Comparator;

/**
 * Eri yhteystietoryhmien tyypin perusteella saadaan ryhmiteltyä osoitteet
 * helpottaa viestin lähetyksen priorisointia
 */
public class YhteystiedotComparator implements Comparator<YhteystiedotRyhmaDto> {
    public YhteystiedotComparator() {}

    @Override
    public int compare(YhteystiedotRyhmaDto o1, YhteystiedotRyhmaDto o2) {
        if (getPriority(o1)>getPriority(o2)){
            return 1;
        }
        if (getPriority(o1)<getPriority(o2)){
            return -1;
        }
        return 0;
    }

    private int getPriority(YhteystiedotRyhmaDto yht) {
        if (yht.getRyhmaKuvaus().equals(YhteystietojenTyypit.TYOOSOITE)) return 1;
        if (yht.getRyhmaKuvaus().equals(YhteystietojenTyypit.KOTIOSOITE)) return 2;
        if (yht.getRyhmaKuvaus().equals(YhteystietojenTyypit.MUU_OSOITE)) return 3;
        if (yht.getRyhmaKuvaus().equals(YhteystietojenTyypit.VAPAA_AJAN_OSOITE)) return 4;
        return 5;
    }
}
