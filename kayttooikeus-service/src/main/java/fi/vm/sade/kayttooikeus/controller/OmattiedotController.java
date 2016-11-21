package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.service.OmatTiedotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/omattiedot")
public class OmattiedotController {
    private OmatTiedotService omatTiedotService;

    @Autowired
    public OmattiedotController(OmatTiedotService omatTiedotService) {
        this.omatTiedotService = omatTiedotService;
    }
    
    @ResponseBody
    @RequestMapping(value = "/oid", method = RequestMethod.GET, produces = "text/plain")
    public String oid() {
        return omatTiedotService.getCurrentUserOid(); 
    }
}
