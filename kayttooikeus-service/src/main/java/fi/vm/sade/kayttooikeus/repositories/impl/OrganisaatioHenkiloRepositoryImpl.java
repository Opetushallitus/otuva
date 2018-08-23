package fi.vm.sade.kayttooikeus.repositories.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloDto;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloWithOrganisaatioDto;
import fi.vm.sade.kayttooikeus.dto.PalveluRooliGroup;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.OrganisaatioHenkiloCustomRepository;
import fi.vm.sade.kayttooikeus.repositories.criteria.OrganisaatioHenkiloCriteria;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.*;

import static fi.vm.sade.kayttooikeus.model.QHenkilo.henkilo;
import static fi.vm.sade.kayttooikeus.model.QOrganisaatioHenkilo.organisaatioHenkilo;

@Repository
public class OrganisaatioHenkiloRepositoryImpl implements OrganisaatioHenkiloCustomRepository {

    private final EntityManager em;

    public OrganisaatioHenkiloRepositoryImpl(JpaContext context) {
        this.em = context.getEntityManagerByManagedType(OrganisaatioHenkilo.class);
    }

    private JPAQueryFactory jpa() {
        return new JPAQueryFactory(this.em);
    }

    public static BooleanExpression voimassa(QOrganisaatioHenkilo oh, LocalDate at) {
        return oh.passivoitu.eq(false)
                .and(oh.voimassaAlkuPvm.isNull().or(oh.voimassaAlkuPvm.loe(at)))
                .and(oh.voimassaLoppuPvm.isNull().or(oh.voimassaLoppuPvm.goe(at)));
    }

    public static BooleanBuilder hasAnyPalveluRooli(QPalvelu palvelu, QKayttoOikeus kayttoOikeus, PalveluRooliGroup palveluRooliList) {
        HashMap<String, Set<String>> requiredRoles = new HashMap<>();
        if(palveluRooliList == PalveluRooliGroup.HENKILOHAKU) {
            HashSet<String> henkilohakuKayttooikeusRoolit = new HashSet<>(Arrays.asList("REKISTERINPITAJA", "READ", "CRUD"));
            HashSet<String> henkilohakuOppijanumerorekisteriRoolit = new HashSet<>(Arrays.asList("REKISTERINPITAJA_READ","REKISTERINPITAJA","READ","HENKILON_RU"));
            requiredRoles.put("KAYTTOOIKEUS", henkilohakuKayttooikeusRoolit);
            requiredRoles.put("OPPIJANUMEROREKISTERI", henkilohakuOppijanumerorekisteriRoolit);
        }

        BooleanBuilder hasPalveluRooliBooleanBuilder = new BooleanBuilder();
        requiredRoles.forEach((p,r) -> {
            hasPalveluRooliBooleanBuilder.or(palvelu.name.eq(p).and(kayttoOikeus.rooli.in(r)));
        });
        return hasPalveluRooliBooleanBuilder;
    }

    @Override
    public List<String> findDistinctOrganisaatiosForHenkiloOid(String henkiloOid) {
        return jpa().from(organisaatioHenkilo)
                .innerJoin(organisaatioHenkilo.henkilo, henkilo)
                .where(voimassa(organisaatioHenkilo, LocalDate.now())
                        .and(henkilo.oidHenkilo.eq(henkiloOid)))
                .select(organisaatioHenkilo.organisaatioOid).distinct().fetch();
    }

    @Override
    public List<String> findUsersOrganisaatioHenkilosByPalveluRoolis(String henkiloOid, PalveluRooliGroup requiredRoles) {
        QHenkilo henkilo = QHenkilo.henkilo;
        QOrganisaatioHenkilo organisaatioHenkilo = QOrganisaatioHenkilo.organisaatioHenkilo;
        QMyonnettyKayttoOikeusRyhmaTapahtuma myonnettyKayttoOikeusRyhmaTapahtuma = QMyonnettyKayttoOikeusRyhmaTapahtuma.myonnettyKayttoOikeusRyhmaTapahtuma;
        QKayttoOikeusRyhma kayttoOikeusRyhma = QKayttoOikeusRyhma.kayttoOikeusRyhma;
        QKayttoOikeus kayttoOikeus = QKayttoOikeus.kayttoOikeus;
        QPalvelu palvelu = QPalvelu.palvelu;

        return jpa().from(organisaatioHenkilo)
                .innerJoin(organisaatioHenkilo.henkilo, henkilo)
                .innerJoin(organisaatioHenkilo.myonnettyKayttoOikeusRyhmas, myonnettyKayttoOikeusRyhmaTapahtuma)
                .innerJoin(myonnettyKayttoOikeusRyhmaTapahtuma.kayttoOikeusRyhma, kayttoOikeusRyhma)
                .innerJoin(kayttoOikeusRyhma.kayttoOikeus, kayttoOikeus)
                .innerJoin(kayttoOikeus.palvelu, palvelu)
                .where(voimassa(organisaatioHenkilo, LocalDate.now())
                        .and(henkilo.oidHenkilo.eq(henkiloOid))
                        .and(hasAnyPalveluRooli(palvelu, kayttoOikeus, requiredRoles)))
                .select(organisaatioHenkilo.organisaatioOid)
                .distinct().fetch();

    }

    @Override
    public List<OrganisaatioHenkiloWithOrganisaatioDto> findActiveOrganisaatioHenkiloListDtos(String henkiloOoid) {
        return this.findActiveOrganisaatioHenkiloListDtos(henkiloOoid, null);
    }

    @Override
    public List<OrganisaatioHenkiloWithOrganisaatioDto> findActiveOrganisaatioHenkiloListDtos(String henkiloOoid, PalveluRooliGroup requiredRoles) {
        QOrganisaatioHenkilo organisaatioHenkilo = QOrganisaatioHenkilo.organisaatioHenkilo;
        QHenkilo henkilo = QHenkilo.henkilo;
        QMyonnettyKayttoOikeusRyhmaTapahtuma myonnettyKayttoOikeusRyhmaTapahtuma = QMyonnettyKayttoOikeusRyhmaTapahtuma.myonnettyKayttoOikeusRyhmaTapahtuma;
        QKayttoOikeusRyhma kayttoOikeusRyhma = QKayttoOikeusRyhma.kayttoOikeusRyhma;
        QKayttoOikeus kayttoOikeus = QKayttoOikeus.kayttoOikeus;
        QPalvelu palvelu = QPalvelu.palvelu;

        JPAQuery<?> query = jpa().from(organisaatioHenkilo)
                .innerJoin(organisaatioHenkilo.henkilo, henkilo)
                .leftJoin(organisaatioHenkilo.myonnettyKayttoOikeusRyhmas, myonnettyKayttoOikeusRyhmaTapahtuma)
                .leftJoin(myonnettyKayttoOikeusRyhmaTapahtuma.kayttoOikeusRyhma, kayttoOikeusRyhma)
                .leftJoin(kayttoOikeusRyhma.kayttoOikeus, kayttoOikeus)
                .leftJoin(kayttoOikeus.palvelu, palvelu);

        query.where(voimassa(organisaatioHenkilo, LocalDate.now())
            .and(henkilo.oidHenkilo.eq(henkiloOoid)));

        // Suodatetaan pois henkilön organisaatiot, joihin henkilöllä ei ole yhtäkään parametrina annetun palveluroolilistan mukaista palveluroolia
        if(PalveluRooliGroup.HENKILOHAKU.equals(requiredRoles)) {
            query.where( organisaatioHenkilo.myonnettyKayttoOikeusRyhmas.isNotEmpty()
                    .and(hasAnyPalveluRooli(palvelu, kayttoOikeus, requiredRoles)));
        }

        return query.select(organisaatioHenkiloDtoProjection(OrganisaatioHenkiloWithOrganisaatioDto.class))
                .orderBy(organisaatioHenkilo.organisaatioOid.asc())
                .distinct()
                .fetch();

    }

    @Override
    public Optional<OrganisaatioHenkiloDto> findByHenkiloOidAndOrganisaatioOid(String henkiloOid, String organisaatioOid) {
        return Optional.ofNullable(jpa().from(organisaatioHenkilo)
                .join(organisaatioHenkilo.henkilo)
                .where(organisaatioHenkilo.organisaatioOid.eq(organisaatioOid),
                        organisaatioHenkilo.henkilo.oidHenkilo.eq(henkiloOid)
                ).select(organisaatioHenkiloDtoProjection(OrganisaatioHenkiloDto.class))
                .distinct().fetchOne());
    }

    @Override
    public List<OrganisaatioHenkiloDto> findOrganisaatioHenkilosForHenkilo(String henkiloOid) {
        return jpa().from(organisaatioHenkilo)
                .join(organisaatioHenkilo.henkilo)
                .where(organisaatioHenkilo.henkilo.oidHenkilo.eq(henkiloOid))
                .select(organisaatioHenkiloDtoProjection(OrganisaatioHenkiloDto.class))
                .fetch();
    }

    private<T extends OrganisaatioHenkiloDto> QBean<T> organisaatioHenkiloDtoProjection(Class<T> clz) {
        return Projections.bean(clz,
                organisaatioHenkilo.id.as("id"),
                organisaatioHenkilo.organisaatioOid.as("organisaatioOid"),
                organisaatioHenkilo.organisaatioHenkiloTyyppi.as("organisaatioHenkiloTyyppi"),
                organisaatioHenkilo.tehtavanimike.as("tehtavanimike"),
                organisaatioHenkilo.passivoitu.as("passivoitu"),
                organisaatioHenkilo.voimassaAlkuPvm.as("voimassaAlkuPvm"),
                organisaatioHenkilo.voimassaLoppuPvm.as("voimassaLoppuPvm")
        );
    }

    @Override
    public Collection<String> findOrganisaatioOidBy(OrganisaatioHenkiloCriteria criteria) {
        QOrganisaatioHenkilo qOrganisaatioHenkilo = QOrganisaatioHenkilo.organisaatioHenkilo;

        JPAQuery<String> query = jpa().from(qOrganisaatioHenkilo)
                .select(qOrganisaatioHenkilo.organisaatioOid)
                .distinct();

        Optional.ofNullable(criteria.getPassivoitu()).ifPresent(passivoitu
                -> query.where(qOrganisaatioHenkilo.passivoitu.eq(passivoitu)));
        Optional.ofNullable(criteria.getOrganisaatioOids()).ifPresent(organisaatioOids
                -> query.where(qOrganisaatioHenkilo.organisaatioOid.in(organisaatioOids)));
        Optional.ofNullable(criteria.getKayttoOikeusRyhmaNimet()).ifPresent(tunnisteet -> {
            QMyonnettyKayttoOikeusRyhmaTapahtuma qMyonnettyKayttoOikeusRyhma = QMyonnettyKayttoOikeusRyhmaTapahtuma.myonnettyKayttoOikeusRyhmaTapahtuma;
            QKayttoOikeusRyhma qKayttoOikeusRyhma = QKayttoOikeusRyhma.kayttoOikeusRyhma;

            query.join(qOrganisaatioHenkilo.myonnettyKayttoOikeusRyhmas, qMyonnettyKayttoOikeusRyhma);
            query.join(qMyonnettyKayttoOikeusRyhma.kayttoOikeusRyhma, qKayttoOikeusRyhma);
            query.where(qKayttoOikeusRyhma.tunniste.in(tunnisteet));
        });

        return query.fetch();
    }

    @Override
    public boolean isHenkiloInOrganisaatio(String henkiloOid, String organisaatioOid, boolean passivoitu) {
        QOrganisaatioHenkilo qOrganisaatioHenkilo = QOrganisaatioHenkilo.organisaatioHenkilo;
        return jpa().from(qOrganisaatioHenkilo)
                .where(qOrganisaatioHenkilo.henkilo.oidHenkilo.eq(henkiloOid))
                .where(qOrganisaatioHenkilo.organisaatioOid.eq(organisaatioOid))
                .where(qOrganisaatioHenkilo.passivoitu.eq(passivoitu))
                .select(Expressions.ONE).fetchFirst() != null;
    }

    @Override
    public Set<String> findValidByKayttooikeus(String oidHenkilo, String palveluName, String rooli) {
        QHenkilo henkilo = QHenkilo.henkilo;
        QOrganisaatioHenkilo organisaatioHenkilo = QOrganisaatioHenkilo.organisaatioHenkilo;
        QKayttoOikeusRyhma kayttoOikeusRyhma = QKayttoOikeusRyhma.kayttoOikeusRyhma;
        QKayttoOikeus kayttoOikeus = QKayttoOikeus.kayttoOikeus;
        QPalvelu palvelu = QPalvelu.palvelu;
        QMyonnettyKayttoOikeusRyhmaTapahtuma myonnettyKayttoOikeusRyhmaTapahtuma = QMyonnettyKayttoOikeusRyhmaTapahtuma.myonnettyKayttoOikeusRyhmaTapahtuma;
        return new HashSet<>(jpa().from(organisaatioHenkilo)
                .innerJoin(organisaatioHenkilo.henkilo, henkilo)
                .innerJoin(organisaatioHenkilo.myonnettyKayttoOikeusRyhmas, myonnettyKayttoOikeusRyhmaTapahtuma)
                .innerJoin(myonnettyKayttoOikeusRyhmaTapahtuma.kayttoOikeusRyhma, kayttoOikeusRyhma)
                .innerJoin(kayttoOikeusRyhma.kayttoOikeus, kayttoOikeus)
                .innerJoin(kayttoOikeus.palvelu, palvelu)
                .where(organisaatioHenkilo.passivoitu.isFalse())
                .where(palvelu.name.eq(palveluName))
                .where(kayttoOikeus.rooli.eq(rooli))
                .select(organisaatioHenkilo.organisaatioOid)
                .distinct()
                .fetch());
    }
}
