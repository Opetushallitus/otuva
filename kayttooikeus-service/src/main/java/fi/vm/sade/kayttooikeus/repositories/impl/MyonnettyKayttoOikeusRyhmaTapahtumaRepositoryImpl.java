package fi.vm.sade.kayttooikeus.repositories.impl;

import com.google.common.collect.Sets;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import fi.vm.sade.kayttooikeus.dto.AccessRightTypeDto;
import fi.vm.sade.kayttooikeus.dto.GroupTypeDto;
import fi.vm.sade.kayttooikeus.dto.KayttooikeusPerustiedotDto;
import fi.vm.sade.kayttooikeus.dto.MyonnettyKayttoOikeusDto;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.MyonnettyKayttoOikeusRyhmaTapahtumaRepositoryCustom;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static fi.vm.sade.kayttooikeus.model.QKayttoOikeus.kayttoOikeus;
import static fi.vm.sade.kayttooikeus.model.QKayttoOikeusRyhma.kayttoOikeusRyhma;
import static fi.vm.sade.kayttooikeus.model.QMyonnettyKayttoOikeusRyhmaTapahtuma.myonnettyKayttoOikeusRyhmaTapahtuma;

import javax.persistence.EntityManager;

import static fi.vm.sade.kayttooikeus.model.QOrganisaatioHenkilo.organisaatioHenkilo;
import static fi.vm.sade.kayttooikeus.model.QPalvelu.palvelu;

@Repository
public class MyonnettyKayttoOikeusRyhmaTapahtumaRepositoryImpl implements MyonnettyKayttoOikeusRyhmaTapahtumaRepositoryCustom {

    private final EntityManager entityManager;

    public MyonnettyKayttoOikeusRyhmaTapahtumaRepositoryImpl(JpaContext jpaContext) {
        this.entityManager = jpaContext.getEntityManagerByManagedType(MyonnettyKayttoOikeusRyhmaTapahtuma.class);
    }

    private JPAQueryFactory jpa() {
        return new JPAQueryFactory(this.entityManager);
    }

    private BooleanBuilder getValidKayttoOikeusRyhmaCriteria(String oid) {
        LocalDate now = LocalDate.now();
        return new BooleanBuilder()
                .and(organisaatioHenkilo.henkilo.oidHenkilo.eq(oid))
                .and(organisaatioHenkilo.passivoitu.eq(false))
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

    @Override
    public List<KayttooikeusPerustiedotDto.KayttooikeusOrganisaatiotDto> listCurrentKayttooikeusForHenkilo(String oidHenkilo) {
        QMyonnettyKayttoOikeusRyhmaTapahtuma myonnettyKayttoOikeusRyhmaTapahtuma = QMyonnettyKayttoOikeusRyhmaTapahtuma.myonnettyKayttoOikeusRyhmaTapahtuma;
        QOrganisaatioHenkilo organisaatioHenkilo = QOrganisaatioHenkilo.organisaatioHenkilo;
        QHenkilo henkilo = QHenkilo.henkilo;
        QKayttoOikeusRyhma kayttoOikeusRyhma = QKayttoOikeusRyhma.kayttoOikeusRyhma;
        QKayttoOikeus kayttoOikeus = QKayttoOikeus.kayttoOikeus;

        return jpa()
                .select(henkilo.oidHenkilo, organisaatioHenkilo.organisaatioOid, kayttoOikeusRyhma)
                .distinct()
                .from(myonnettyKayttoOikeusRyhmaTapahtuma)
                .leftJoin(myonnettyKayttoOikeusRyhmaTapahtuma.organisaatioHenkilo, organisaatioHenkilo)
                .leftJoin(organisaatioHenkilo.henkilo, henkilo)
                .leftJoin(myonnettyKayttoOikeusRyhmaTapahtuma.kayttoOikeusRyhma, kayttoOikeusRyhma)
                .leftJoin(kayttoOikeusRyhma.kayttoOikeus, kayttoOikeus)
                .where(henkilo.oidHenkilo.eq(oidHenkilo))
                .fetch()
                .stream()
                .map(tuple -> KayttooikeusPerustiedotDto.KayttooikeusOrganisaatiotDto.builder()
                        .organisaatioOid(tuple.get(organisaatioHenkilo.organisaatioOid))
                        .kayttooikeusOikeudetDtoSet(
                                Sets.newHashSet(Optional.ofNullable(tuple.get(kayttoOikeusRyhma)).orElse(new KayttoOikeusRyhma()).getKayttoOikeus()
                                        .stream()
                                        .map(kayttoOikeus1 -> KayttooikeusPerustiedotDto.KayttooikeusOrganisaatiotDto.KayttooikeusOikeudetDto.builder()
                                                .oikeus(kayttoOikeus1.getRooli())
                                                .palvelu(kayttoOikeus1.getPalvelu().getName())
                                                .build())
                                        .collect(Collectors.toSet())))
                        .build())
                .collect(Collectors.toList());
    }

}
