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

    @Controller
    @RequestMapping("/virkailija")
    public static class VirkailijaPaths extends RedirectPathsController {
        @Override
        protected String getTargetFile() {
            return "/virkailija/index.html";
        }
    }
    
    public abstract static class RedirectPathsController {
        protected abstract String getTargetFile();

        @RequestMapping
        public String virkailijaIndex() {
            return getTargetFile();
        }

        @RequestMapping("{:[^\\.]*}")
        public String virkailijaRedirect() {
            return "forward:" + getTargetFile();
        }

        @RequestMapping("/{:.*?}/{:[^\\.]*}")
        public String virkailijaRedirect2Part() {
            return "forward:" + getTargetFile();
        }

        @RequestMapping("/{:.*?}/{:.*?}/{:[^\\.]*}")
        public String virkailijaRedirect3Part() {
            return "forward:" + getTargetFile();
        }
    }
}
