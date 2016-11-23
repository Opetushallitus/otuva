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
    public static class VirkailijaPaths extends Html5RedirectPathsController {
        @Override
        protected String getTargetFile() {
            return "/virkailija/index.html";
        }
    }

    /**
     * Redirects paths to given file returned by {@link #getTargetFile()},
     * for a html page to support HTML5 style navigation. However, does not
     * forward paths ending to part containing dot. E.g. resource.js or style.css etc.
     */
    public abstract static class Html5RedirectPathsController {
        protected abstract String getTargetFile();

        @RequestMapping
        public String index() {
            return getTargetFile();
        }

        @RequestMapping("{:[^\\.]*}")
        public String redirectPath() {
            return "forward:" + getTargetFile();
        }

        @RequestMapping("/**/{:[^\\.]*}")
        public String redirectPathMultiParts() {
            return "forward:" + getTargetFile();
        }
    }
}
