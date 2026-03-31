package fi.vm.sade.otuva.mocksubstanceservice.controller;

import fi.vm.sade.otuva.mocksubstanceservice.services.CasOppijaUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private void populateSessionDetails(Authentication auth, Model model) {
        try {
            var details = (WebAuthenticationDetails) auth.getDetails();
            model.addAttribute("sessionId", details.getSessionId());
            model.addAttribute("credentials", auth.getCredentials());
        } catch (Exception e) {
            log.error("Could not acquire session id", e);
        }
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("message", "Mock Substance Service");
        model.addAttribute("currentDate", java.time.LocalDate.now());
        var auth = SecurityContextHolder.getContext().getAuthentication();
        populateSessionDetails(auth, model);
        return "home";
    }

    @GetMapping("/secure")
    public String secure(Model model) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var principal = (CasOppijaUserDetailsService.CasAuthenticatedUser) auth.getPrincipal();
        populateSessionDetails(auth, model);
        model.addAttribute("username", principal.getUsername());
        model.addAttribute("clientName", principal.getAttributes().get("clientName"));
        model.addAttribute("displayName", principal.getAttributes().get("displayName"));
        model.addAttribute("givenName", principal.getAttributes().get("givenName"));
        model.addAttribute("cn", principal.getAttributes().get("cn"));
        return "secure";
    }

}
