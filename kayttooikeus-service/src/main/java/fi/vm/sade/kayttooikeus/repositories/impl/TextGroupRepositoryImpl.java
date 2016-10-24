package fi.vm.sade.kayttooikeus.repositories.impl;

import fi.vm.sade.kayttooikeus.model.TextGroup;
import fi.vm.sade.kayttooikeus.repositories.TextGroupRepository;
import fi.vm.sade.kayttooikeus.service.dto.TextGroupDto;
import org.springframework.stereotype.Repository;

@Repository
public class TextGroupRepositoryImpl extends AbstractRepository implements TextGroupRepository {

    @Override
    public TextGroup insert(TextGroup textGroup) {
        return persist(textGroup);
    }
}
