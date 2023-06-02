package ee.ria.govsso.enduserselfservice.govssosession;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
@RequiredArgsConstructor
public class GovssoSessionService {

    private final WebClient sessionRestClient;

    public List<GovssoSession> getSubjectSessions(String subject) {
        return sessionRestClient
                .get()
                .uri("/admin/sessions/{subject}", subject)
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(GovssoSession.class)
                .collectList()
                .blockOptional().orElseThrow();
    }

    public void endSession(String subject, String sessionId) {
        sessionRestClient
                .delete()
                .uri("/admin/sessions/{subject}/{sessionId}", subject, sessionId)
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    public void endSubjectSessions(String subject) {
        sessionRestClient
                .delete()
                .uri("/admin/sessions/{subject}", subject)
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

}
