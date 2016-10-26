package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.LocalizableDto;

import java.util.Collection;

public interface LocalizationService {
    <T extends LocalizableDto, C extends Collection<T>> C localize(C list);

    <T extends LocalizableDto> T localize(T dto);
}
