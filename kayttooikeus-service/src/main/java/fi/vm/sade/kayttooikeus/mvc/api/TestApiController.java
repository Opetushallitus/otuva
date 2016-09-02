package fi.vm.sade.kayttooikeus.mvc.api;

import com.wordnik.swagger.annotations.Api;
import fi.vm.sade.kayttooikeus.service.HenkiloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * User: tommiratamaa
 * Date: 2.9.2016
 * Time: 17.04
 */
@RestController
@Api(value = "test", description = "Test API")
@RequestMapping("/test")
public class TestApiController {
    @Autowired
    private HenkiloService henkiloService;
    
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(method = GET)
    @ResponseBody
    public Map<String,Object> test() {
        Map<String,Object> jsonStatus = new HashMap<>();
        jsonStatus.put("henkilosCount", henkiloService.countHenkilos());
        return jsonStatus;
    }
}
