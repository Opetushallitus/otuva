package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.config.properties.UrlConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/config")
public class ConfigController {
    private UrlConfiguration urlProperties;
    
    @Autowired
    public ConfigController(UrlConfiguration urlProperties) {
        this.urlProperties = urlProperties;
    }
    
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/frontProperties", method = RequestMethod.GET, produces = "text/plain")
    public String frontPropertiesJson() {
        return urlProperties.frontPropertiesToJson();
    }
}
