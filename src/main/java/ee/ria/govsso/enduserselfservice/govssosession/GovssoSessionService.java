package ee.ria.govsso.enduserselfservice.govssosession;

import ee.ria.govsso.enduserselfservice.logging.ClientRequestLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
@RequiredArgsConstructor
public class GovssoSessionService {

    private final WebClient webClient;
    private final ClientRequestLogger requestLogger;

    public List<GovssoSession> getSubjectSessions(String subject) {
        List<GovssoSession> govssoSessions = webClient
                .get()
                .uri("/admin/sessions/{subject}", uriBuilder -> {
                    URI uri = uriBuilder.build(subject);
                    requestLogger.logRequest(uri.toString(), HttpMethod.GET);
                    return uri;
                })
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(GovssoSession.class)
                .collectList()
                .blockOptional().orElseThrow();
        requestLogger.logResponse(HttpStatus.OK.value(), govssoSessions);
        return govssoSessions;
    }

    public void endSession(String subject, String sessionId) {
        webClient
                .delete()
                .uri("/admin/sessions/{subject}/{sessionId}", uriBuilder -> {
                    URI uri = uriBuilder.build(subject, sessionId);
                    requestLogger.logRequest(uri.toString(), HttpMethod.DELETE);
                    return uri;
                })
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        requestLogger.logResponse(HttpStatus.OK.value());
    }

    public void endSubjectSessions(String subject) {
        webClient
                .delete()
                .uri("/admin/sessions/{subject}", uriBuilder -> {
                    URI uri = uriBuilder.build(subject);
                    requestLogger.logRequest(uri.toString(), HttpMethod.DELETE);
                    return uri;
                })
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        requestLogger.logResponse(HttpStatus.OK.value());
    }

}
