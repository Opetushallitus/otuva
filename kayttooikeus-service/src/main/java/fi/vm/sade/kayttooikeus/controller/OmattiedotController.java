package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/omattiedot")
public class OmattiedotController {
    private PermissionCheckerService omatTiedotService;

    @Autowired
    public OmattiedotController(PermissionCheckerService omatTiedotService) {
        this.omatTiedotService = omatTiedotService;
    }
    
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/oid", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String oid() {
        return omatTiedotService.getCurrentUserOid(); 
    }
}
