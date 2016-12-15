package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.dto.AccessRightTypeDto;
import fi.vm.sade.kayttooikeus.dto.GroupTypeDto;
import fi.vm.sade.kayttooikeus.dto.MyonnettyKayttoOikeusDto;

import java.util.List;

public interface MyonnettyKayttoOikeusRyhmaTapahtumaRepository {
    List<Long> findMasterIdsByHenkilo(String henkiloOid);

    List<MyonnettyKayttoOikeusDto> findByHenkiloInOrganisaatio(String henkiloOid, String organisaatioOid);

    List<AccessRightTypeDto> findValidAccessRightsByOid(String oid);

    List<GroupTypeDto> findValidGroupsByHenkilo(String oid);
}
