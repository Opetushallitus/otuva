package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.service.HenkiloService;
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

    @RequestMapping("/test")
    public Map<String,Object> test() {
        Map<String,Object> jsonStatus = new HashMap<>();
        jsonStatus.put("henkilosCount", henkiloService.countHenkilos());
        return jsonStatus;
    }
}
