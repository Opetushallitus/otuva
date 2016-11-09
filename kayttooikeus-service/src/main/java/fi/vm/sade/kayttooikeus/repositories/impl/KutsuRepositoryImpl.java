package fi.vm.sade.kayttooikeus.repositories.impl;

import fi.vm.sade.kayttooikeus.dto.KutsuListDto;
import fi.vm.sade.kayttooikeus.dto.KutsuOrganisaatioListDto;
import fi.vm.sade.kayttooikeus.model.Kutsu;
import fi.vm.sade.kayttooikeus.repositories.KutsuCriteria;
import fi.vm.sade.kayttooikeus.repositories.KutsuRepository;
import fi.vm.sade.kayttooikeus.repositories.OrderBy;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.querydsl.core.types.Projections.bean;
import static fi.vm.sade.kayttooikeus.model.QKutsu.kutsu;
import static fi.vm.sade.kayttooikeus.model.QKutsuOrganisaatio.kutsuOrganisaatio;
import static fi.vm.sade.kayttooikeus.repositories.KutsuRepository.KutsuOrganisaatioOrder.*;

@Repository
public class KutsuRepositoryImpl extends BaseRepositoryImpl<Kutsu> implements KutsuRepository {
    @Override
    public List<KutsuListDto> listKutsuListDtos(KutsuCriteria criteria) {
        return jpa().from(kutsu).where(criteria.builder(kutsu))
                .select(bean(KutsuListDto.class,
                    kutsu.id.as("id"),
                    kutsu.tila.as("tila"),
                    kutsu.aikaleima.as("aikaleima"),
                    kutsu.sahkoposti.as("sahkoposti")
                )).orderBy(kutsu.aikaleima.desc()).fetch();
    }
    
    @Override
    public List<KutsuOrganisaatioListDto> listKutsuOrganisaatioListDtos(KutsuCriteria kutsuCriteria,
            OrderBy<KutsuOrganisaatioOrder> orderBy) {
        return jpa().from(kutsu).innerJoin(kutsu.organisaatiot, kutsuOrganisaatio)
                .where(kutsuCriteria.builder(kutsu))
                .select(bean(KutsuOrganisaatioListDto.class,
                    kutsu.id.as("kutsuId"),
                    kutsuOrganisaatio.id.as("id"),
                    kutsuOrganisaatio.organisaatioOid.as("oid")
                )).orderBy(orderBy.order(SAHKOPOSTI, kutsu.sahkoposti)
                        .order(ORGANISAATIO, kutsuOrganisaatio.organisaatioOid)
                        .order(AIKALEIMA, kutsu.aikaleima)
                        .order(kutsuOrganisaatio.organisaatioOid).get()).fetch();
    }
}
