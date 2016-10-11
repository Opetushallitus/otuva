package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.Application;
import fi.vm.sade.kayttooikeus.service.HenkiloService;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by autio on 15.9.2016.
 */
@RestController
@Api(value = "test", description = "Test API")
public class TestController extends AbstractApiController{

    @Autowired
    private HenkiloService henkiloService;

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public Map<String,Object> test() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        for (GrantedAuthority grantedAuthority : auth.getAuthorities()) {
            logger.info("auth " + grantedAuthority.getAuthority());
        }
        return getHenkilosCount();
    }

    @RequestMapping(value = "/test2", method = RequestMethod.GET)
    @PreAuthorize("hasAnyRole('ROLE_APP_HENKILONHALLINTA_READ', 'ROLE_APP_HENKILONHALLINTA_CRUD')")
    public Map<String,Object> test2() {
        return getHenkilosCount();
    }

    private Map<String, Object> getHenkilosCount() {
        logger.debug("Sample Debug Message");
        Map<String,Object> jsonStatus = new HashMap<>();
        jsonStatus.put("henkilosCount", henkiloService.countHenkilos());
        return jsonStatus;
    }
}
