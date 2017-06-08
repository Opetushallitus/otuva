package fi.vm.sade.kayttooikeus.repositories.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import fi.vm.sade.kayttooikeus.dto.AccessRightTypeDto;
import fi.vm.sade.kayttooikeus.dto.GroupTypeDto;
import fi.vm.sade.kayttooikeus.dto.MyonnettyKayttoOikeusDto;
import fi.vm.sade.kayttooikeus.model.MyonnettyKayttoOikeusRyhmaTapahtuma;
import fi.vm.sade.kayttooikeus.model.QHenkilo;
import fi.vm.sade.kayttooikeus.repositories.MyonnettyKayttoOikeusRyhmaTapahtumaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

import static fi.vm.sade.kayttooikeus.model.QKayttoOikeus.kayttoOikeus;
import fi.vm.sade.kayttooikeus.model.QKayttoOikeusRyhma;
import static fi.vm.sade.kayttooikeus.model.QKayttoOikeusRyhma.kayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.model.QMyonnettyKayttoOikeusRyhmaTapahtuma;
import static fi.vm.sade.kayttooikeus.model.QMyonnettyKayttoOikeusRyhmaTapahtuma.myonnettyKayttoOikeusRyhmaTapahtuma;
import fi.vm.sade.kayttooikeus.model.QOrganisaatioHenkilo;
import static fi.vm.sade.kayttooikeus.model.QOrganisaatioHenkilo.organisaatioHenkilo;
import static fi.vm.sade.kayttooikeus.model.QPalvelu.palvelu;

@Repository
public class MyonnettyKayttoOikeusRyhmaTapahtumaRepositoryImpl extends AbstractRepository
        implements MyonnettyKayttoOikeusRyhmaTapahtumaRepository {

    private BooleanBuilder getValidKayttoOikeusRyhmaCriteria(String oid) {
        LocalDate now = LocalDate.now();
        return new BooleanBuilder()
                .and(organisaatioHenkilo.henkilo.oidHenkilo.eq(oid))
                .and(organisaatioHenkilo.passivoitu.eq(false))
                .and(organisaatioHenkilo.henkilo.passivoitu.eq(false))
                .and(myonnettyKayttoOikeusRyhmaTapahtuma.voimassaAlkuPvm.loe(now))
                .and(myonnettyKayttoOikeusRyhmaTapahtuma.voimassaLoppuPvm.goe(now));
    }

    @Override
    public List<Long> findMasterIdsByHenkilo(String henkiloOid) {
        BooleanBuilder criteria = getValidKayttoOikeusRyhmaCriteria(henkiloOid);
        return jpa().from(myonnettyKayttoOikeusRyhmaTapahtuma)
                .where(criteria)
                .distinct()
                .select(myonnettyKayttoOikeusRyhmaTapahtuma.kayttoOikeusRyhma.id)
                .fetch();
    }

    @Override
    public List<MyonnettyKayttoOikeusDto> findByHenkiloInOrganisaatio(String henkiloOid, String organisaatioOid) {
        BooleanBuilder booleanBuilder = new BooleanBuilder()
                .and(myonnettyKayttoOikeusRyhmaTapahtuma.organisaatioHenkilo.henkilo.oidHenkilo.eq(henkiloOid))
                .and(myonnettyKayttoOikeusRyhmaTapahtuma.kayttoOikeusRyhma.hidden.eq(false));

        if (!StringUtils.isEmpty(organisaatioOid)) {
            booleanBuilder.and(organisaatioHenkilo.organisaatioOid.eq(organisaatioOid));
        }

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
                .where(booleanBuilder)
                .orderBy(myonnettyKayttoOikeusRyhmaTapahtuma.id.asc()).fetch();
    }

    @Override
    public List<AccessRightTypeDto> findValidAccessRightsByOid(String oid) {
        BooleanBuilder criteria = getValidKayttoOikeusRyhmaCriteria(oid);

        return jpa()
                .from(myonnettyKayttoOikeusRyhmaTapahtuma)
                .leftJoin(myonnettyKayttoOikeusRyhmaTapahtuma.organisaatioHenkilo, organisaatioHenkilo)
                .leftJoin(myonnettyKayttoOikeusRyhmaTapahtuma.kayttoOikeusRyhma, kayttoOikeusRyhma)
                .leftJoin(kayttoOikeusRyhma.kayttoOikeus, kayttoOikeus)
                .leftJoin(kayttoOikeus.palvelu, palvelu)
                .select(Projections.bean(AccessRightTypeDto.class,
                        myonnettyKayttoOikeusRyhmaTapahtuma.organisaatioHenkilo.organisaatioOid.as("organisaatioOid"),
                        kayttoOikeus.palvelu.name.as("palvelu"),
                        kayttoOikeus.rooli.as("rooli")))
                .where(criteria)
                .distinct()
                .fetch();
    }

    @Override
    public List<GroupTypeDto> findValidGroupsByHenkilo(String oid) {
        BooleanBuilder criteria = getValidKayttoOikeusRyhmaCriteria(oid);

        return jpa()
                .from(myonnettyKayttoOikeusRyhmaTapahtuma)
                .leftJoin(myonnettyKayttoOikeusRyhmaTapahtuma.organisaatioHenkilo, organisaatioHenkilo)
                .leftJoin(myonnettyKayttoOikeusRyhmaTapahtuma.kayttoOikeusRyhma, kayttoOikeusRyhma)
                .select(Projections.bean(GroupTypeDto.class,
                        myonnettyKayttoOikeusRyhmaTapahtuma.organisaatioHenkilo.organisaatioOid.as("organisaatioOid"),
                        myonnettyKayttoOikeusRyhmaTapahtuma.kayttoOikeusRyhma.name.as("nimi")))
                .where(criteria)
                .distinct()
                .fetch();
    }

    @Override
    public List<MyonnettyKayttoOikeusRyhmaTapahtuma> findByVoimassaLoppuPvmBefore(LocalDate voimassaLoppuPvm) {
        QMyonnettyKayttoOikeusRyhmaTapahtuma qMyonnettyKayttoOikeusRyhma = QMyonnettyKayttoOikeusRyhmaTapahtuma.myonnettyKayttoOikeusRyhmaTapahtuma;
        QKayttoOikeusRyhma qKayttoOikeusRyhma = QKayttoOikeusRyhma.kayttoOikeusRyhma;
        QOrganisaatioHenkilo qOrganisaatioHenkilo = QOrganisaatioHenkilo.organisaatioHenkilo;
        QHenkilo qHenkilo = QHenkilo.henkilo;

        return jpa().from(qMyonnettyKayttoOikeusRyhma)
                .join(qMyonnettyKayttoOikeusRyhma.organisaatioHenkilo, qOrganisaatioHenkilo).fetchJoin()
                .join(qMyonnettyKayttoOikeusRyhma.kayttoOikeusRyhma, qKayttoOikeusRyhma).fetchJoin()
                .join(qOrganisaatioHenkilo.henkilo, qHenkilo).fetchJoin()
                .where(qMyonnettyKayttoOikeusRyhma.voimassaLoppuPvm.lt(voimassaLoppuPvm))
                .distinct().select(qMyonnettyKayttoOikeusRyhma).fetch();
    }

}
