package fi.vm.sade.kayttooikeus.repositories.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.BooleanExpression;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloListDto;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloDto;
import fi.vm.sade.kayttooikeus.model.QOrganisaatioHenkilo;
import fi.vm.sade.kayttooikeus.repositories.OrganisaatioHenkiloRepository;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static fi.vm.sade.kayttooikeus.model.QHenkilo.henkilo;
import static fi.vm.sade.kayttooikeus.model.QOrganisaatioHenkilo.organisaatioHenkilo;

@Repository
public class OrganisaatioHenkiloRepositoryImpl extends AbstractRepository implements OrganisaatioHenkiloRepository {
    public static BooleanExpression voimassa(QOrganisaatioHenkilo oh, LocalDate at) {
        return oh.passivoitu.eq(false)
                .and(oh.voimassaAlkuPvm.isNull().or(oh.voimassaAlkuPvm.loe(at)))
                .and(oh.voimassaLoppuPvm.isNull().or(oh.voimassaLoppuPvm.goe(at)));
    }

    @Override
    public List<String> findDistinctOrganisaatiosForHenkiloOid(String henkiloOid) {
        return jpa().from(organisaatioHenkilo)
                .innerJoin(organisaatioHenkilo.henkilo, henkilo)
                .where(voimassa(organisaatioHenkilo, new LocalDate())
                        .and(henkilo.oidHenkilo.eq(henkiloOid)))
                .select(organisaatioHenkilo.organisaatioOid).distinct().fetch();
    }

    @Override
    public List<OrganisaatioHenkiloListDto> findOrganisaatioHenkiloListDtos(String henkiloOoid) {
        return jpa().from(organisaatioHenkilo)
                .innerJoin(organisaatioHenkilo.henkilo, henkilo)
                .where(voimassa(organisaatioHenkilo, new LocalDate())
                        .and(henkilo.oidHenkilo.eq(henkiloOoid)))
                .select(Projections.bean(OrganisaatioHenkiloListDto.class,
                        organisaatioHenkilo.id.as("id"),
                        organisaatioHenkilo.organisaatioHenkiloTyyppi.as("tyyppi"),
                        organisaatioHenkilo.organisaatioOid.as("organisaatioOid"),
                        organisaatioHenkilo.passivoitu.as("passivoitu"),
                        organisaatioHenkilo.voimassaAlkuPvm.as("voimassaAlkuPvm"),
                        organisaatioHenkilo.voimassaLoppuPvm.as("voimassaLoppuPvm"),
                        organisaatioHenkilo.tehtavanimike.as("tehtavanimike")
                )).orderBy(organisaatioHenkilo.organisaatioOid.asc()).fetch();
    }

    @Override
    public Optional<OrganisaatioHenkiloDto> findByHenkiloOidAndOrganisaatioOid(String henkiloOid, String organisaatioOid) {
        return Optional.ofNullable(jpa().from(organisaatioHenkilo)
                .join(organisaatioHenkilo.henkilo)
                .where(
                        organisaatioHenkilo.organisaatioOid.eq(organisaatioOid),
                        organisaatioHenkilo.henkilo.oidHenkilo.eq(henkiloOid)
                ).select(organisaatioHenkiloDtoProjection())
                .distinct().fetchOne());
    }

    @Override
    public List<OrganisaatioHenkiloDto> findOrganisaatioHenkilosForHenkilo(String henkiloOid) {
        return jpa().from(organisaatioHenkilo)
                .join(organisaatioHenkilo.henkilo)
                .where(
                        organisaatioHenkilo.henkilo.oidHenkilo.eq(henkiloOid)
                ).select(organisaatioHenkiloDtoProjection())
                .fetch();
    }

    private QBean<OrganisaatioHenkiloDto> organisaatioHenkiloDtoProjection() {
        return Projections.bean(OrganisaatioHenkiloDto.class,
                organisaatioHenkilo.id.as("id"),
                organisaatioHenkilo.organisaatioOid.as("organisaatioOid"),
                organisaatioHenkilo.organisaatioHenkiloTyyppi.as("organisaatioHenkiloTyyppi"),
                organisaatioHenkilo.tehtavanimike.as("tehtavanimike"),
                organisaatioHenkilo.passivoitu.as("passivoitu"),
                organisaatioHenkilo.voimassaAlkuPvm.as("voimassaAlkuPvm"),
                organisaatioHenkilo.voimassaLoppuPvm.as("voimassaLoppuPvm")
        );
    }
}
