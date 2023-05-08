package ee.ria.govsso.enduserselfservice.govssosession;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
@RequiredArgsConstructor
public class GovssoSessionService {

    private final WebClient sessionRestClient;

    public Flux<GovssoSession> getSubjectSessions(String subject) {
        return sessionRestClient
                .get()
                .uri("/admin/sessions/{subject}", subject)
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(GovssoSession.class);
    }

    public Mono<Void> endSession(String subject, String sessionId) {
        return sessionRestClient
                .delete()
                .uri("/admin/sessions/{subject}/{sessionId}", subject, sessionId)
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<Void> endSubjectSessions(String subject) {
        return sessionRestClient
                .delete()
                .uri("/admin/sessions/{subject}", subject)
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Void.class);
    }

}
