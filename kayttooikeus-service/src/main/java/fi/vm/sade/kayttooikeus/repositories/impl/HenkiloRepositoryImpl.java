package fi.vm.sade.kayttooikeus.repositories.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;

import fi.vm.sade.kayttooikeus.dto.KayttajaTyyppi;
import fi.vm.sade.kayttooikeus.dto.HenkilohakuCriteria;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.HenkiloHibernateRepository;
import fi.vm.sade.kayttooikeus.repositories.criteria.OrganisaatioHenkiloCriteria;
import fi.vm.sade.kayttooikeus.repositories.dto.HenkilohakuResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.querydsl.core.types.ExpressionUtils.eq;
import static java.util.stream.Collectors.toSet;

@Repository
@RequiredArgsConstructor
public class HenkiloRepositoryImpl extends BaseRepositoryImpl<Henkilo> implements HenkiloHibernateRepository {

    @Override
    public Set<String> findOidsBy(OrganisaatioHenkiloCriteria criteria) {
        QOrganisaatioHenkilo qOrganisaatio = QOrganisaatioHenkilo.organisaatioHenkilo;
        QHenkilo qHenkilo = QHenkilo.henkilo;

        JPAQuery<String> query = jpa()
                .select(qHenkilo.oidHenkilo).distinct()
                .from(qOrganisaatio)
                .join(qOrganisaatio.henkilo, qHenkilo);

        Optional.ofNullable(criteria.getKayttajaTyyppi()).ifPresent(kayttajaTyyppi
                -> query.where(qHenkilo.kayttajaTyyppi.eq(kayttajaTyyppi)));
        Optional.ofNullable(criteria.getPassivoitu()).ifPresent(passivoitu
                -> query.where(qOrganisaatio.passivoitu.eq(passivoitu)));
        Optional.ofNullable(criteria.getOrganisaatioOids()).ifPresent(organisaatioOid
                -> query.where(qOrganisaatio.organisaatioOid.in(organisaatioOid)));

        if (criteria.getKayttoOikeusRyhmaNimet() != null || criteria.getKayttooikeudet() != null) {
            QMyonnettyKayttoOikeusRyhmaTapahtuma qMyonnettyKayttoOikeusRyhma = QMyonnettyKayttoOikeusRyhmaTapahtuma.myonnettyKayttoOikeusRyhmaTapahtuma;
            QKayttoOikeusRyhma qKayttoOikeusRyhma = QKayttoOikeusRyhma.kayttoOikeusRyhma;

            query.join(qOrganisaatio.myonnettyKayttoOikeusRyhmas, qMyonnettyKayttoOikeusRyhma);
            query.join(qMyonnettyKayttoOikeusRyhma.kayttoOikeusRyhma, qKayttoOikeusRyhma);

            if (criteria.getKayttoOikeusRyhmaNimet() != null) {
                query.where(qKayttoOikeusRyhma.tunniste.in(criteria.getKayttoOikeusRyhmaNimet()));
            }
            if (criteria.getKayttooikeudet() != null) {
                QKayttoOikeus qKayttoOikeus = QKayttoOikeus.kayttoOikeus;
                QPalvelu qPalvelu = QPalvelu.palvelu;

                query.join(qKayttoOikeusRyhma.kayttoOikeus, qKayttoOikeus);
                query.join(qKayttoOikeus.palvelu, qPalvelu);
                query.where(qPalvelu.name.concat("_").concat(qKayttoOikeus.rooli).in(criteria.getKayttooikeudet()));
            }
        }

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

        Optional.ofNullable(criteria.getKayttajaTyyppi()).ifPresent(kayttajaTyyppi
                -> query.where(qHenkilo.kayttajaTyyppi.eq(kayttajaTyyppi)));
        Optional.ofNullable(criteria.getPassivoitu()).ifPresent(passivoitu -> {
            query.where(qOrganisaatio.passivoitu.eq(passivoitu));
            query.where(qOrganisaatioTarget.passivoitu.eq(passivoitu));
        });
        Optional.ofNullable(criteria.getOrganisaatioOids()).ifPresent(organisaatioOid
                -> query.where(qOrganisaatio.organisaatioOid.in(organisaatioOid)));

        if (criteria.getKayttoOikeusRyhmaNimet() != null || criteria.getKayttooikeudet() != null) {
            QMyonnettyKayttoOikeusRyhmaTapahtuma qMyonnettyKayttoOikeusRyhma = QMyonnettyKayttoOikeusRyhmaTapahtuma.myonnettyKayttoOikeusRyhmaTapahtuma;
            QKayttoOikeusRyhma qKayttoOikeusRyhma = QKayttoOikeusRyhma.kayttoOikeusRyhma;

            query.join(qOrganisaatio.myonnettyKayttoOikeusRyhmas, qMyonnettyKayttoOikeusRyhma);
            query.join(qMyonnettyKayttoOikeusRyhma.kayttoOikeusRyhma, qKayttoOikeusRyhma);

            if (criteria.getKayttoOikeusRyhmaNimet() != null) {
                query.where(qKayttoOikeusRyhma.tunniste.in(criteria.getKayttoOikeusRyhmaNimet()));
            }
            if (criteria.getKayttooikeudet() != null) {
                QKayttoOikeus qKayttoOikeus = QKayttoOikeus.kayttoOikeus;
                QPalvelu qPalvelu = QPalvelu.palvelu;

                query.join(qKayttoOikeusRyhma.kayttoOikeus, qKayttoOikeus);
                query.join(qKayttoOikeus.palvelu, qPalvelu);
                query.where(qPalvelu.name.concat("_").concat(qKayttoOikeus.rooli).in(criteria.getKayttooikeudet()));
            }
        }

        return new LinkedHashSet<>(query.fetch());
    }

    @Override
    public Set<HenkilohakuResultDto> findHenkiloByCriteria(HenkilohakuCriteria criteria, KayttajaTyyppi kayttajaTyyppi) {
        QHenkilo qHenkilo = QHenkilo.henkilo;
        QOrganisaatioHenkilo qOrganisaatioHenkilo = QOrganisaatioHenkilo.organisaatioHenkilo;
        QMyonnettyKayttoOikeusRyhmaTapahtuma qMyonnettyKayttoOikeusRyhmaTapahtuma
                = QMyonnettyKayttoOikeusRyhmaTapahtuma.myonnettyKayttoOikeusRyhmaTapahtuma;
        QKayttajatiedot qKayttajatiedot = QKayttajatiedot.kayttajatiedot;

        JPAQuery<Tuple> query = jpa().from(qHenkilo)
                .innerJoin(qHenkilo.kayttajatiedot, qKayttajatiedot)
                .select(qHenkilo.sukunimiCached, qHenkilo.etunimetCached, qHenkilo.oidHenkilo, qHenkilo.kayttajatiedot.username)
                .distinct();

        if (criteria.getOrganisaatioOids() != null) {
            query.innerJoin(qHenkilo.organisaatioHenkilos, qOrganisaatioHenkilo)
                    .on(qOrganisaatioHenkilo.passivoitu.isFalse()
                            .and(qOrganisaatioHenkilo.organisaatioOid.in(criteria.getOrganisaatioOids())));
        } else {
            query.leftJoin(qHenkilo.organisaatioHenkilos, qOrganisaatioHenkilo);
        }

        if (criteria.getKayttooikeusryhmaId() != null) {
            query.leftJoin(qOrganisaatioHenkilo.myonnettyKayttoOikeusRyhmas, qMyonnettyKayttoOikeusRyhmaTapahtuma);
        }

        query.where(getHenkiloWhereCriteria(criteria, kayttajaTyyppi, qHenkilo, qKayttajatiedot, qMyonnettyKayttoOikeusRyhmaTapahtuma));

        return query.fetch().stream().map(tuple -> new HenkilohakuResultDto(
                tuple.get(qHenkilo.oidHenkilo),
                tuple.get(qHenkilo.etunimetCached),
                tuple.get(qHenkilo.sukunimiCached),
                tuple.get(qHenkilo.kayttajatiedot.username)
        )).collect(toSet());
    }

    private BooleanBuilder getHenkiloWhereCriteria(
                                HenkilohakuCriteria criteria,
                                KayttajaTyyppi kayttajaTyyppi,
                                QHenkilo qHenkilo,
                                QKayttajatiedot qKayttajatiedot,
                                QMyonnettyKayttoOikeusRyhmaTapahtuma qMyonnettyKayttoOikeusRyhmaTapahtuma) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qHenkilo.kayttajaTyyppi.eq(kayttajaTyyppi));

        if (StringUtils.hasLength(criteria.getNameQuery())) {
            String trimmedQuery = criteria.getNameQuery().trim();
            builder.and(
                Expressions.anyOf(
                    qHenkilo.oidHenkilo.eq(trimmedQuery),
                    qHenkilo.etunimetCached.startsWithIgnoreCase(trimmedQuery),
                    qHenkilo.sukunimiCached.startsWithIgnoreCase(trimmedQuery),
                    qHenkilo.kutsumanimiCached.startsWithIgnoreCase(trimmedQuery),
                    qKayttajatiedot.username.startsWithIgnoreCase(trimmedQuery)
                )
            );
        }

        if (criteria.getKayttooikeusryhmaId() != null) {
            builder.and(qMyonnettyKayttoOikeusRyhmaTapahtuma.kayttoOikeusRyhma.id.eq(criteria.getKayttooikeusryhmaId()));
        }

        return builder;
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

    @Override
    public Set<String> findOidsByKayttoOikeusRyhmaId(Long kayttoOikeusRyhmaId) {
        QMyonnettyKayttoOikeusRyhmaTapahtuma qMyonnettyKayttoOikeusRyhma = QMyonnettyKayttoOikeusRyhmaTapahtuma.myonnettyKayttoOikeusRyhmaTapahtuma;
        QKayttoOikeusRyhma qKayttoOikeusRyhma = QKayttoOikeusRyhma.kayttoOikeusRyhma;
        QOrganisaatioHenkilo qOrganisaatioHenkilo = QOrganisaatioHenkilo.organisaatioHenkilo;
        QHenkilo qHenkilo = QHenkilo.henkilo;

        return jpa().from(qMyonnettyKayttoOikeusRyhma)
                .join(qMyonnettyKayttoOikeusRyhma.kayttoOikeusRyhma, qKayttoOikeusRyhma)
                .join(qMyonnettyKayttoOikeusRyhma.organisaatioHenkilo, qOrganisaatioHenkilo)
                .join(qOrganisaatioHenkilo.henkilo, qHenkilo)
                .where(qKayttoOikeusRyhma.id.eq(kayttoOikeusRyhmaId))
                .where(qOrganisaatioHenkilo.passivoitu.isFalse())
                .select(qHenkilo.oidHenkilo).distinct()
                .fetch().stream().collect(toSet());
    }

    @Override
    public Set<String> findOidsByHavingUsername() {
        QKayttajatiedot qKayttajatiedot = QKayttajatiedot.kayttajatiedot;
        QHenkilo qHenkilo = QHenkilo.henkilo;

        return jpa().from(qKayttajatiedot)
                .join(qKayttajatiedot.henkilo, qHenkilo)
                .where(qKayttajatiedot.username.isNotNull())
                .select(qHenkilo.oidHenkilo).distinct()
                .fetch().stream().collect(toSet());
    }

}
