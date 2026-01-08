package fi.vm.sade.kayttooikeus.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import fi.vm.sade.kayttooikeus.dto.OrganisaatioPalveluRooliDto;
import fi.vm.sade.kayttooikeus.repositories.MyonnettyKayttoOikeusRyhmaTapahtumaRepository;
import lombok.RequiredArgsConstructor;

import static java.util.stream.Collectors.toSet;

import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class KayttajarooliProvider {
    private final MyonnettyKayttoOikeusRyhmaTapahtumaRepository myonnettyKayttoOikeusRyhmaTapahtumaRepository;

    public Set<String> getByKayttajaOid(String kayttajaOid) {
        return streamRooliByKayttajaOid(kayttajaOid).collect(toSet());
    }

    Stream<String> streamRooliByKayttajaOid(String kayttajaOid) {
        return myonnettyKayttoOikeusRyhmaTapahtumaRepository.findOrganisaatioPalveluRooliByOid(kayttajaOid)
                .stream()
                .flatMap(this::getRoolit)
                .map(String::toUpperCase);
    }

    public Map<String, Set<String>> getRolesByOrganisation(String kayttajaOid) {
        var roles = new HashMap<String, Set<String>>();
        myonnettyKayttoOikeusRyhmaTapahtumaRepository.findOrganisaatioPalveluRooliByOid(kayttajaOid)
            .stream()
            .forEach(dto -> {
                var role = dto.getPalvelu() + "_" + dto.getRooli();
                var cur = roles.get(dto.getOrganisaatioOid());
                if (cur == null) {
                    cur = new HashSet<String>();
                }
                cur.add(role);
                roles.put(dto.getOrganisaatioOid(), cur);
            });
        return roles;
    }

    private Stream<String> getRoolit(OrganisaatioPalveluRooliDto dto) {
        Set<String> roolit = new HashSet<>();

        StringBuilder builder = new StringBuilder("ROLE_APP_");
        // ROLE_APP_<palvelu>
        builder.append(dto.getPalvelu());
        roolit.add(builder.toString());

        // ROLE_APP_<palvelu>_<kayttooikeus_rooli>
        builder.append("_");
        builder.append(dto.getRooli());
        roolit.add(builder.toString());

        // ROLE_APP_<palvelu>_<kayttooikeus_rooli>_<organisaatiooid>
        builder.append("_");
        builder.append(dto.getOrganisaatioOid());
        roolit.add(builder.toString());

        return roolit.stream();
    }
}
