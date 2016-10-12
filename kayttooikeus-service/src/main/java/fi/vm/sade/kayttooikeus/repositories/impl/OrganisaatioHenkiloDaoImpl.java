package fi.vm.sade.kayttooikeus.repositories.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import fi.vm.sade.kayttooikeus.model.QOrganisaatioHenkilo;
import fi.vm.sade.kayttooikeus.repositories.OrganisaatioHenkiloDao;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static fi.vm.sade.kayttooikeus.model.QHenkilo.henkilo;
import static fi.vm.sade.kayttooikeus.model.QOrganisaatioHenkilo.organisaatioHenkilo;

/**
 * User: tommiratamaa
 * Date: 12/10/2016
 * Time: 14.47
 */
@Repository
public class OrganisaatioHenkiloDaoImpl extends AbstractDao implements OrganisaatioHenkiloDao {
    private static BooleanExpression voimassa(QOrganisaatioHenkilo oh, LocalDate at) {
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
}
