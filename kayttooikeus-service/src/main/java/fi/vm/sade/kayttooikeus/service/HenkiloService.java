package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.model.HenkiloTyyppi;

import java.util.List;

public interface HenkiloService {
    List<String> findHenkilos(HenkiloTyyppi henkiloTyyppi, List<String> ooids, String groupName);
}
