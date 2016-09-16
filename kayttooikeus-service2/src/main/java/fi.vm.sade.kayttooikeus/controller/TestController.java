package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.Application;
import fi.vm.sade.kayttooikeus.service.HenkiloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by autio on 15.9.2016.
 */
@RestController
public class TestController {

    @Autowired
    private HenkiloService henkiloService;

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    @RequestMapping("/test")
    public Map<String,Object> test() {
        logger.debug("Sample Debug Message");
        Map<String,Object> jsonStatus = new HashMap<>();
        jsonStatus.put("henkilosCount", henkiloService.countHenkilos());
        return jsonStatus;
    }
}
