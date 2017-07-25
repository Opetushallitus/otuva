package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.dto.LocalizableOrganisaatio;
import fi.vm.sade.kayttooikeus.dto.TextGroupMapDto;
import fi.vm.sade.kayttooikeus.repositories.TextGroupRepository;
import fi.vm.sade.kayttooikeus.service.LocalizationService;
import fi.vm.sade.kayttooikeus.dto.Localizable;
import fi.vm.sade.kayttooikeus.dto.LocalizableDto;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
public class LocalizationServiceImpl implements LocalizationService {
    private final TextGroupRepository textGroupRepository;

    private final OrganisaatioClient organisaatioClient;
    
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

    @Override
    public <T extends LocalizableOrganisaatio, C extends Collection<T>> C localizeOrgs(C list) {
        list.forEach(localizableOrganisaatio -> localizableOrganisaatio.setNimi(
                new TextGroupMapDto(null, this.organisaatioClient
                        .getOrganisaatioPerustiedotCached(localizableOrganisaatio.getOrganisaatioOid(), OrganisaatioClient.Mode.requireCache())
                        .orElseThrow(() -> new NotFoundException("Organisaatio not found by oid " + localizableOrganisaatio.getOrganisaatioOid()))
                        .getNimi())));
        return list;
    }
}
