package ee.ria.govsso.enduserselfservice.controller;

import ee.ria.govsso.enduserselfservice.govssosession.GovssoSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final GovssoSessionService govssoSessionService;

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<?> endSession(@AuthenticationPrincipal OidcUser oidcUser, @PathVariable String sessionId) {
        if (oidcUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        govssoSessionService.endSession(oidcUser.getSubject(), sessionId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/sessions")
    public ResponseEntity<?> endAllSessions(@AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        govssoSessionService.endSubjectSessions(oidcUser.getSubject());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
