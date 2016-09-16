package fi.vm.sade.kayttooikeus.service.impl;

//import fi.vm.sade.kayttooikeus.dao.DbTestDao;
import fi.vm.sade.kayttooikeus.dao.DbTestDao;
import fi.vm.sade.kayttooikeus.service.HenkiloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User: tommiratamaa
 * Date: 2.9.2016
 * Time: 17.05
 */
@Service
public class HenkiloServiceImpl implements HenkiloService {
    @Autowired
    private DbTestDao dbTestDao;

    @Override
    @Transactional(readOnly = true)
    public Long countHenkilos() {
//        return Long.valueOf(55);
        return dbTestDao.countHenkilos();
    }
}
