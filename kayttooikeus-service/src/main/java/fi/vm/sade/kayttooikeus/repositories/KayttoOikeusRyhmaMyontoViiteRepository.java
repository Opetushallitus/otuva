package fi.vm.sade.kayttooikeus.repositories;

import java.util.List;

public interface KayttoOikeusRyhmaMyontoViiteRepository {
    List<Long> getSlaveIdsByMasterIds(List<Long> masterIds);
}
