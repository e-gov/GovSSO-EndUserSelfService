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
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final GovssoSessionService govssoSessionService;

    @DeleteMapping("/sessions/{sessionId}")
    public Mono<?> endSession(@AuthenticationPrincipal OidcUser oidcUser, @PathVariable String sessionId) {
        if (oidcUser == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        return govssoSessionService.endSession(oidcUser.getSubject(), sessionId)
                .then(Mono.just(ResponseEntity.status(HttpStatus.OK).build()));
    }

    @DeleteMapping("/sessions")
    public Mono<?> endAllSessions(@AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        return govssoSessionService.endSubjectSessions(oidcUser.getSubject())
                .then(Mono.just(ResponseEntity.status(HttpStatus.OK).build()));
    }

}
