package ee.ria.govsso.enduserselfservice.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Controller
public class IndexController {

    @GetMapping("/")
    public ModelAndView index(@AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser == null) {
            return new ModelAndView("index");
        }
        return new ModelAndView("dashboard", Map.of("userAttributes", oidcUser.getAttributes()));
    }

}
