package fi.vm.sade.kayttooikeus.repositories.impl;

import fi.vm.sade.kayttooikeus.model.MyonnettyKayttoOikeusRyhmaTapahtuma;
import fi.vm.sade.kayttooikeus.repositories.MyonnettyKayttoOikeusRyhmaTapahtumaRepository;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static fi.vm.sade.kayttooikeus.model.QMyonnettyKayttoOikeusRyhmaTapahtuma.myonnettyKayttoOikeusRyhmaTapahtuma;
import static fi.vm.sade.kayttooikeus.model.QOrganisaatioHenkilo.organisaatioHenkilo;

@Repository
public class MyonnettyKayttoOikeusRyhmaTapahtumaRepositoryImpl extends AbstractRepository implements MyonnettyKayttoOikeusRyhmaTapahtumaRepository {
    @Override
    public List<MyonnettyKayttoOikeusRyhmaTapahtuma> findValidByHenkiloOid(String henkiloOid) {
        LocalDate now = new LocalDate();

        return jpa().from(myonnettyKayttoOikeusRyhmaTapahtuma).where(
                myonnettyKayttoOikeusRyhmaTapahtuma.voimassaAlkuPvm.loe(now)
                        .and(myonnettyKayttoOikeusRyhmaTapahtuma.voimassaLoppuPvm.goe(now))
                        .and(organisaatioHenkilo.henkilo.oidHenkilo.eq(henkiloOid))
                        .and(organisaatioHenkilo.passivoitu.eq(false))
                        .and(organisaatioHenkilo.henkilo.passivoitu.eq(false)))
                .distinct().select(myonnettyKayttoOikeusRyhmaTapahtuma).fetch();
    }

    @Override
    public List<MyonnettyKayttoOikeusRyhmaTapahtuma> findByHenkiloInOrganisaatio(String henkiloOid, String organisaatioOid) {
        return jpa()
                .from(myonnettyKayttoOikeusRyhmaTapahtuma)
                .distinct()
                .leftJoin(myonnettyKayttoOikeusRyhmaTapahtuma.organisaatioHenkilo, organisaatioHenkilo)
                .leftJoin(organisaatioHenkilo.henkilo)
                .where(
                        myonnettyKayttoOikeusRyhmaTapahtuma.organisaatioHenkilo.henkilo.oidHenkilo.eq(henkiloOid)
                                .and(myonnettyKayttoOikeusRyhmaTapahtuma.kayttoOikeusRyhma.hidden.eq(false))
                ).orderBy(myonnettyKayttoOikeusRyhmaTapahtuma.id.asc())
                .select(myonnettyKayttoOikeusRyhmaTapahtuma).fetch();
    }

}
