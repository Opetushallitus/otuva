package fi.vm.sade.kayttooikeus.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class StaticController {
    @RequestMapping("/")
    public String index() {
        return "redirect:/virkailija";
    }

    @RequestMapping("/swagger")
    public String swagger() {
        return "redirect:/swagger-ui.html";
    }

    @RequestMapping("/virkailija")
    public String virkailijaIndex() {
        return "/virkailija/index.html";
    }

    @RequestMapping(value = "/virkailija/{[path:[^\\.]*}")
    public String redirect() {
        return "forward:/virkailija/index.html";
    }
}
