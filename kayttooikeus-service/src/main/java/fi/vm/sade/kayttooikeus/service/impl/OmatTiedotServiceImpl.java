package fi.vm.sade.kayttooikeus.service.impl;

import fi.vm.sade.kayttooikeus.service.OmatTiedotService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OmatTiedotServiceImpl extends AbstractService implements OmatTiedotService {
    @Override
    @Transactional(readOnly = true)
    public String getCurrentUserOid() {
        return super.getCurrentUserOid();
    }
}
