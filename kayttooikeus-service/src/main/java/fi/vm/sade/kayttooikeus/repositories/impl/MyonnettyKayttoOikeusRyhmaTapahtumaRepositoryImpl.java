package fi.vm.sade.kayttooikeus.repositories.impl;

import com.querydsl.core.types.Projections;
import fi.vm.sade.kayttooikeus.repositories.MyonnettyKayttoOikeusRyhmaTapahtumaRepository;
import fi.vm.sade.kayttooikeus.service.dto.MyonnettyKayttoOikeusDto;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static fi.vm.sade.kayttooikeus.model.QMyonnettyKayttoOikeusRyhmaTapahtuma.myonnettyKayttoOikeusRyhmaTapahtuma;
import static fi.vm.sade.kayttooikeus.model.QOrganisaatioHenkilo.organisaatioHenkilo;

@Repository
public class MyonnettyKayttoOikeusRyhmaTapahtumaRepositoryImpl extends AbstractRepository implements MyonnettyKayttoOikeusRyhmaTapahtumaRepository {

    @Override
    public List<Long> findMasterIdsByHenkilo(String henkiloOid) {
        LocalDate now = new LocalDate();

        return jpa().from(myonnettyKayttoOikeusRyhmaTapahtuma).where(
                myonnettyKayttoOikeusRyhmaTapahtuma.voimassaAlkuPvm.loe(now)
                        .and(myonnettyKayttoOikeusRyhmaTapahtuma.voimassaLoppuPvm.goe(now))
                        .and(organisaatioHenkilo.henkilo.oidHenkilo.eq(henkiloOid))
                        .and(organisaatioHenkilo.passivoitu.eq(false))
                        .and(organisaatioHenkilo.henkilo.passivoitu.eq(false)))
                .distinct().select(myonnettyKayttoOikeusRyhmaTapahtuma.kayttoOikeusRyhma.id).fetch();
    }

    @Override
    public List<MyonnettyKayttoOikeusDto> findByHenkiloInOrganisaatio(String henkiloOid, String organisaatioOid) {
        return jpa()
                .from(myonnettyKayttoOikeusRyhmaTapahtuma)
                .leftJoin(myonnettyKayttoOikeusRyhmaTapahtuma.organisaatioHenkilo, organisaatioHenkilo)
                .leftJoin(organisaatioHenkilo.henkilo)
                .select(Projections.bean(MyonnettyKayttoOikeusDto.class,
                        myonnettyKayttoOikeusRyhmaTapahtuma.kayttoOikeusRyhma.id.as("ryhmaId"),
                        myonnettyKayttoOikeusRyhmaTapahtuma.id.as("myonnettyTapahtumaId"),
                        myonnettyKayttoOikeusRyhmaTapahtuma.organisaatioHenkilo.tehtavanimike.as("tehtavanimike"),
                        myonnettyKayttoOikeusRyhmaTapahtuma.organisaatioHenkilo.organisaatioOid.as("organisaatioOid"),
                        myonnettyKayttoOikeusRyhmaTapahtuma.tila.as("tila"),
                        myonnettyKayttoOikeusRyhmaTapahtuma.kasittelija.oidHenkilo.as("kasittelijaOid"),
                        myonnettyKayttoOikeusRyhmaTapahtuma.kayttoOikeusRyhma.description.id.as("ryhmaNamesId"),
                        myonnettyKayttoOikeusRyhmaTapahtuma.voimassaAlkuPvm.as("alkuPvm"),
                        myonnettyKayttoOikeusRyhmaTapahtuma.voimassaLoppuPvm.as("voimassaPvm"),
                        myonnettyKayttoOikeusRyhmaTapahtuma.aikaleima.as("kasitelty"),
                        myonnettyKayttoOikeusRyhmaTapahtuma.syy.as("muutosSyy")
                ))
                .where(
                        myonnettyKayttoOikeusRyhmaTapahtuma.organisaatioHenkilo.henkilo.oidHenkilo.eq(henkiloOid)
                                .and(myonnettyKayttoOikeusRyhmaTapahtuma.kayttoOikeusRyhma.hidden.eq(false))
                ).orderBy(myonnettyKayttoOikeusRyhmaTapahtuma.id.asc()).fetch();
    }

}
