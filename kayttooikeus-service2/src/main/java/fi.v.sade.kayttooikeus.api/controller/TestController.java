package fi.v.sade.kayttooikeus.api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by autio on 15.9.2016.
 */
@RestController
public class TestController {

    @RequestMapping("/")
    public String welcome() {
        return "welcome";
    }
}
