package fi.vm.sade.kayttooikeus.repositories.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.MyonnettyKayttoOikeusRyhmaTapahtumaRepository;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Repository;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Repository
public class MyonnettyKayttoOikeusRyhmaTapahtumaRepositoryImpl extends AbstractRepository implements MyonnettyKayttoOikeusRyhmaTapahtumaRepository {
    @Override
    public List<MyonnettyKayttoOikeusRyhmaTapahtuma> findValidByHenkiloOid(String henkiloOid) {

        QMyonnettyKayttoOikeusRyhmaTapahtuma mkort = QMyonnettyKayttoOikeusRyhmaTapahtuma.myonnettyKayttoOikeusRyhmaTapahtuma;
        QOrganisaatioHenkilo oh = QOrganisaatioHenkilo.organisaatioHenkilo;

        LocalDate now = new LocalDate();

        return jpa().from(mkort).where(
                    mkort.voimassaAlkuPvm.loe(now)
                    .and(mkort.voimassaLoppuPvm.goe(now))
                    .and(oh.henkilo.oidHenkilo.eq(henkiloOid))
                    .and(oh.passivoitu.eq(false))
                    .and(oh.henkilo.passivoitu.eq(false)))
                .distinct().select(mkort).fetch();
    }

    @Override
    public List<MyonnettyKayttoOikeusRyhmaTapahtuma> findByHenkiloInOrganisaatio(String henkiloOid, String organisaatioOid) {
        QMyonnettyKayttoOikeusRyhmaTapahtuma mko = QMyonnettyKayttoOikeusRyhmaTapahtuma.myonnettyKayttoOikeusRyhmaTapahtuma;
        QOrganisaatioHenkilo oh = QOrganisaatioHenkilo.organisaatioHenkilo;
        QKayttoOikeusRyhma kor = new QKayttoOikeusRyhma("k1");
        QKayttoOikeus ko = new QKayttoOikeus("ko1");
        QPalvelu palvelu = new QPalvelu("p1");
        QTextGroup descr = new QTextGroup("descr");
        QTextGroup descr2 = new QTextGroup("descr2");

        BooleanExpression restriction = oh.henkilo.oidHenkilo.eq(henkiloOid);
        BooleanExpression hidden = kor.hidden.eq(false);
        if (StringUtils.isNotBlank(organisaatioOid)) {
            restriction = oh.henkilo.oidHenkilo.eq(henkiloOid).and(oh.organisaatioOid.eq(organisaatioOid));
        }

        return jpa().from(mko).distinct().innerJoin(mko.kayttoOikeusRyhma, kor)
                .leftJoin(kor.description, descr2)
                .leftJoin(descr2.texts)
                .innerJoin(mko.organisaatioHenkilo, oh)
                .innerJoin(oh.henkilo)
                .leftJoin(kor.kayttoOikeus, ko)
                .leftJoin(ko.palvelu, palvelu)
                .leftJoin(palvelu.description, descr)
                .leftJoin(descr.texts)
                .where(restriction, hidden).select(mko).fetch();

    }

}
