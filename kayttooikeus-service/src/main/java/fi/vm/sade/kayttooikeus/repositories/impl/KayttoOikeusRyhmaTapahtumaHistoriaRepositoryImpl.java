package fi.vm.sade.kayttooikeus.repositories.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import fi.vm.sade.kayttooikeus.dto.KayttoOikeudenTila;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRyhmaTapahtumaHistoriaRepository;
import fi.vm.sade.kayttooikeus.service.dto.MyonnettyKayttoOikeusDto;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class KayttoOikeusRyhmaTapahtumaHistoriaRepositoryImpl extends AbstractRepository implements KayttoOikeusRyhmaTapahtumaHistoriaRepository {

    @Override
    public List<MyonnettyKayttoOikeusDto> findByHenkiloInOrganisaatio(String henkiloOid, String organisaatioOid) {
        QKayttoOikeusRyhmaTapahtumaHistoria korth = QKayttoOikeusRyhmaTapahtumaHistoria.kayttoOikeusRyhmaTapahtumaHistoria;
        QOrganisaatioHenkilo oh = QOrganisaatioHenkilo.organisaatioHenkilo;
        QKayttoOikeusRyhma kor = new QKayttoOikeusRyhma("k1");
        QKayttoOikeus ko = new QKayttoOikeus("ko1");
        QPalvelu palvelu = new QPalvelu("p1");

        List<KayttoOikeudenTila> tilat = new ArrayList<KayttoOikeudenTila>();
        tilat.add(KayttoOikeudenTila.HYLATTY);
        tilat.add(KayttoOikeudenTila.PERUUTETTU);
        tilat.add(KayttoOikeudenTila.SULJETTU);
        BooleanExpression history = korth.tila.in(tilat);
        BooleanExpression restriction = oh.henkilo.oidHenkilo.eq(henkiloOid);
        BooleanExpression hidden = kor.hidden.eq(false);
        if (StringUtils.isNotBlank(organisaatioOid)) {
            restriction = oh.henkilo.oidHenkilo.eq(henkiloOid).and(oh.organisaatioOid.eq(organisaatioOid));
        }

        return jpa().from(korth).distinct()
                .innerJoin(korth.kayttoOikeusRyhma, kor)
                .innerJoin(korth.organisaatioHenkilo, oh)
                .innerJoin(oh.henkilo)
                .leftJoin(kor.kayttoOikeus, ko)
                .leftJoin(ko.palvelu, palvelu)
                .select(Projections.bean(MyonnettyKayttoOikeusDto.class,
                        korth.kayttoOikeusRyhma.id.as("ryhmaId"),
                        korth.id.as("myonnettyTapahtumaId"),
                        korth.organisaatioHenkilo.tehtavanimike.as("tehtavanimike"),
                        korth.organisaatioHenkilo.organisaatioOid.as("organisaatioOid"),
                        korth.tila.as("tila"),
                        korth.kasittelija.oidHenkilo.as("kasittelijaOid"),
                        korth.kayttoOikeusRyhma.description.id.as("ryhmaNamesId"),
                        korth.aikaleima.as("kasitelty"),
                        korth.syy.as("muutosSyy")

                )).where(history, restriction, hidden).fetch();
    }
}
