package fi.vm.sade.kayttooikeus.repositories.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import fi.vm.sade.kayttooikeus.dto.HenkiloTyyppi;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.HenkiloHibernateRepository;
import fi.vm.sade.kayttooikeus.repositories.criteria.HenkiloCriteria;
import fi.vm.sade.kayttooikeus.repositories.criteria.OrganisaatioHenkiloCriteria;
import fi.vm.sade.kayttooikeus.repositories.dto.HenkilohakuResultDto;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.querydsl.core.types.ExpressionUtils.eq;
import static fi.vm.sade.kayttooikeus.model.QHenkilo.henkilo;
import static fi.vm.sade.kayttooikeus.model.QKayttajatiedot.kayttajatiedot;
import static fi.vm.sade.kayttooikeus.model.QMyonnettyKayttoOikeusRyhmaTapahtuma.myonnettyKayttoOikeusRyhmaTapahtuma;
import static fi.vm.sade.kayttooikeus.model.QOrganisaatioHenkilo.organisaatioHenkilo;

@Repository
public class HenkiloRepositoryImpl extends BaseRepositoryImpl<Henkilo> implements HenkiloHibernateRepository {

    @Override
    public Set<String> findOidsBy(OrganisaatioHenkiloCriteria criteria) {
        QOrganisaatioHenkilo qOrganisaatio = QOrganisaatioHenkilo.organisaatioHenkilo;
        QHenkilo qHenkilo = QHenkilo.henkilo;

        JPAQuery<String> query = jpa()
                .select(qHenkilo.oidHenkilo).distinct()
                .from(qOrganisaatio)
                .join(qOrganisaatio.henkilo, qHenkilo);

        Optional.ofNullable(criteria.getPassivoitu()).ifPresent(passivoitu
                -> query.where(qOrganisaatio.passivoitu.eq(passivoitu)));
        Optional.ofNullable(criteria.getOrganisaatioOids()).ifPresent(organisaatioOid
                -> query.where(qOrganisaatio.organisaatioOid.in(organisaatioOid)));
        Optional.ofNullable(criteria.getKayttoOikeusRyhmaNimet()).ifPresent(kayttoOikeusRyhmaNimet -> {
            QMyonnettyKayttoOikeusRyhmaTapahtuma qMyonnettyKayttoOikeusRyhma = QMyonnettyKayttoOikeusRyhmaTapahtuma.myonnettyKayttoOikeusRyhmaTapahtuma;
            QKayttoOikeusRyhma qKayttoOikeusRyhma = QKayttoOikeusRyhma.kayttoOikeusRyhma;

            query.join(qOrganisaatio.myonnettyKayttoOikeusRyhmas, qMyonnettyKayttoOikeusRyhma);
            query.join(qMyonnettyKayttoOikeusRyhma.kayttoOikeusRyhma, qKayttoOikeusRyhma);
            query.where(qKayttoOikeusRyhma.name.in(kayttoOikeusRyhmaNimet));
        });

        return new LinkedHashSet<>(query.fetch());
    }

    @Override
    public Set<String> findOidsBySamaOrganisaatio(String henkiloOid, OrganisaatioHenkiloCriteria criteria) {
        QHenkilo qHenkilo = QHenkilo.henkilo;
        QOrganisaatioHenkilo qOrganisaatio = QOrganisaatioHenkilo.organisaatioHenkilo;
        QHenkilo qHenkiloTarget = new QHenkilo("henkiloTarget");
        QOrganisaatioHenkilo qOrganisaatioTarget = new QOrganisaatioHenkilo("organisaatioTarget");

        JPAQuery<String> query = jpa()
                .select(qHenkiloTarget.oidHenkilo).distinct()
                .from(qHenkilo, qHenkiloTarget)
                .join(qHenkilo.organisaatioHenkilos, qOrganisaatio)
                .join(qHenkiloTarget.organisaatioHenkilos, qOrganisaatioTarget)
                .where(qHenkilo.oidHenkilo.eq(henkiloOid))
                .where(eq(qOrganisaatio.organisaatioOid, qOrganisaatioTarget.organisaatioOid));

        Optional.ofNullable(criteria.getPassivoitu()).ifPresent(passivoitu -> {
            query.where(qOrganisaatio.passivoitu.eq(passivoitu));
            query.where(qOrganisaatioTarget.passivoitu.eq(passivoitu));
        });
        Optional.ofNullable(criteria.getOrganisaatioOids()).ifPresent(organisaatioOid
                -> query.where(qOrganisaatio.organisaatioOid.in(organisaatioOid)));
        Optional.ofNullable(criteria.getKayttoOikeusRyhmaNimet()).ifPresent(kayttoOikeusRyhmaNimet -> {
            QMyonnettyKayttoOikeusRyhmaTapahtuma qMyonnettyKayttoOikeusRyhma = QMyonnettyKayttoOikeusRyhmaTapahtuma.myonnettyKayttoOikeusRyhmaTapahtuma;
            QKayttoOikeusRyhma qKayttoOikeusRyhma = QKayttoOikeusRyhma.kayttoOikeusRyhma;

            query.join(qOrganisaatio.myonnettyKayttoOikeusRyhmas, qMyonnettyKayttoOikeusRyhma);
            query.join(qMyonnettyKayttoOikeusRyhma.kayttoOikeusRyhma, qKayttoOikeusRyhma);
            query.where(qKayttoOikeusRyhma.name.in(kayttoOikeusRyhmaNimet));
        });

        return new LinkedHashSet<>(query.fetch());
    }

    @Override
    public List<HenkilohakuResultDto> findByCriteria(HenkiloCriteria criteria) {
        QHenkilo qHenkilo = QHenkilo.henkilo;
        QOrganisaatioHenkilo qOrganisaatioHenkilo = QOrganisaatioHenkilo.organisaatioHenkilo;
        QMyonnettyKayttoOikeusRyhmaTapahtuma qMyonnettyKayttoOikeusRyhmaTapahtuma
                = QMyonnettyKayttoOikeusRyhmaTapahtuma.myonnettyKayttoOikeusRyhmaTapahtuma;
        QKayttajatiedot qKayttajatiedot = QKayttajatiedot.kayttajatiedot;

        JPAQuery<HenkilohakuResultDto> query = jpa().from(qHenkilo)
                // Not excluding henkilos without organisation (different condition on where)
                .leftJoin(qHenkilo.organisaatioHenkilos, qOrganisaatioHenkilo)
                .leftJoin(qOrganisaatioHenkilo.myonnettyKayttoOikeusRyhmas, qMyonnettyKayttoOikeusRyhmaTapahtuma)
                .leftJoin(qHenkilo.kayttajatiedot, qKayttajatiedot)
                // Organisaatiohenkilos need to be added later (enrichment)
                .select(Projections.constructor(HenkilohakuResultDto.class,
                        qHenkilo.etunimetCached.append(", ").append(qHenkilo.sukunimiCached),
                        qHenkilo.oidHenkilo,
                        qKayttajatiedot.username));

        query.where(criteria.condition(qHenkilo, qOrganisaatioHenkilo, qMyonnettyKayttoOikeusRyhmaTapahtuma));

        return query.distinct().fetch();
    }

    @Override
    public List<String> findHenkiloOids(HenkiloTyyppi henkiloTyyppi, List<String> ooids, String groupName) {
        BooleanBuilder booleanBuilder = new BooleanBuilder()
                .and(henkilo.henkiloTyyppi.eq(henkiloTyyppi));

        if (!CollectionUtils.isEmpty(ooids)) {
            booleanBuilder.and(organisaatioHenkilo.organisaatioOid.in(ooids));
        }
        if (!StringUtils.isEmpty(groupName)) {
            booleanBuilder.and(myonnettyKayttoOikeusRyhmaTapahtuma.kayttoOikeusRyhma.name.eq(groupName));
        }

        BooleanBuilder voimassa = new BooleanBuilder()
                .and(myonnettyKayttoOikeusRyhmaTapahtuma.voimassaAlkuPvm.loe(LocalDate.now())
                        .or(myonnettyKayttoOikeusRyhmaTapahtuma.voimassaAlkuPvm.isNull()))
                .and(myonnettyKayttoOikeusRyhmaTapahtuma.voimassaLoppuPvm.gt(LocalDate.now())
                        .or(myonnettyKayttoOikeusRyhmaTapahtuma.voimassaLoppuPvm.isNull()));
        booleanBuilder.and(voimassa);

        return jpa().from(henkilo)
                .leftJoin(henkilo.kayttajatiedot, kayttajatiedot)
                .leftJoin(henkilo.organisaatioHenkilos, organisaatioHenkilo)
                .leftJoin(organisaatioHenkilo.myonnettyKayttoOikeusRyhmas, myonnettyKayttoOikeusRyhmaTapahtuma)
                .distinct()
                .select(henkilo.oidHenkilo)
                .where(booleanBuilder)
                .fetch();
    }

    @Override
    public List<Henkilo> findByKayttoOikeusRyhmatAndOrganisaatiot(Set<Long> kayttoOikeusRyhmaIds, Set<String> organisaatioOids) {
        QMyonnettyKayttoOikeusRyhmaTapahtuma qMyonnettyKayttoOikeusRyhma = QMyonnettyKayttoOikeusRyhmaTapahtuma.myonnettyKayttoOikeusRyhmaTapahtuma;
        QKayttoOikeusRyhma qKayttoOikeusRyhma = QKayttoOikeusRyhma.kayttoOikeusRyhma;
        QOrganisaatioHenkilo qOrganisaatioHenkilo = QOrganisaatioHenkilo.organisaatioHenkilo;
        QHenkilo qHenkilo = QHenkilo.henkilo;

        return jpa().from(qMyonnettyKayttoOikeusRyhma)
                .join(qMyonnettyKayttoOikeusRyhma.kayttoOikeusRyhma, qKayttoOikeusRyhma)
                .join(qMyonnettyKayttoOikeusRyhma.organisaatioHenkilo, qOrganisaatioHenkilo)
                .join(qOrganisaatioHenkilo.henkilo, qHenkilo)
                .where(qKayttoOikeusRyhma.id.in(kayttoOikeusRyhmaIds))
                .where(qOrganisaatioHenkilo.organisaatioOid.in(organisaatioOids))
                .where(qOrganisaatioHenkilo.passivoitu.isFalse())
                .select(qHenkilo).distinct().fetch();
    }
}
