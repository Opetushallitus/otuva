package fi.vm.sade.kayttooikeus.enumeration;

import com.google.common.collect.Lists;
import com.querydsl.core.types.OrderSpecifier;
import fi.vm.sade.kayttooikeus.model.QHenkilo;
import fi.vm.sade.kayttooikeus.model.QKayttajatiedot;
import lombok.Getter;

import java.util.List;

@Getter
public enum OrderByHenkilohaku {
    HENKILO_NIMI_ASC("HENKILO_NIMI_ASC", Lists.newArrayList(QHenkilo.henkilo.sukunimiCached.asc(), QHenkilo.henkilo.etunimetCached.asc())),
    HENKILO_NIMI_DESC("HENKILO_NIMI_DESC", Lists.newArrayList(QHenkilo.henkilo.sukunimiCached.desc(), QHenkilo.henkilo.etunimetCached.desc())),
    USERNAME_ASC("USERNAME_ASC", Lists.newArrayList(QKayttajatiedot.kayttajatiedot.username.asc().nullsLast())),
    USERNAME_DESC("USERNAME_DESC", Lists.newArrayList(QKayttajatiedot.kayttajatiedot.username.desc().nullsLast())),
    ;

    private final String entry;
    private final List<OrderSpecifier> value;

    OrderByHenkilohaku(String entry, List<OrderSpecifier> value) {
        this.entry = entry;
        this.value = value;
    }
}
