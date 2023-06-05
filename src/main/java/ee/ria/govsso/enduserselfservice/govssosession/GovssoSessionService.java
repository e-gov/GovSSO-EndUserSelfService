package ee.ria.govsso.enduserselfservice.govssosession;

import ee.ria.govsso.enduserselfservice.configuration.TimeConfigurationProperties;
import ee.ria.govsso.enduserselfservice.logging.ClientRequestLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
@RequiredArgsConstructor
public class GovssoSessionService {

    private final WebClient webClient;
    private final ClientRequestLogger requestLogger;
    private final TimeConfigurationProperties timeConfigurationProperties;

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
        return govssoSessions.stream()
                .map(this::withLocalTimezone)
                .toList();
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

    private GovssoSession withLocalTimezone(GovssoSession session) {
        return new GovssoSession(
                session.sessionId(),
                withLocalTimeZone(session.authenticatedAt()),
                session.ipAddresses(),
                session.userAgent(),
                session.services()
                        .stream()
                        .map(this::withLocalTimezone)
                        .toList()
        );
    }

    private GovssoSession.Service withLocalTimezone(GovssoSession.Service service) {
        return new GovssoSession.Service(
                service.clientNames(),
                withLocalTimeZone(service.authenticatedAt()),
                withLocalTimeZone(service.expiresAt()),
                withLocalTimeZone(service.lastUpdatedAt())
        );
    }

    private OffsetDateTime withLocalTimeZone(OffsetDateTime offsetDateTime) {
        return offsetDateTime.atZoneSameInstant(timeConfigurationProperties.localZone()).toOffsetDateTime();
    }

}
