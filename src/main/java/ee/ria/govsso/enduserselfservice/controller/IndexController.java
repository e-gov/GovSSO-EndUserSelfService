package ee.ria.govsso.enduserselfservice.controller;

import ee.ria.govsso.enduserselfservice.govssosession.GovssoSession;
import ee.ria.govsso.enduserselfservice.govssosession.GovssoSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final GovssoSessionService govssoSessionService;

    @GetMapping("/")
    public ModelAndView index(@AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser == null) {
            return new ModelAndView("index");
        }
        List<GovssoSession> sessions = govssoSessionService.getSubjectSessions(oidcUser.getSubject());
        Map<String, Object> model = createDashboardViewModel(oidcUser, sessions);
        return new ModelAndView("dashboard", model);
    }

    private static Map<String, Object> createDashboardViewModel(OidcUser oidcUser, List<GovssoSession> sessions) {
        return Map.ofEntries(
                entry("userAttributes", oidcUser.getAttributes()),
                entry("govssoSessions", sessions));
    }

}
