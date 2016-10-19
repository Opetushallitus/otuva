package fi.vm.sade.kayttooikeus.repositories.impl;

import fi.vm.sade.kayttooikeus.model.OrganisaatioViite;
import fi.vm.sade.kayttooikeus.repositories.OrganisaatioViiteRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import static fi.vm.sade.kayttooikeus.model.QOrganisaatioViite.organisaatioViite;

@Repository
public class OrganisaatioViiteRepositoryImpl extends AbstractRepository implements OrganisaatioViiteRepository {

    @Override
    public List<OrganisaatioViite> findByKayttoOikeusRyhmaId(Long id) {
        return jpa().from(organisaatioViite)
                .where(organisaatioViite.kayttoOikeusRyhma.id.eq(id))
                .select(organisaatioViite).fetch();
    }

}
