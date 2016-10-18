package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.repositories.TextGroupRepository;
import fi.vm.sade.kayttooikeus.service.LocalizationService;
import fi.vm.sade.kayttooikeus.service.dto.Localizable;
import fi.vm.sade.kayttooikeus.service.dto.LocalizableDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

@Service
public class LocalizationServiceImpl implements LocalizationService {
    private TextGroupRepository textGroupRepository;
    
    @Autowired
    public LocalizationServiceImpl(TextGroupRepository textGroupRepository) {
        this.textGroupRepository = textGroupRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public <T extends LocalizableDto, C extends Collection<T>> C localize(C list) {
        localize(list.stream().flatMap(LocalizableDto::localizableTexts));
        return list;
    }
    
    protected void localize(Stream<Localizable> localizable) {
        Map<Long,List<Localizable>> byId = localizable
                .filter(v -> v != null && v.getId() != null).collect(groupingBy(Localizable::getId));
        if (!byId.isEmpty()) {
            textGroupRepository.findTexts(byId.keySet())
                .forEach(fetched -> byId.get(fetched.getTextGroupId())
                    .forEach(text -> text.put(fetched.getLang(), fetched.getText())));
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public <T extends LocalizableDto> T localize(T dto) {
        if (dto != null) {
            localize(dto.localizableTexts());
        }
        return dto;
    }
}
