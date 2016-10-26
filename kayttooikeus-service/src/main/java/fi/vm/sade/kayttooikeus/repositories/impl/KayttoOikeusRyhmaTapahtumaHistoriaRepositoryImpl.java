package fi.vm.sade.kayttooikeus.repositories.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import fi.vm.sade.kayttooikeus.dto.KayttoOikeudenTila;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRyhmaTapahtumaHistoriaRepository;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class KayttoOikeusRyhmaTapahtumaHistoriaRepositoryImpl extends AbstractRepository implements KayttoOikeusRyhmaTapahtumaHistoriaRepository {

    @Override
    public List<KayttoOikeusRyhmaTapahtumaHistoria> findByHenkiloInOrganisaatio(String henkiloOid, String organisaatioOid) {
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
                .innerJoin(korth.kayttoOikeusRyhma, kor).fetchJoin()
                .innerJoin(korth.organisaatioHenkilo, oh)
                .innerJoin(oh.henkilo)
                .leftJoin(kor.kayttoOikeus, ko).fetchJoin()
                .leftJoin(ko.palvelu, palvelu).fetchJoin()
                .where(history, restriction, hidden)
                .select(korth).fetch();

    }
}
