package fi.vm.sade.kayttooikeus.repositories.impl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import fi.vm.sade.kayttooikeus.repositories.criteria.HenkiloCriteria;
import fi.vm.sade.kayttooikeus.repositories.criteria.OrganisaatioHenkiloCriteria;
import fi.vm.sade.kayttooikeus.repositories.dto.HenkilohakuResultDto;
import com.querydsl.jpa.impl.JPAQuery;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.QHenkilo;
import fi.vm.sade.kayttooikeus.repositories.HenkiloHibernateRepository;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.querydsl.core.types.ExpressionUtils.eq;
import fi.vm.sade.kayttooikeus.model.QKayttajatiedot;

import static java.util.stream.Collectors.toSet;
import fi.vm.sade.kayttooikeus.model.QKayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.model.QMyonnettyKayttoOikeusRyhmaTapahtuma;
import fi.vm.sade.kayttooikeus.model.QOrganisaatioHenkilo;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class HenkiloRepositoryImpl extends BaseRepositoryImpl<Henkilo> implements HenkiloHibernateRepository {

    private final OrganisaatioClient organisaatioClient;

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
    public List<HenkilohakuResultDto> findByCriteria(HenkiloCriteria.HenkiloCriteriaFunction<QHenkilo, QOrganisaatioHenkilo, QMyonnettyKayttoOikeusRyhmaTapahtuma> criteria,
                                                     Long offset,
                                                     List<String> organisaatioOidRestrictionList,
                                                     List<OrderSpecifier> orderBy) {
        QHenkilo qHenkilo = QHenkilo.henkilo;
        QOrganisaatioHenkilo qOrganisaatioHenkilo = QOrganisaatioHenkilo.organisaatioHenkilo;
        QMyonnettyKayttoOikeusRyhmaTapahtuma qMyonnettyKayttoOikeusRyhmaTapahtuma
                = QMyonnettyKayttoOikeusRyhmaTapahtuma.myonnettyKayttoOikeusRyhmaTapahtuma;
        QKayttajatiedot qKayttajatiedot = QKayttajatiedot.kayttajatiedot;

        JPAQuery<Tuple> query = jpa().from(qHenkilo)
                // Not excluding henkilos without organisation (different condition on where)
                .leftJoin(qHenkilo.organisaatioHenkilos, qOrganisaatioHenkilo)
                .leftJoin(qOrganisaatioHenkilo.myonnettyKayttoOikeusRyhmas, qMyonnettyKayttoOikeusRyhmaTapahtuma)
                .leftJoin(qHenkilo.kayttajatiedot, qKayttajatiedot)
                // Organisaatiohenkilos need to be added later (enrichment)
                .select(qHenkilo.sukunimiCached,
                        qHenkilo.etunimetCached,
                        qHenkilo.oidHenkilo,
                        qHenkilo.kayttajatiedot.username)
                .distinct();

        if (offset != null) {
            query.offset(offset);
        }
        query.limit(100L);

        if (organisaatioOidRestrictionList != null && !organisaatioOidRestrictionList.isEmpty()) {
            query.where(qOrganisaatioHenkilo.organisaatioOid.in(organisaatioOidRestrictionList));
        }

        if (orderBy != null) {
            orderBy.forEach(query::orderBy);
        }

        query.where(criteria.apply(qHenkilo, qOrganisaatioHenkilo, qMyonnettyKayttoOikeusRyhmaTapahtuma));

        return query.fetch().stream().map(tuple -> new HenkilohakuResultDto(
                tuple.get(qHenkilo.sukunimiCached) + ", " + tuple.get(qHenkilo.etunimetCached),
                tuple.get(qHenkilo.oidHenkilo),
                tuple.get(qHenkilo.kayttajatiedot.username)
        )).collect(Collectors.toList());
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
